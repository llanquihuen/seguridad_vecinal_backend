package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
import cl.seguridad.vecinal.modelo.dto.AlertaResponseDto;
import cl.seguridad.vecinal.service.AlertaService;
import cl.seguridad.vecinal.service.GoogleAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://seguridadvecinalchile.cl"})
public class AlertaInformeController {

    private final AlertaService alertaService;
    private final GoogleAiService googleAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AlertaInformeController(AlertaService alertaService, GoogleAiService googleAiService) {
        this.alertaService = alertaService;
        this.googleAiService = googleAiService;
    }

    @PostMapping("/informe")
    public ResponseEntity<?> generarInforme(@RequestBody Map<String, Object> body) {
        try {
            // 1) Leer filtros
            String fechaInicioStr = asString(body.get("fechaInicio"));
            String fechaFinStr = asString(body.get("fechaFin"));
            String tipoStr = asString(body.get("tipo"));
            String estadoStr = asString(body.get("estado"));
            String sector = asString(body.get("sector"));
            Integer limite = body.get("limite") instanceof Number ? ((Number) body.get("limite")).intValue() : 100;
            Boolean incluirDetalle = body.get("incluirDetalle") instanceof Boolean ? (Boolean) body.get("incluirDetalle") : Boolean.TRUE;

            LocalDateTime inicio = null;
            LocalDateTime fin = null;
            if (fechaInicioStr != null && fechaFinStr != null) {
                try {
                    inicio = LocalDateTime.parse(fechaInicioStr);
                    fin = LocalDateTime.parse(fechaFinStr);
                } catch (DateTimeParseException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of(
                                    "status", "error",
                                    "message", "Formato de fecha inválido. Use ISO-8601, ej: 2025-11-29T13:45:00"
                            ));
                }
            }

            TipoAlertaEnum tipoFiltro = parseTipo(tipoStr);
            EstadoAlerta estadoFiltro = parseEstado(estadoStr);

            // 2) Obtener alertas base: por defecto últimos 2 meses
            List<Alerta> base;
            if (inicio != null && fin != null) {
                base = alertaService.obtenerAlertasPorRangoFechas(inicio, fin);
            } else {
                base = alertaService.obtenerAlertasDosMeses();
                // Además, fija el rango para el reporte
                inicio = LocalDateTime.now().minusDays(60);
                fin = LocalDateTime.now();
            }

            // 3) Aplicar filtros en memoria sobre TODAS las candidatas (sin límite aún)
            int limiteSeguro = (limite == null) ? 100 : Math.max(1, Math.min(limite, 500));
            final String sectorFiltro = sector == null ? null : sector.trim().toLowerCase();

            List<Alerta> candidatas = base.stream()
                    .filter(a -> tipoFiltro == null || a.getTipo() == tipoFiltro)
                    .filter(a -> estadoFiltro == null || a.getEstado() == estadoFiltro)
                    .filter(a -> {
                        if (sectorFiltro == null || sectorFiltro.isBlank()) return true;
                        String as = Optional.ofNullable(a.getSector()).orElse("").toLowerCase();
                        return as.contains(sectorFiltro);
                    })
                    .sorted(Comparator.comparing(Alerta::getFechaHora).reversed())
                    .collect(Collectors.toList());

            int totalEncontradas = candidatas.size();

            // 4) Agregados sobre población completa
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

            // Top sectores
            List<Map.Entry<String, Long>> topSectores = porSector.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            // Métricas básicas
            int numSectores = porSector.size();
            double mediaDiaria = porDia.values().stream().mapToLong(v -> v).average().orElse(0.0);
            double medianaDiaria = calcularMediana(new ArrayList<>(porDia.values()));

            // Anomalías sencillas
            List<List<Object>> diasPico = calcularDiasPico(porDia);
            List<List<Object>> horasPico = porHora.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> toPair(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            List<List<Object>> sectoresZ = calcularZscore(porSector);

            // 5) Muestra estratificada/representativa
            int limiteMuestraHeur = Math.min(200, Math.max(50, (int) Math.sqrt(Math.max(totalEncontradas, 1)) * 5));
            int limiteMuestra = Math.min(limiteSeguro, limiteMuestraHeur);
            Set<String> sectoresTopSet = topSectores.stream().map(Map.Entry::getKey).collect(Collectors.toSet());

            List<Alerta> muestraTopSectores = candidatas.stream()
                    .filter(a -> sectoresTopSet.contains(sectorVillaComuna(a)))
                    .limit(limiteMuestra)
                    .collect(Collectors.toList());

            // Asegurar inclusión de top-5 alertas recientes para los top-3 sectores
            Set<String> top3 = topSectores.stream().limit(3).map(Map.Entry::getKey).collect(Collectors.toCollection(LinkedHashSet::new));
            List<Alerta> recientesTop3 = candidatas.stream()
                    .filter(a -> top3.contains(sectorVillaComuna(a)))
                    .limit(15)
                    .collect(Collectors.toList());

            LinkedHashSet<Alerta> muestraSet = new LinkedHashSet<>();
            muestraSet.addAll(muestraTopSectores);
            muestraSet.addAll(recientesTop3);
            List<Alerta> muestra = new ArrayList<>(muestraSet).subList(0, Math.min(muestraSet.size(), limiteMuestra));

            // 6) Construir payload compacto para IA
            Map<String, Object> aiData = new LinkedHashMap<>();
            aiData.put("rango", Map.of("inicio", inicio.toString(), "fin", fin.toString()));
            aiData.put("totales", Map.of("encontradas", totalEncontradas, "sectores", numSectores,
                    "mediaDiaria", mediaDiaria, "medianaDiaria", medianaDiaria));
            aiData.put("porTipo", toPairs(porTipo));
            aiData.put("porEstado", toPairs(porEstado));
            aiData.put("porHora", toPairsIntLong(porHora));
            aiData.put("porDiaSemana", toPairsStrLong(porDiaSemana));
            aiData.put("topSectores", topSectores.stream().map(e -> toPair(e.getKey(), e.getValue())).collect(Collectors.toList()));
            Map<String, Object> tendencias = new LinkedHashMap<>();
            tendencias.put("diaria", porDia.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> toPair(e.getKey().toString(), e.getValue()))
                    .collect(Collectors.toList()));
            aiData.put("tendencias", tendencias);
            Map<String, Object> anom = new LinkedHashMap<>();
            anom.put("diasPico", diasPico);
            anom.put("horasPico", horasPico);
            anom.put("sectoresZscore", sectoresZ);
            aiData.put("anomalias", anom);
            aiData.put("muestra", muestra.stream().limit(20).map(a -> Map.of(
                    "fecha", a.getFechaHora() == null ? null : a.getFechaHora().toString(),
                    "tipo", a.getTipo() == null ? null : a.getTipo().name(),
                    "estado", a.getEstado() == null ? null : a.getEstado().name(),
                    "sector", sectorVillaComuna(a),
                    "detalle", Optional.ofNullable(a.getDescripcion()).orElse("")
            )).collect(Collectors.toList()));

            String prompt = "Eres analista de seguridad. Con base en el siguiente JSON agregado, entrega:\n" +
                    "- Patrones por tipo, sector, hora y día.\n" +
                    "- Tendencias y posibles causas.\n" +
                    "- Sectores/horas con anomalías y recomendaciones accionables.\n" +
                    "- Resumen ejecutivo (máx 8 viñetas) y 3 prioridades tácticas para la próxima semana.\n" +
                    "Responde en minimo 350–400 palabras. Si falta información, indícalo. Si necesitas mas palabras solo agrega mas, no cortes las frases\n\n" +
                    objectMapper.writeValueAsString(aiData);

            // 7) Llamar a Gemini con configuración ajustada
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("contents", List.of(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", prompt))
            )));
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.2);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.9);
            generationConfig.put("maxOutputTokens", 600);
            aiRequest.put("generationConfig", generationConfig);

            Mono<String> respuestaMono = googleAiService.generarContenido(aiRequest);
            String respuestaRaw = respuestaMono.block(Duration.ofSeconds(60));
            String informe = extraerTextoGemini(respuestaRaw);

            // 8) Respuesta
            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("status", "success");
            resultado.put("filtros", filtrosMap(inicio, fin, tipoFiltro, estadoFiltro, sector, limiteMuestra));
            resultado.put("totalEncontradas", totalEncontradas);
            resultado.put("totalUsadas", muestra.size());
            Map<String, Object> agregados = new LinkedHashMap<>();
            agregados.put("porTipo", porTipo);
            agregados.put("porEstado", porEstado);
            agregados.put("porSectorTop10", topSectores);
            agregados.put("porHora", porHora);
            agregados.put("porDiaSemana", porDiaSemana);
            agregados.put("mediaDiaria", mediaDiaria);
            agregados.put("medianaDiaria", medianaDiaria);
            agregados.put("anomalias", anom);
            resultado.put("agregados", agregados);
            resultado.put("informeAi", informe);
            resultado.put("modelo", "gemini-2.0-flash");
            resultado.put("modo", "agregado");
            resultado.put("muestra", muestra.stream().limit(10).map(AlertaResponseDto::new).collect(Collectors.toList()));

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "No se pudo generar el informe: " + e.getMessage()
                    ));
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

    private String construirPromptPromedio(List<Alerta> alertas,
                                           LocalDateTime inicio,
                                           LocalDateTime fin,
                                           TipoAlertaEnum tipo,
                                           EstadoAlerta estado,
                                           String sector,
                                           boolean incluirDetalle) {
        long total = alertas.size();
        long activas = alertas.stream().filter(a -> a.getEstado() == EstadoAlerta.ACTIVA).count();
        long enProceso = alertas.stream().filter(a -> a.getEstado() == EstadoAlerta.EN_PROCESO).count();
        long atendidas = alertas.stream().filter(a -> a.getEstado() == EstadoAlerta.ATENDIDA).count();

        Map<TipoAlertaEnum, Long> porTipo = alertas.stream()
                .collect(Collectors.groupingBy(Alerta::getTipo, Collectors.counting()));

        // Top sectores (incluyendo Villa y Comuna para que IA tenga contexto)
        List<Map.Entry<String, Long>> topSectores = alertas.stream()
                .collect(Collectors.groupingBy(a -> sectorVillaComuna(a), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();

        // Picos horarios (0-23)
        List<Map.Entry<Integer, Long>> picosHorarios = alertas.stream()
                .collect(Collectors.groupingBy(a -> a.getFechaHora() != null ? a.getFechaHora().getHour() : -1, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getKey() >= 0)
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(4)
                .toList();

        String sectoresTxt = topSectores.stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("; "));
        String horasTxt = picosHorarios.stream()
                .map(e -> String.format("%02d:00=%d", e.getKey(), e.getValue()))
                .collect(Collectors.joining("; "));

        StringBuilder sb = new StringBuilder();
        sb.append("Genera un informe en español de extensión media (alrededor de 1000-1300 caracteres). ")
          .append("Debes ser preciso, sin relleno, y centrado en hotspots (sectores con más incidentes,NOTA: Al mencionar el sector se debe indicar la villa del sector y la comuna a la que pertenecen obligatoriamente) y horarios pico. ")
          .append("Finaliza con 3–5 recomendaciones de vigilancia focalizada. No inventes datos.\n\n");

        sb.append("Periodo: ").append(inicio).append(" a ").append(fin).append(". ");
        if (tipo != null) sb.append("Tipo: ").append(tipo).append(". ");
        if (estado != null) sb.append("Estado: ").append(estado).append(". ");
        if (sector != null && !sector.isBlank()) sb.append("Sector filtro: ").append(sector).append(". ");

        sb.append("\nTotales: ").append(total)
          .append(" (Activas=").append(activas)
          .append(", En proceso=").append(enProceso)
          .append(", Atendidas=").append(atendidas).append(").\n");

        if (!porTipo.isEmpty()) {
            sb.append("Por tipo: ");
            porTipo.entrySet().stream()
                    .sorted(Map.Entry.<TipoAlertaEnum, Long>comparingByValue().reversed())
                    .forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()).append("; "));
            sb.append("\n");
        }

        if (!topSectores.isEmpty()) sb.append("Hotspots (top sectores): ").append(sectoresTxt).append("\n");
        if (!picosHorarios.isEmpty()) sb.append("Horarios pico: ").append(horasTxt).append("\n");

        if (incluirDetalle && !alertas.isEmpty()) {
            sb.append("Ejemplos breves: ");
            String ejemplos = alertas.stream().limit(2).map(a ->
                    "[" + a.getFechaHora() + "] " + a.getTipo() + " en " +
                            sectorVillaComuna(a)
            ).collect(Collectors.joining("; "));
            sb.append(ejemplos).append("\n");
        }

        sb.append("Redacta el análisis resaltando sectores que requieren mayor vigilancia y justificando con datos. ")
          .append("Concluye con una lista corta de recomendaciones operativas (patrullajes por horario, puntos de control, coordinación). ");

        return sb.toString();
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
        m.put("estado", estado);
        m.put("sector", sector);
        m.put("limite", limite);
        return m;
    }

    private Map<String, Object> resumenBasico(List<Alerta> alertas) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("porEstado", alertas.stream().collect(Collectors.groupingBy(Alerta::getEstado, Collectors.counting())));
        r.put("porTipo", alertas.stream().collect(Collectors.groupingBy(Alerta::getTipo, Collectors.counting())));
        // Agrupar por sector enriquecido con Villa y Comuna para coherencia con el prompt
        r.put("porSectorTop5", alertas.stream()
                .collect(Collectors.groupingBy(this::sectorVillaComuna, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList()));
        return r;
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
        } catch (Exception ignored) { }

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
        double var = valores.stream().mapToDouble(v -> Math.pow(v - media, 2)).average().orElse(0.0);
        double sd = Math.sqrt(var);
        double umbral = media + 2 * sd;
        return porDia.entrySet().stream()
                .filter(e -> e.getValue() > umbral)
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toPair(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<List<Object>> calcularZscore(Map<String, Long> porSector) {
        if (porSector == null || porSector.isEmpty()) return Collections.emptyList();
        List<Long> valores = new ArrayList<>(porSector.values());
        double media = valores.stream().mapToLong(v -> v).average().orElse(0.0);
        double var = valores.stream().mapToDouble(v -> Math.pow(v - media, 2)).average().orElse(0.0);
        double sd = Math.sqrt(var);
        final double sdSafe = (sd == 0.0) ? 1.0 : sd; // evitar división por cero cuando todos son iguales
        List<List<Object>> lista = porSector.entrySet().stream()
                .map(e -> toPair(e.getKey(), round2((e.getValue() - media) / sdSafe)))
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    private List<List<Object>> toPairsIntLong(Map<Integer, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByKey())
                .map(e -> toPair(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<List<Object>> toPairsStrLong(Map<String, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByKey())
                .map(e -> toPair(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<Object> toPair(Object a, Object b) {
        List<Object> list = new ArrayList<>(2);
        list.add(a);
        list.add(b);
        return list;
    }
}
