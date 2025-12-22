// src/main/java/cl/seguridad/vecinal/controller/AlertaController.java
package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.dao.AlertaRepository;
import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.*;
import cl.seguridad.vecinal.modelo.dto.AlertaCreateRequest;
import cl.seguridad.vecinal.modelo.dto.CambioEstadoRequest;
import cl.seguridad.vecinal.modelo.dto.DashboardStatsDto;
import cl.seguridad.vecinal.modelo.dto.AlertaResponseDto;
import cl.seguridad.vecinal.modelo.mapper.AlertaMapper;
import cl.seguridad.vecinal.service.AlertaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    // Constants to avoid literal duplication
    private static final String FIELD_FECHA_HORA = "fechaHora";
    private static final String KEY_ALERTAS = "alertas";
    private static final String KEY_ALERTA = "alerta";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_TOTAL = "total";
    private static final String KEY_TOTAL_ELEMENTS = "totalElements";
    private static final String KEY_TOTAL_PAGES = "totalPages";
    private static final String KEY_CURRENT_PAGE = "currentPage";
    private static final String VALUE_SUCCESS = "success";

    private final AlertaService alertaService;
    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlertaMapper alertaMapper;

    // Constructor injection for dependencies
    public AlertaController(AlertaService alertaService,
                            AlertaRepository alertaRepository,
                            UsuarioRepository usuarioRepository,
                            AlertaMapper alertaMapper) {
        this.alertaService = alertaService;
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.alertaMapper = alertaMapper;
    }

    // ========== CREAR ALERTA ==========
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearAlerta(@Valid @RequestBody AlertaCreateRequest request) {
        Alerta nuevaAlerta = alertaService.crearAlerta(request);
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTA, alertaMapper.toDto(nuevaAlerta));
        response.put(KEY_STATUS, VALUE_SUCCESS);
        response.put(KEY_MESSAGE, "Alerta creada exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== OBTENER TODAS LAS ALERTAS (CON PAGINACIÓN) ==========
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerAlertasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = FIELD_FECHA_HORA) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Alerta> alertaPage = alertaService.obtenerAlertasPaginadas(pageable);
        List<AlertaResponseDto> alertas = alertaMapper.toDtoList(alertaPage.getContent());
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTAS, alertas);
        response.put(KEY_CURRENT_PAGE, alertaPage.getNumber());
        response.put(KEY_TOTAL_PAGES, alertaPage.getTotalPages());
        response.put(KEY_TOTAL_ELEMENTS, alertaPage.getTotalElements());
        response.put("size", alertaPage.getSize());
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== OBTENER ALERTA POR ID ==========
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerAlertaPorId(@PathVariable Integer id) {
        Optional<Alerta> alerta = alertaService.obtenerAlertaPorId(id);
        Alerta entity = alerta.orElseThrow(() -> new EntityNotFoundException("Alerta no encontrada"));
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTA, alertaMapper.toDto(entity));
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== OBTENER ALERTAS ACTIVAS ==========
    @GetMapping("/activas")
    public ResponseEntity<Map<String, Object>> obtenerAlertasActivas() {
        List<Alerta> alertas = alertaService.obtenerAlertasActivas();
        List<AlertaResponseDto> alertasDto = alertaMapper.toDtoList(alertas);
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTAS, alertasDto);
        response.put(KEY_TOTAL, alertasDto.size());
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== OBTENER ALERTAS RECIENTES (ÚLTIMAS 24 HORAS) ==========
    @GetMapping("/recientes")
    public ResponseEntity<Map<String, Object>> obtenerAlertasRecientes() {
        List<Alerta> alertas = alertaService.obtenerAlertasRecientes();
        List<AlertaResponseDto> alertasDto = alertaMapper.toDtoList(alertas);
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTAS, alertasDto);
        response.put(KEY_TOTAL, alertasDto.size());
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== OBTENER ALERTAS POR USUARIO ==========
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> obtenerAlertasPorUsuario(
            @PathVariable Integer usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_FECHA_HORA).descending());
        Page<Alerta> alertaPage = alertaService.obtenerAlertasPorUsuario(usuarioId, pageable);
        List<AlertaResponseDto> alertas = alertaMapper.toDtoList(alertaPage.getContent());
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTAS, alertas);
        response.put(KEY_CURRENT_PAGE, alertaPage.getNumber());
        response.put(KEY_TOTAL_PAGES, alertaPage.getTotalPages());
        response.put(KEY_TOTAL_ELEMENTS, alertaPage.getTotalElements());
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== OBTENER ALERTAS POR ESTADO ==========
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> obtenerAlertasPorEstado(
            @PathVariable String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            EstadoAlerta estadoAlerta = EstadoAlerta.valueOf(estado.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_FECHA_HORA).descending());
            Page<Alerta> alertaPage = alertaService.obtenerAlertasPorEstado(estadoAlerta, pageable);

            List<AlertaResponseDto> alertas = alertaPage.getContent().stream()
                    .map(AlertaResponseDto::new)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put(KEY_ALERTAS, alertas);
            response.put(KEY_CURRENT_PAGE, alertaPage.getNumber());
            response.put(KEY_TOTAL_PAGES, alertaPage.getTotalPages());
            response.put(KEY_TOTAL_ELEMENTS, alertaPage.getTotalElements());
            response.put(KEY_STATUS, VALUE_SUCCESS);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put(KEY_STATUS, "error");
            error.put(KEY_MESSAGE, "Estado inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put(KEY_STATUS, "error");
            error.put(KEY_MESSAGE, "Error al obtener alertas por estado: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER ALERTAS CERCANAS ==========
    @GetMapping("/cercanas")
    public ResponseEntity<Map<String, Object>> obtenerAlertasCercanas(
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam(defaultValue = "5.0") Double radio) {

        List<Alerta> alertas = alertaService.obtenerAlertasCercanas(latitud, longitud, radio);
        List<AlertaResponseDto> alertasDto = alertaMapper.toDtoList(alertas);
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTAS, alertasDto);
        response.put(KEY_TOTAL, alertasDto.size());
        response.put("radio", radio + " km");
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== CAMBIAR ESTADO DE ALERTA ==========
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstadoAlerta(
            @PathVariable Integer id,
            @RequestBody CambioEstadoRequest request) {

        EstadoAlerta nuevoEstado = EstadoAlerta.valueOf(request.getEstado().toUpperCase());
        Alerta alerta = alertaService.cambiarEstadoAlerta(id, nuevoEstado, request.getAdminId(), request.getNotas());
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTA, alertaMapper.toDto(alerta));
        response.put(KEY_STATUS, VALUE_SUCCESS);
        response.put(KEY_MESSAGE, "Estado de alerta actualizado");
        return ResponseEntity.ok(response);
    }

    // ========== OBTENER ESTADÍSTICAS ==========
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        AlertaService.AlertaStats stats = alertaService.obtenerEstadisticas();
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_TOTAL, stats.total);
        response.put("activas", stats.activas);
        response.put("enProceso", stats.enProceso);
        response.put("resueltas", stats.resueltas);
        response.put("hoy", stats.hoy);
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== ELIMINAR ALERTA ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarAlerta(@PathVariable Integer id) {
        alertaService.eliminarAlerta(id);
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_STATUS, VALUE_SUCCESS);
        response.put(KEY_MESSAGE, "Alerta eliminada exitosamente");
        return ResponseEntity.ok(response);
    }

    // ========== OBTENER TIPOS DE ALERTA DISPONIBLES ==========
    @GetMapping("/tipos")
    public ResponseEntity<Map<String, Object>> obtenerTiposAlerta() {
        List<Map<String, String>> tipos = TipoAlertaEnum.asListOfMaps();
        Map<String, Object> response = new HashMap<>();
        response.put("tipos", tipos);
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // OBTENER ALERTAS POR SECTOR
    @GetMapping("/sector/{sector}")
    public ResponseEntity<Map<String, Object>> obtenerAlertasPorSector(
            @PathVariable String sector,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Sort sort = Sort.by(FIELD_FECHA_HORA).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Alerta> alertaPage = alertaRepository.findBySector(sector, pageable);
        List<AlertaResponseDto> alertas = alertaMapper.toDtoList(alertaPage.getContent());
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTAS, alertas);
        response.put(KEY_CURRENT_PAGE, alertaPage.getNumber());
        response.put(KEY_TOTAL_PAGES, alertaPage.getTotalPages());
        response.put(KEY_TOTAL_ELEMENTS, alertaPage.getTotalElements());
        response.put("sector", sector);
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }

    // ========== ESTADÍSTICAS DE ALERTAS (PARA DASHBOARD) ==========

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getAlertasStats(
            @RequestParam(required = false) Long villaId,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        DashboardStatsDto dto = alertaService.obtenerDashboardStats(villaId, sector, fechaInicio, fechaFin, email);
        return ResponseEntity.ok(dto);
    }

// ========== ALERTAS RECIENTES PARA DASHBOARD ==========

    @GetMapping("/recientes-dashboard")
    public ResponseEntity<Map<String, Object>> getAlertasRecientes(
            @RequestParam(defaultValue = "10") int limit) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Usuario currentUser = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(FIELD_FECHA_HORA).descending());

        Page<Alerta> alertasPage;
        if (currentUser.getRole() == Role.ADMIN_VILLA) {
            alertasPage = alertaRepository.findByVillaId(currentUser.getVillaId(), pageRequest);
        } else {
            alertasPage = alertaRepository.findAll(pageRequest);
        }

        List<AlertaResponseDto> alertas = alertaMapper.toDtoList(alertasPage.getContent());
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_ALERTAS, alertas);
        response.put(KEY_TOTAL, alertasPage.getTotalElements());
        response.put(KEY_STATUS, VALUE_SUCCESS);
        return ResponseEntity.ok(response);
    }
}