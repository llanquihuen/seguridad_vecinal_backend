// src/main/java/cl/seguridad/vecinal/controller/AlertaController.java
package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
import cl.seguridad.vecinal.modelo.dto.AlertaCreateRequest;
import cl.seguridad.vecinal.modelo.dto.AlertaResponseDto;
import cl.seguridad.vecinal.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    // ========== CREAR ALERTA ==========
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearAlerta(@Valid @RequestBody AlertaCreateRequest request) {
        try {
            Alerta nuevaAlerta = alertaService.crearAlerta(request);

            Map<String, Object> response = new HashMap<>();
            response.put("alerta", new AlertaResponseDto(nuevaAlerta));
            response.put("status", "success");
            response.put("message", "Alerta creada exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER TODAS LAS ALERTAS (CON PAGINACIÓN) ==========
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerAlertasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Alerta> alertaPage = alertaService.obtenerAlertasPaginadas(pageable);

            List<AlertaResponseDto> alertas = alertaPage.getContent().stream()
                    .map(AlertaResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("alertas", alertas);
            response.put("currentPage", alertaPage.getNumber());
            response.put("totalPages", alertaPage.getTotalPages());
            response.put("totalElements", alertaPage.getTotalElements());
            response.put("size", alertaPage.getSize());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener alertas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER ALERTA POR ID ==========
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerAlertaPorId(@PathVariable Integer id) {
        try {
            Optional<Alerta> alerta = alertaService.obtenerAlertaPorId(id);

            if (alerta.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("alerta", new AlertaResponseDto(alerta.get()));
                response.put("status", "success");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Alerta no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener alerta: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER ALERTAS ACTIVAS ==========
    @GetMapping("/activas")
    public ResponseEntity<Map<String, Object>> obtenerAlertasActivas() {
        try {
            List<Alerta> alertas = alertaService.obtenerAlertasActivas();

            List<AlertaResponseDto> alertasDto = alertas.stream()
                    .map(AlertaResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("alertas", alertasDto);
            response.put("total", alertasDto.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener alertas activas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER ALERTAS RECIENTES (ÚLTIMAS 24 HORAS) ==========
    @GetMapping("/recientes")
    public ResponseEntity<Map<String, Object>> obtenerAlertasRecientes() {
        try {
            List<Alerta> alertas = alertaService.obtenerAlertasRecientes();

            List<AlertaResponseDto> alertasDto = alertas.stream()
                    .map(AlertaResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("alertas", alertasDto);
            response.put("total", alertasDto.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener alertas recientes: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER ALERTAS POR USUARIO ==========
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> obtenerAlertasPorUsuario(
            @PathVariable Integer usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaHora").descending());
            Page<Alerta> alertaPage = alertaService.obtenerAlertasPorUsuario(usuarioId, pageable);

            List<AlertaResponseDto> alertas = alertaPage.getContent().stream()
                    .map(AlertaResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("alertas", alertas);
            response.put("currentPage", alertaPage.getNumber());
            response.put("totalPages", alertaPage.getTotalPages());
            response.put("totalElements", alertaPage.getTotalElements());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener alertas del usuario: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER ALERTAS POR ESTADO ==========
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> obtenerAlertasPorEstado(
            @PathVariable String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            EstadoAlerta estadoAlerta = EstadoAlerta.valueOf(estado.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaHora").descending());
            Page<Alerta> alertaPage = alertaService.obtenerAlertasPorEstado(estadoAlerta, pageable);

            List<AlertaResponseDto> alertas = alertaPage.getContent().stream()
                    .map(AlertaResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("alertas", alertas);
            response.put("currentPage", alertaPage.getNumber());
            response.put("totalPages", alertaPage.getTotalPages());
            response.put("totalElements", alertaPage.getTotalElements());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Estado inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener alertas por estado: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== OBTENER ALERTAS CERCANAS ==========
    @GetMapping("/cercanas")
    public ResponseEntity<Map<String, Object>> obtenerAlertasCercanas(
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam(defaultValue = "5.0") Double radio) {

        try {
            List<Alerta> alertas = alertaService.obtenerAlertasCercanas(latitud, longitud, radio);

            List<AlertaResponseDto> alertasDto = alertas.stream()
                    .map(AlertaResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("alertas", alertasDto);
            response.put("total", alertasDto.size());
            response.put("radio", radio + " km");
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener alertas cercanas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== CAMBIAR ESTADO DE ALERTA ==========
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstadoAlerta(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {

        try {
            String estadoStr = (String) request.get("estado");
            Integer adminId = (Integer) request.get("adminId");
            String notas = (String) request.get("notas");

            EstadoAlerta nuevoEstado = EstadoAlerta.valueOf(estadoStr.toUpperCase());

            Alerta alerta = alertaService.cambiarEstadoAlerta(id, nuevoEstado, adminId, notas);

            Map<String, Object> response = new HashMap<>();
            response.put("alerta", new AlertaResponseDto(alerta));
            response.put("status", "success");
            response.put("message", "Estado de alerta actualizado");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Estado inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========== OBTENER ESTADÍSTICAS ==========
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            AlertaService.AlertaStats stats = alertaService.obtenerEstadisticas();

            Map<String, Object> response = new HashMap<>();
            response.put("total", stats.total);
            response.put("activas", stats.activas);
            response.put("enProceso", stats.enProceso);
            response.put("resueltas", stats.resueltas);
            response.put("hoy", stats.hoy);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== ELIMINAR ALERTA ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarAlerta(@PathVariable Integer id) {
        try {
            alertaService.eliminarAlerta(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerta eliminada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========== OBTENER TIPOS DE ALERTA DISPONIBLES ==========
    @GetMapping("/tipos")
    public ResponseEntity<Map<String, Object>> obtenerTiposAlerta() {
        try {
            List<Map<String, String>> tipos = new ArrayList<>();

            for (TipoAlertaEnum tipo : TipoAlertaEnum.values()) {
                Map<String, String> tipoInfo = new HashMap<>();
                tipoInfo.put("value", tipo.name());
                tipoInfo.put("titulo", tipo.getTitulo());
                tipoInfo.put("descripcion", tipo.getDescripcion());
                tipos.add(tipoInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tipos", tipos);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener tipos de alerta: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}