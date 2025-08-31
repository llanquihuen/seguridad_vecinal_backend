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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            response.put("sales", 0); // Placeholder
            response.put("orders", 0); // Placeholder
            response.put("revenue", 0); // Placeholder
            response.put("totalUsers", stats.total);
            response.put("activeUsers", stats.active);
            response.put("verifiedUsers", stats.verified);
            response.put("adminUsers", stats.admins);
            response.put("pendingUsers", stats.pending);
            response.put("status", "success");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener estadísticas");
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== CRUD USUARIOS ==========

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

    // OBTENER USUARIO POR ID
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

    // BUSCAR USUARIOS POR RUT O EMAIL
    @GetMapping("/users/search")
    public ResponseEntity<Map<String, Object>> searchUser(@RequestParam String query) {
        try {
            Optional<Usuario> userByRut = userService.getUserByRut(query);
            Optional<Usuario> userByEmail = userService.getUserByEmail(query);

            Usuario foundUser = userByRut.orElse(userByEmail.orElse(null));

            Map<String, Object> response = new HashMap<>();
            if (foundUser != null) {
                response.put("user", new UserResponseDto(foundUser));
                response.put("status", "success");
                response.put("message", "Usuario encontrado");
            } else {
                response.put("status", "not_found");
                response.put("message", "Usuario no encontrado");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error en la búsqueda: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
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
}