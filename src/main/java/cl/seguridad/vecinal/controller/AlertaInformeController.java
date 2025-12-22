package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
import cl.seguridad.vecinal.modelo.dto.AlertaResponseDto;
import cl.seguridad.vecinal.service.AlertaService;
import cl.seguridad.vecinal.service.GoogleAiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alertas")
public class AlertaInformeController {
    private static final Logger logger = LoggerFactory.getLogger(AlertaInformeController.class);

    private static final String KEY_SECTOR = "sector";
    private static final String KEY_ESTADO = "estado";

    private static final String KEY_STATUS = "status";
    private static final String KEY_LIMITE = "limite";

    private static final String KEY_POR_TIPO = "porTipo";
    private static final String KEY_POR_ESTADO = "porEstado";

    private final AlertaService alertaService;
    private final GoogleAiService googleAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AlertaInformeController(AlertaService alertaService, GoogleAiService googleAiService) {
        this.alertaService = alertaService;
        this.googleAiService = googleAiService;
    }

    @PostMapping("/informe")
    public ResponseEntity<Map<String, Object>> generarInforme(@RequestBody Map<String, Object> body) {
        try {
            FiltrosInforme filtros = parsearFiltros(body);
            RangoFechas rango = resolverRangoFechas(filtros);
            List<Alerta> candidatas = obtenerYFiltrarAlertas(rango, filtros);
            DatosAgregados agregados = calcularAgregados(candidatas);
            List<Alerta> muestra = seleccionarMuestra(candidatas, agregados.topSectores(), filtros.limiteSeguro());
            String informe = generarInformeAi(rango, candidatas.size(), agregados, muestra);
            Map<String, Object> resultado = construirRespuesta(filtros, rango, candidatas.size(), muestra, agregados, informe);

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            KEY_STATUS, "error",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            KEY_STATUS, "error",
                            "message", "No se pudo generar el informe: " + e.getMessage()
                    ));
        }
    }

    private FiltrosInforme parsearFiltros(Map<String, Object> body) {
        String fechaInicioStr = asString(body.get("fechaInicio"));
        String fechaFinStr = asString(body.get("fechaFin"));
        String tipoStr = asString(body.get("tipo"));
        String estadoStr = asString(body.get(KEY_ESTADO));
        String sector = asString(body.get(KEY_SECTOR));
        int limite = body.get(KEY_LIMITE) instanceof Number number ? number.intValue() : 100;

        TipoAlertaEnum tipoFiltro = parseTipo(tipoStr);
        EstadoAlerta estadoFiltro = parseEstado(estadoStr);
        int limiteSeguro = Math.max(1, Math.min(limite, 500));
        String sectorFiltro = sector == null ? null : sector.trim().toLowerCase();

        return new FiltrosInforme(fechaInicioStr, fechaFinStr, tipoFiltro, estadoFiltro, sectorFiltro, limiteSeguro);
    }

    private RangoFechas resolverRangoFechas(FiltrosInforme filtros) {
        LocalDateTime inicio;
        LocalDateTime fin;

        if (filtros.fechaInicioStr() != null && filtros.fechaFinStr() != null) {
            LocalDateTime[] rango = parseFechasOrThrow(filtros.fechaInicioStr(), filtros.fechaFinStr());
            inicio = rango[0];
            fin = rango[1];
        } else {
            inicio = LocalDateTime.now().minusDays(60);
            fin = LocalDateTime.now();
        }

        return new RangoFechas(inicio, fin);
    }

    private List<Alerta> obtenerYFiltrarAlertas(RangoFechas rango, FiltrosInforme filtros) {
        List<Alerta> base = (rango.inicio() != null && rango.fin() != null)
                ? alertaService.obtenerAlertasPorRangoFechas(rango.inicio(), rango.fin())
                : alertaService.obtenerAlertasDosMeses();

        return base.stream()
                .filter(a -> filtros.tipoFiltro() == null || a.getTipo() == filtros.tipoFiltro())
                .filter(a -> filtros.estadoFiltro() == null || a.getEstado() == filtros.estadoFiltro())
                .filter(a -> cumpleFiltroSector(a, filtros.sectorFiltro()))
                .sorted(Comparator.comparing(Alerta::getFechaHora).reversed())
                .toList();
    }

    private boolean cumpleFiltroSector(Alerta alerta, String sectorFiltro) {
        if (sectorFiltro == null || sectorFiltro.isBlank()) {
            return true;
        }
        String alertaSector = Optional.ofNullable(alerta.getSector()).orElse("").toLowerCase();
        return alertaSector.contains(sectorFiltro);
    }

    private DatosAgregados calcularAgregados(List<Alerta> candidatas) {
        Map<TipoAlertaEnum, Long> porTipo = candidatas.stream()
                .collect(Collectors.groupingBy(Alerta::getTipo, Collectors.counting()));

        Map<EstadoAlerta, Long> porEstado = candidatas.stream()
                .collect(Collectors.groupingBy(Alerta::getEstado, Collectors.counting()));

        Map<String, Long> porSector = candidatas.stream()
                .collect(Collectors.groupingBy(this::sectorVillaComuna, Collectors.counting()));

        Map<Integer, Long> porHora = candidatas.stream()
                .collect(Collectors.groupingBy(a -> a.getFechaHora() != null ? a.getFechaHora().getHour() : -1, Collectors.counting()));
        porHora.remove(-1);

        Map<LocalDate, Long> porDia = candidatas.stream()
                .filter(a -> a.getFechaHora() != null)
                .collect(Collectors.groupingBy(a -> a.getFechaHora().toLocalDate(), Collectors.counting()));

        Map<String, Long> porDiaSemana = candidatas.stream()
                .filter(a -> a.getFechaHora() != null)
                .collect(Collectors.groupingBy(a -> diaCorto(a.getFechaHora().getDayOfWeek()), Collectors.counting()));

        List<Map.Entry<String, Long>> topSectores = porSector.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .toList();

        int numSectores = porSector.size();
        double mediaDiaria = porDia.values().stream().mapToLong(v -> v).average().orElse(0.0);
        double medianaDiaria = calcularMediana(new ArrayList<>(porDia.values()));

        List<List<Object>> diasPico = calcularDiasPico(porDia);
        List<List<Object>> horasPico = porHora.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> toPair(e.getKey(), e.getValue()))
                .toList();
        List<List<Object>> sectoresZ = calcularZscore(porSector);

        return new DatosAgregados(porTipo, porEstado, porSector, porHora, porDia, porDiaSemana,
                topSectores, numSectores, mediaDiaria, medianaDiaria, diasPico, horasPico, sectoresZ);
    }

    private List<Alerta> seleccionarMuestra(List<Alerta> candidatas, List<Map.Entry<String, Long>> topSectores, int limiteSeguro) {
        int totalEncontradas = candidatas.size();
        int limiteMuestraHeur = Math.min(200, Math.max(50, (int) Math.sqrt(Math.max(totalEncontradas, 1)) * 5));
        int limiteMuestra = Math.min(limiteSeguro, limiteMuestraHeur);

        Set<String> sectoresTopSet = topSectores.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        List<Alerta> muestraTopSectores = candidatas.stream()
                .filter(a -> sectoresTopSet.contains(sectorVillaComuna(a)))
                .limit(limiteMuestra)
                .toList();

        Set<String> top3 = topSectores.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Alerta> recientesTop3 = candidatas.stream()
                .filter(a -> top3.contains(sectorVillaComuna(a)))
                .limit(15)
                .toList();

        LinkedHashSet<Alerta> muestraSet = new LinkedHashSet<>();
        muestraSet.addAll(muestraTopSectores);
        muestraSet.addAll(recientesTop3);

        return new ArrayList<>(muestraSet).subList(0, Math.min(muestraSet.size(), limiteMuestra));
    }

    private String generarInformeAi(RangoFechas rango, int totalEncontradas, DatosAgregados agregados, List<Alerta> muestra) throws JsonProcessingException {
        Map<String, Object> aiData = construirPayloadAi(rango, totalEncontradas, agregados, muestra);
        String prompt = construirPromptAi(aiData);
        Map<String, Object> aiRequest = construirRequestAi(prompt);

        Mono<String> respuestaMono = googleAiService.generarContenido(aiRequest);
        String respuestaRaw = respuestaMono.block(Duration.ofSeconds(60));
        return extraerTextoGemini(respuestaRaw);
    }

    private Map<String, Object> construirPayloadAi(RangoFechas rango, int totalEncontradas, DatosAgregados agregados, List<Alerta> muestra) {
        Map<String, Object> aiData = new LinkedHashMap<>();
        aiData.put("rango", Map.of("inicio", rango.inicio().toString(), "fin", rango.fin().toString()));
        aiData.put("totales", Map.of("encontradas", totalEncontradas, "sectores", agregados.numSectores(),
                "mediaDiaria", agregados.mediaDiaria(), "medianaDiaria", agregados.medianaDiaria()));
        aiData.put(KEY_POR_TIPO, toPairs(agregados.porTipo()));
        aiData.put(KEY_POR_ESTADO, toPairs(agregados.porEstado()));
        aiData.put("porHora", toPairsIntLong(agregados.porHora()));
        aiData.put("porDiaSemana", toPairsStrLong(agregados.porDiaSemana()));
        aiData.put("topSectores", agregados.topSectores().stream()
                .map(e -> toPair(e.getKey(), e.getValue())).toList());

        Map<String, Object> tendencias = new LinkedHashMap<>();
        tendencias.put("diaria", agregados.porDia().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toPair(e.getKey().toString(), e.getValue()))
                .toList());
        aiData.put("tendencias", tendencias);

        Map<String, Object> anom = new LinkedHashMap<>();
        anom.put("diasPico", agregados.diasPico());
        anom.put("horasPico", agregados.horasPico());
        anom.put("sectoresZscore", agregados.sectoresZ());
        aiData.put("anomalias", anom);

        aiData.put("muestra", muestra.stream().limit(20).map(a -> Map.of(
                "fecha", a.getFechaHora() == null ? null : a.getFechaHora().toString(),
                "tipo", a.getTipo() == null ? null : a.getTipo().name(),
                KEY_ESTADO, a.getEstado() == null ? null : a.getEstado().name(),
                KEY_SECTOR, sectorVillaComuna(a),
                "detalle", Optional.ofNullable(a.getDescripcion()).orElse("")
        )).toList());

        return aiData;
    }

    private String construirPromptAi(Map<String, Object> aiData) throws JsonProcessingException {
        return "Eres analista de seguridad. Con base en el siguiente JSON agregado, entrega:\n" +
                "- Patrones por tipo, sector, hora y día.\n" +
                "- Tendencias y posibles causas.\n" +
                "- Sectores/horas con anomalías y recomendaciones accionables.\n" +
                "- Resumen ejecutivo (máx 8 viñetas) y 3 prioridades tácticas para la próxima semana.\n" +
                "Responde en minimo 350–400 palabras. El resultado dalo en formato markdown que se vea profesional, que sea facil de leer" +
                objectMapper.writeValueAsString(aiData);
    }

    private Map<String, Object> construirRequestAi(String prompt) {
        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
        )));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.9);
        generationConfig.put("maxOutputTokens", 2500);
        aiRequest.put("generationConfig", generationConfig);

        return aiRequest;
    }

    private Map<String, Object> construirRespuesta(FiltrosInforme filtros, RangoFechas rango, int totalEncontradas,
                                                    List<Alerta> muestra, DatosAgregados agregados, String informe) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put(KEY_STATUS, "success");
        resultado.put("filtros", filtrosMap(rango.inicio(), rango.fin(), filtros.tipoFiltro(),
                filtros.estadoFiltro(), filtros.sectorFiltro(), muestra.size()));
        resultado.put("totalEncontradas", totalEncontradas);
        resultado.put("totalUsadas", muestra.size());

        Map<String, Object> agregadosMap = new LinkedHashMap<>();
        agregadosMap.put(KEY_POR_TIPO, agregados.porTipo());
        agregadosMap.put(KEY_POR_ESTADO, agregados.porEstado());
        agregadosMap.put("porSectorTop10", agregados.topSectores());
        agregadosMap.put("porHora", agregados.porHora());
        agregadosMap.put("porDiaSemana", agregados.porDiaSemana());
        agregadosMap.put("mediaDiaria", agregados.mediaDiaria());
        agregadosMap.put("medianaDiaria", agregados.medianaDiaria());

        Map<String, Object> anom = new LinkedHashMap<>();
        anom.put("diasPico", agregados.diasPico());
        anom.put("horasPico", agregados.horasPico());
        anom.put("sectoresZscore", agregados.sectoresZ());
        agregadosMap.put("anomalias", anom);

        resultado.put("agregados", agregadosMap);
        resultado.put("informeAi", informe);
        resultado.put("modelo", "gemini-2.0-flash");
        resultado.put("modo", "agregado");
        resultado.put("muestra", muestra.stream().limit(10).map(AlertaResponseDto::new).toList());

        return resultado;
    }

    private record FiltrosInforme(String fechaInicioStr, String fechaFinStr, TipoAlertaEnum tipoFiltro,
                                  EstadoAlerta estadoFiltro, String sectorFiltro, int limiteSeguro) {}

    private record RangoFechas(LocalDateTime inicio, LocalDateTime fin) {}

    private record DatosAgregados(Map<TipoAlertaEnum, Long> porTipo, Map<EstadoAlerta, Long> porEstado,
                                  Map<String, Long> porSector, Map<Integer, Long> porHora,
                                  Map<LocalDate, Long> porDia, Map<String, Long> porDiaSemana,
                                  List<Map.Entry<String, Long>> topSectores, int numSectores,
                                  double mediaDiaria, double medianaDiaria,
                                  List<List<Object>> diasPico, List<List<Object>> horasPico,
                                  List<List<Object>> sectoresZ) {}

    private LocalDateTime[] parseFechasOrThrow(String fechaInicioStr, String fechaFinStr) {
        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicioStr);
            LocalDateTime fin = LocalDateTime.parse(fechaFinStr);
            return new LocalDateTime[]{inicio, fin};
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use ISO-8601, ej: 2025-11-29T13:45:00");
        }
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o).trim();
    }

    private TipoAlertaEnum parseTipo(String tipoStr) {
        if (tipoStr == null || tipoStr.isBlank()) return null;
        try {
            return TipoAlertaEnum.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private EstadoAlerta parseEstado(String estadoStr) {
        if (estadoStr == null || estadoStr.isBlank()) return null;
        try {
            return EstadoAlerta.valueOf(estadoStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String extraerTextoGemini(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return "No hubo respuesta del modelo.";
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode text = parts.get(0).path("text");
                    if (!text.isMissingNode()) return text.asText();
                }
            }
            return "No se pudo extraer el texto de la respuesta del modelo.";
        } catch (Exception e) {
            return "Error al parsear respuesta del modelo: " + e.getMessage();
        }
    }

    private Map<String, Object> filtrosMap(LocalDateTime inicio,
                                           LocalDateTime fin,
                                           TipoAlertaEnum tipo,
                                           EstadoAlerta estado,
                                           String sector,
                                           Integer limite) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fechaInicio", inicio);
        m.put("fechaFin", fin);
        m.put("tipo", tipo);
        m.put(KEY_ESTADO, estado);
        m.put(KEY_SECTOR, sector);
        m.put(KEY_LIMITE, limite);
        return m;
    }


    // Helper: compone "Sector (Villa X, Comuna Y)" con valores de respaldo si faltan
    private String sectorVillaComuna(Alerta a) {
        String sector = Optional.ofNullable(a.getSector()).filter(s -> !s.isBlank()).orElse("(sin sector)");
        String villa = null;
        String comuna = Optional.ofNullable(a.getComuna()).filter(s -> !s.isBlank()).orElse(null);

        try {
            if (a.getUsuario() != null) {
                // Nombre de la villa desde la relación del usuario si existe
                villa = Optional.ofNullable(a.getUsuario().getVillaNombre()).filter(s -> !s.isBlank()).orElse(null);
                if (comuna == null) {
                    comuna = Optional.ofNullable(a.getUsuario().getComunaNombre()).filter(s -> !s.isBlank()).orElse(null);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al obtener datos de villa/comuna para alerta {}: {}", 
                    a.getId(), e.getMessage());
        }

        StringBuilder sb = new StringBuilder(sector);
        List<String> detalles = new ArrayList<>();
        if (villa != null) detalles.add("Villa " + villa);
        if (comuna != null) detalles.add("Comuna " + comuna);
        if (!detalles.isEmpty()) {
            sb.append(" (").append(String.join(", ", detalles)).append(")");
        }
        return sb.toString();
    }

    // ==== Utilidades de agregación compacta ====
    private String diaCorto(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "L";
            case TUESDAY: return "M";
            case WEDNESDAY: return "X";
            case THURSDAY: return "J";
            case FRIDAY: return "V";
            case SATURDAY: return "S";
            case SUNDAY: return "D";
            default: return dow.name();
        }
    }

    private double calcularMediana(List<Long> valores) {
        if (valores == null || valores.isEmpty()) return 0.0;
        List<Long> copia = new ArrayList<>(valores);
        copia.sort(Comparator.naturalOrder());
        int n = copia.size();
        if (n % 2 == 1) {
            return copia.get(n / 2);
        } else {
            return (copia.get(n / 2 - 1) + copia.get(n / 2)) / 2.0;
        }
    }

    private List<List<Object>> calcularDiasPico(Map<LocalDate, Long> porDia) {
        if (porDia == null || porDia.isEmpty()) return Collections.emptyList();
        List<Long> valores = new ArrayList<>(porDia.values());
        double media = valores.stream().mapToLong(v -> v).average().orElse(0.0);
        double varMedia = valores.stream().mapToDouble(v -> Math.pow(v - media, 2)).average().orElse(0.0);
        double sd = Math.sqrt(varMedia);
        double umbral = media + 2 * sd;
        return porDia.entrySet().stream()
                .filter(e -> e.getValue() > umbral)
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toPair(e.getKey().toString(), e.getValue()))
                .toList();
    }

    private List<List<Object>> calcularZscore(Map<String, Long> porSector) {
        if (porSector == null || porSector.isEmpty()) return Collections.emptyList();
        List<Long> valores = new ArrayList<>(porSector.values());
        double media = valores.stream().mapToLong(v -> v).average().orElse(0.0);
        double varia = valores.stream().mapToDouble(v -> Math.pow(v - media, 2)).average().orElse(0.0);
        double sd = Math.sqrt(varia);
        final double sdSafe = (sd == 0.0) ? 1.0 : sd; // evitar división por cero cuando todos son iguales
        List<List<Object>> lista = porSector.entrySet().stream()
                .map(e -> toPair(e.getKey(), round2((e.getValue() - media) / sdSafe)))
                .toList();
        lista.sort((a, b) -> Double.compare(((Number) b.get(1)).doubleValue(), ((Number) a.get(1)).doubleValue()));
        if (lista.size() > 10) {
            return new ArrayList<>(lista.subList(0, 10));
        }
        return lista;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private <K> List<List<Object>> toPairs(Map<K, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, Long>comparingByValue().reversed())
                .map(e -> toPair(String.valueOf(e.getKey()), e.getValue()))
                .toList();
    }

    private List<List<Object>> toPairsIntLong(Map<Integer, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByKey())
                .map(e -> toPair(e.getKey(), e.getValue()))
                .toList();
    }

    private List<List<Object>> toPairsStrLong(Map<String, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByKey())
                .map(e -> toPair(e.getKey(), e.getValue()))
                .toList();
    }

    private List<Object> toPair(Object a, Object b) {
        List<Object> list = new ArrayList<>(2);
        list.add(a);
        list.add(b);
        return list;
    }
}
