// src/main/java/cl/seguridad/vecinal/service/AlertaService.java
package cl.seguridad.vecinal.service;

import cl.seguridad.vecinal.dao.AlertaRepository;
import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.dto.AlertaCreateRequest;
import cl.seguridad.vecinal.modelo.dto.DashboardStatsDto;
import cl.seguridad.vecinal.modelo.Role;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public AlertaService(AlertaRepository alertaRepository, UsuarioRepository usuarioRepository) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
    }


    // Crear nueva alerta
    public Alerta crearAlerta(AlertaCreateRequest request) {
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Alerta alerta = new Alerta();
        alerta.setUsuario(usuario);
        alerta.setTipo(request.getTipo());

        // ✅ Título: usar el enviado o generar automático del tipo
        String titulo = request.getTitulo() != null && !request.getTitulo().isBlank() ?
                request.getTitulo() :
                request.getTipo().getTitulo();
        alerta.setTitulo(titulo);

        // ✅ Descripción: usar la enviada o generar automática del tipo
        String descripcion = request.getDescripcion() != null && !request.getDescripcion().isBlank() ?
                request.getDescripcion() :
                request.getTipo().getDescripcion();
        alerta.setDescripcion(descripcion);

        alerta.setLatitud(request.getLatitud());
        alerta.setLongitud(request.getLongitud());
        alerta.setDireccion(request.getDireccion());
        alerta.setSector(usuario.getSector());          // ← Del usuario en BD
        alerta.setComuna(usuario.getComunaNombre());    // ← "San Bernardo"
        alerta.setCiudad(usuario.getCiudadNombre());    // ← "Santiago"
        alerta.setSilenciosa(Boolean.TRUE.equals(request.getSilenciosa()));
        alerta.setEstado(EstadoAlerta.ACTIVA);
        alerta.setFechaHora(LocalDateTime.now());

        return alertaRepository.save(alerta);
    }

    // Obtener todas las alertas con paginación
    public Page<Alerta> obtenerAlertasPaginadas(Pageable pageable) {
        return alertaRepository.findAll(pageable);
    }

    // Obtener alerta por ID
    public Optional<Alerta> obtenerAlertaPorId(Integer id) {
        return alertaRepository.findById(id);
    }

    // Obtener alertas activas
    public List<Alerta> obtenerAlertasActivas() {
        return alertaRepository.findAlertasActivas();
    }

    // Obtener alertas recientes (últimas 24 horas)
    public List<Alerta> obtenerAlertasRecientes() {
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
        return alertaRepository.findAlertasRecientes(hace24Horas);
    }
    // Obtener alertas ultimo Mes
    public List<Alerta> obtenerAlertasMes() {
        LocalDateTime ultimoMes = LocalDateTime.now().minusDays(30);
        return alertaRepository.findAlertasRecientes(ultimoMes);
    }

    // Obtener alertas últimos 2 meses (≈60 días)
    public List<Alerta> obtenerAlertasDosMeses() {
        LocalDateTime dosMeses = LocalDateTime.now().minusDays(60);
        return alertaRepository.findAlertasRecientes(dosMeses);
    }

    // Obtener alertas por usuario
    public Page<Alerta> obtenerAlertasPorUsuario(Integer usuarioId, Pageable pageable) {
        return alertaRepository.findByUsuario_UsuarioId(usuarioId, pageable);
    }

    // Obtener alertas por estado
    public Page<Alerta> obtenerAlertasPorEstado(EstadoAlerta estado, Pageable pageable) {
        return alertaRepository.findByEstado(estado, pageable);
    }

    // Obtener alertas por tipo
    public List<Alerta> obtenerAlertasPorTipo(TipoAlertaEnum tipo) {
        return alertaRepository.findByTipo(tipo);
    }

    // Obtener alertas cercanas a una ubicación
    public List<Alerta> obtenerAlertasCercanas(Double latitud, Double longitud, Double radioKm) {
        return alertaRepository.findByUbicacion(latitud, longitud, radioKm);
    }

    // Cambiar estado de alerta
    public Alerta cambiarEstadoAlerta(Integer alertaId, EstadoAlerta nuevoEstado, Integer adminId, String notas) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));

        alerta.setEstado(nuevoEstado);

        if (EstadoAlerta.ATENDIDA.equals(nuevoEstado)) {
            alerta.setAtendidaPor(adminId);
            alerta.setFechaAtencion(LocalDateTime.now());
            if (notas != null) {
                alerta.setNotasAtencion(notas);
            }
        }

        return alertaRepository.save(alerta);
    }

    // Obtener estadísticas de alertas
    public AlertaStats obtenerEstadisticas() {
        Long total = alertaRepository.count();
        Long activas = alertaRepository.countByEstado(EstadoAlerta.ACTIVA);
        Long enProceso = alertaRepository.countByEstado(EstadoAlerta.EN_PROCESO);
        Long resueltas = alertaRepository.countByEstado(EstadoAlerta.ATENDIDA);
        Long hoy = alertaRepository.countAlertasHoy();

        return new AlertaStats(total, activas, enProceso, resueltas, hoy);
    }

    // Obtener alertas por rango de fechas
    public List<Alerta> obtenerAlertasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return alertaRepository.findByFechaHoraBetween(inicio, fin);
    }

    // Eliminar alerta (soft delete o real delete según necesites)
    public void eliminarAlerta(Integer alertaId) {
        if (!alertaRepository.existsById(alertaId)) {
            throw new EntityNotFoundException("Alerta no encontrada");
        }
        alertaRepository.deleteById(alertaId);
    }

    // Clase interna para estadísticas
    public static class AlertaStats {
        public final Long total;
        public final Long activas;
        public final Long enProceso;
        public final Long resueltas;
        public final Long hoy;

        public AlertaStats(Long total, Long activas, Long enProceso, Long resueltas, Long hoy) {
            this.total = total;
            this.activas = activas;
            this.enProceso = enProceso;
            this.resueltas = resueltas;
            this.hoy = hoy;
        }
    }

// ========== MÉTODOS PARA ESTADÍSTICAS DEL DASHBOARD ==========

    /**
     * Contar alertas por estado
     * @param estado Estado de la alerta (ACTIVA, EN_PROCESO, ATENDIDA)
     * @return Cantidad de alertas en ese estado
     */
    public long countAlertasByEstado(EstadoAlerta estado) {
        return alertaRepository.countByEstado(estado);
    }

    /**
     * Contar alertas creadas hoy
     * @return Cantidad de alertas de hoy
     */
    public long countAlertasHoy() {
        return alertaRepository.countAlertasHoy();
    }

    /**
     * Obtener las últimas 5 alertas para mostrar en actividad reciente
     * @return Lista de las 5 alertas más recientes
     */
    public List<Alerta> findTop5RecentAlertas() {
        return alertaRepository.findTop5ByOrderByFechaHoraDesc();
    }

    // ===================== DASHBOARD STATS (EXTRAÍDO DEL CONTROLADOR) =====================
    public DashboardStatsDto obtenerDashboardStats(Long villaId,
                                                   String sector,
                                                   String fechaInicio,
                                                   String fechaFin,
                                                   String emailUsuario) {
        // Obtener usuario y rol
        Usuario currentUser = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Determinar villa objetivo
        Long targetVillaId = null;
        if (currentUser.getRole() == Role.ADMIN_VILLA) {
            targetVillaId = currentUser.getVillaId();
        } else if (currentUser.getRole() == Role.SUPER_ADMIN) {
            targetVillaId = villaId;
        }

        // Fechas por defecto
        LocalDateTime startDate = fechaInicio != null ? LocalDateTime.parse(fechaInicio + "T00:00:00") : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = fechaFin != null ? LocalDateTime.parse(fechaFin + "T23:59:59") : LocalDateTime.now();

        // Obtener alertas filtradas
        List<cl.seguridad.vecinal.modelo.Alerta> alertas;
        if (targetVillaId != null && sector != null) {
            alertas = alertaRepository.findByVillaIdAndSectorAndFechaHoraBetween(targetVillaId, sector, startDate, endDate);
        } else if (targetVillaId != null) {
            alertas = alertaRepository.findByVillaIdAndFechaHoraBetween(targetVillaId, startDate, endDate);
        } else if (sector != null) {
            alertas = alertaRepository.findBySectorAndFechaHoraBetween(sector, startDate, endDate);
        } else {
            alertas = alertaRepository.findByFechaHoraBetween(startDate, endDate);
        }

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalAlertas(alertas.size());

        // Por tipo
        Map<String, Long> porTipo = alertas.stream()
                .collect(Collectors.groupingBy(a -> a.getTipo().name(), Collectors.counting()));
        dto.setAlertasPorTipo(porTipo);

        // Por estado
        Map<String, Long> porEstado = alertas.stream()
                .collect(Collectors.groupingBy(a -> a.getEstado().name(), Collectors.counting()));
        dto.setAlertasPorEstado(porEstado);

        // Por día (últimos 7 días)
        Map<String, Long> porDia = alertas.stream()
                .filter(a -> a.getFechaHora() != null && a.getFechaHora().isAfter(LocalDateTime.now().minusDays(7)))
                .collect(Collectors.groupingBy(a -> a.getFechaHora().toLocalDate().toString(), Collectors.counting()));
        dto.setAlertasPorDia(porDia);

        // Top sectores
        Map<String, Long> porSector = alertas.stream()
                .filter(a -> a.getSector() != null)
                .collect(Collectors.groupingBy(cl.seguridad.vecinal.modelo.Alerta::getSector, Collectors.counting()));
        List<Map<String, Object>> topSectores = porSector.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("sector", entry.getKey());
                    item.put("cantidad", entry.getValue());
                    return item;
                })
                .toList();
        dto.setTopSectores(topSectores);

        // Por hora
        Map<Integer, Long> porHora = alertas.stream()
                .filter(a -> a.getFechaHora() != null)
                .collect(Collectors.groupingBy(a -> a.getFechaHora().getHour(), Collectors.counting()));
        dto.setAlertasPorHora(porHora);

        // Porcentaje silenciosas
        long silenciosas = alertas.stream().filter(Alerta::getSilenciosa).count();
        double porcentajeSilenciosas = alertas.isEmpty() ? 0 : (silenciosas * 100.0) / alertas.size();
        dto.setPorcentajeSilenciosas(Math.round(porcentajeSilenciosas * 100.0) / 100.0);

        return dto;
    }

}