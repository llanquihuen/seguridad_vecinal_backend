package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.dto.UserCreateRequest;
import cl.seguridad.vecinal.modelo.dto.UserUpdateRequest;
import cl.seguridad.vecinal.modelo.dto.UserResponseDto;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminController {

    @Autowired
    private UserService userService;

    // ========== DASHBOARD STATS ==========
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            UserService.UserStats stats = userService.getUserStats();

            Map<String, Object> response = new HashMap<>();
            response.put("users", stats.total);
            response.put("sales", 0);
            response.put("orders", 0);
            response.put("revenue", 0);
            response.put("totalUsers", stats.total);
            response.put("activeUsers", stats.active);
            response.put("verifiedUsers", stats.verified);
            response.put("adminUsers", stats.admins);
            response.put("pendingUsers", stats.pending);
            response.put("status", "success");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error específico: " + e.getMessage());
            error.put("cause", e.getCause() != null ? e.getCause().getMessage() : "No cause");
            error.put("stackTrace", e.getClass().getSimpleName());

            return ResponseEntity.internalServerError().body(error);
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

    // CAMBIAR ROL DE USUARIO
    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        try {
            String roleStr = request.get("role");
            Role newRole = Role.valueOf(roleStr.toUpperCase());

            Usuario user = userService.changeUserRole(id, newRole);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(user));
            response.put("status", "success");
            response.put("message", "Rol actualizado exitosamente");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Rol inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
}