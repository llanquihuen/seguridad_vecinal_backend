package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.dto.UserCreateRequest;
import cl.seguridad.vecinal.modelo.dto.UserUpdateRequest;
import cl.seguridad.vecinal.modelo.dto.UserResponseDto;

import cl.seguridad.vecinal.service.AlertaService;
import cl.seguridad.vecinal.service.UserService;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AlertaService alertaService;

    // ========== DASHBOARD STATS ==========
    // ESTADiSTICAS DEL DASHBOARD (AGREGAR EN LA SECCIÓN DE DASHBOARD)
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Total usuarios
            long totalUsers = userService.countAllUsers();
            stats.put("totalUsers", totalUsers);

            // Usuarios verificados
            long usuariosVerificados = userService.countVerifiedUsers();
            stats.put("usuariosVerificados", usuariosVerificados);

            // ⬇️ CAMBIO CRÍTICO: alertaService (minúscula) en lugar de AlertaService
            long alertasActivas = alertaService.countAlertasByEstado(EstadoAlerta.ACTIVA);
            stats.put("alertasActivas", alertasActivas);

            long alertasEnProceso = alertaService.countAlertasByEstado(EstadoAlerta.EN_PROCESO);
            stats.put("alertasEnProceso", alertasEnProceso);

            long alertasAtendidas = alertaService.countAlertasByEstado(EstadoAlerta.ATENDIDA);
            stats.put("alertasAtendidas", alertasAtendidas);

            long alertasHoy = alertaService.countAlertasHoy();
            stats.put("alertasHoy", alertasHoy);

            // Actividad reciente
            List<Map<String, Object>> actividadReciente = new ArrayList<>();
            List<Alerta> ultimasAlertas = alertaService.findTop5RecentAlertas();

            for (Alerta alerta : ultimasAlertas) {
                Map<String, Object> actividad = new HashMap<>();
                actividad.put("tipo", "alerta");
                actividad.put("accion", "Nueva alerta: " + alerta.getTitulo());
                actividad.put("usuario", alerta.getUsuario().getNombre() + " " + alerta.getUsuario().getApellido());
                actividad.put("tiempo", getTimeAgo(alerta.getFechaHora()));
                actividadReciente.add(actividad);
            }

            stats.put("actividadReciente", actividadReciente);
            stats.put("status", "success");

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Metodo auxiliar
    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Desconocido";

        try {
            long minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());

            if (minutes < 1) return "Ahora";
            if (minutes == 1) return "1 min";
            if (minutes < 60) return minutes + " min";
            if (minutes < 120) return "1 hora";
            if (minutes < 1440) return (minutes / 60) + " horas";
            if (minutes < 2880) return "1 día";
            return (minutes / 1440) + " días";
        } catch (Exception e) {
            return "Hace tiempo";
        }
    }

    // ========== CRUD USUARIOS ==========

    // ✅ RUTAS ESPECÍFICAS PRIMERO (sin parámetros de path)

    // OBTENER USUARIOS RECIENTES (NUEVO - DEBE IR ANTES DE /{id})
    @GetMapping("/users/recent")
    public ResponseEntity<Map<String, Object>> getRecentUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Usuario> userPage = userService.getUsersPaginated(pageable);

            List<UserResponseDto> users = userPage.getContent().stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", userPage.getNumber());
            response.put("totalPages", userPage.getTotalPages());
            response.put("totalElements", userPage.getTotalElements());
            response.put("size", userPage.getSize());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener usuarios recientes: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // BÚSQUEDA GLOBAL DE USUARIOS (MOVIDO AQUÍ - ANTES DE /{id})
    @GetMapping("/users/search-global")
    public ResponseEntity<Map<String, Object>> searchUsersGlobal(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Término de búsqueda requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
            Page<Usuario> userPage = userService.searchUsers(query.trim(), pageable);

            List<UserResponseDto> users = userPage.getContent().stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", userPage.getNumber());
            response.put("totalPages", userPage.getTotalPages());
            response.put("totalElements", userPage.getTotalElements());
            response.put("size", userPage.getSize());
            response.put("query", query);
            response.put("status", "success");
            response.put("searchTips", getSearchTips());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error en la búsqueda: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ✅ RUTAS CON PARÁMETROS DE PATH AL FINAL

    // OBTENER USUARIO POR ID (MOVIDO DESPUÉS DE LAS RUTAS ESPECÍFICAS)
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Integer id) {
        try {
            Optional<Usuario> usuario = userService.getUserById(id);

            if (usuario.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("user", new UserResponseDto(usuario.get()));
                response.put("status", "success");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener usuario: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // OBTENER TODOS LOS USUARIOS (CON PAGINACIÓN)
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Usuario> userPage = userService.getUsersPaginated(pageable);

            List<UserResponseDto> users = userPage.getContent().stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", userPage.getNumber());
            response.put("totalPages", userPage.getTotalPages());
            response.put("totalElements", userPage.getTotalElements());
            response.put("size", userPage.getSize());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener usuarios: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // CREAR NUEVO USUARIO
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            Usuario newUser = userService.createUser(request);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(newUser));
            response.put("status", "success");
            response.put("message", "Usuario creado exitosamente");

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

    // ACTUALIZAR USUARIO
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest request) {

        try {
            Usuario updatedUser = userService.updateUser(id, request);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(updatedUser));
            response.put("status", "success");
            response.put("message", "Usuario actualizado exitosamente");

            return ResponseEntity.ok(response);

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

    // CAMBIAR VERIFICACIÓN DE USUARIO
    @PutMapping("/users/{id}/verification")
    public ResponseEntity<Map<String, Object>> toggleVerification(@PathVariable Integer id) {
        try {
            Usuario user = userService.toggleVerification(id);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(user));
            response.put("status", "success");
            response.put("message", "Estado de verificación actualizado");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    
    // CAMBIAR ESTADO DE CUENTA
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleAccountStatus(@PathVariable Integer id) {
        try {
            Usuario user = userService.toggleAccountStatus(id);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(user));
            response.put("status", "success");
            response.put("message", "Estado de cuenta actualizado");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // DESACTIVAR USUARIO (SOFT DELETE)
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable Integer id) {
        try {
            userService.deactivateUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Usuario desactivado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ELIMINAR USUARIO PERMANENTEMENTE
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Usuario eliminado permanentemente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Método auxiliar para proporcionar tips de búsqueda
    private List<String> getSearchTips() {
        return Arrays.asList(
                "Busca por nombre: 'Juan', 'María'",
                "Busca por email: 'usuario@email.com'",
                "Busca por RUT: '12345678-9'",
                "Busca por estado: 'activo', 'inactivo'",
                "Busca por verificación: 'verificado', 'pendiente'",
                "Busca por rol: 'admin', 'usuario'"
        );
    }

    // TEST ENDPOINT
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testConnection() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Conexión exitosa con la API Spring Boot");
        response.put("status", "OK");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("port", "8082");

        return ResponseEntity.ok(response);
    }

    // ========== GESTIÓN DE SECTORES ==========

    // OBTENER TODOS LOS SECTORES ÚNICOS
    @GetMapping("/sectores")
    public ResponseEntity<Map<String, Object>> obtenerSectores() {
        try {
            List<String> sectores = userService.getAllSectores();

            Map<String, Object> response = new HashMap<>();
            response.put("sectores", sectores);
            response.put("total", sectores.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener sectores: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ASIGNAR SECTOR A USUARIO
    @PutMapping("/usuarios/{id}/sector")
    public ResponseEntity<Map<String, Object>> asignarSector(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        try {
            String sector = request.get("sector");

            if (sector == null || sector.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "El sector es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            Usuario usuario = userService.asignarSector(id, sector.trim());

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(usuario));
            response.put("status", "success");
            response.put("message", "Sector asignado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al asignar sector: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // OBTENER USUARIOS POR SECTOR
    @GetMapping("/usuarios/sector/{sector}")
    public ResponseEntity<Map<String, Object>> obtenerUsuariosPorSector(
            @PathVariable String sector,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Sort sort = Sort.by("fechaRegistro").descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Usuario> userPage = userService.getUsersBySector(sector, pageable);

            List<UserResponseDto> users = userPage.getContent().stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", userPage.getNumber());
            response.put("totalPages", userPage.getTotalPages());
            response.put("totalElements", userPage.getTotalElements());
            response.put("sector", sector);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener usuarios del sector: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        try {
            String roleStr = request.get("role");

            if (roleStr == null || roleStr.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "El rol es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            Role role;
            try {
                role = Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Rol inválido: " + roleStr + ". Valores permitidos: VECINO, ADMIN_VILLA, SUPER_ADMIN");
                return ResponseEntity.badRequest().body(error);
            }

            Usuario usuario = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            usuario.setRole(role);

            // Si es ADMIN o SUPER_ADMIN, verificar automáticamente
            if (role == Role.SUPER_ADMIN || role == Role.ADMIN_VILLA) {
                usuario.setVerificado(true);
            }

            Usuario updated = userService.saveUser(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(updated));
            response.put("status", "success");
            response.put("message", "Rol actualizado exitosamente a " + role.name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al actualizar rol: " + e.getMessage());
            error.put("cause", e.getCause() != null ? e.getCause().getMessage() : "No cause");
            return ResponseEntity.internalServerError().body(error);
        }
    }



}