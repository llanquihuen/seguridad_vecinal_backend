package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.dto.UserCreateRequest;
import cl.seguridad.vecinal.modelo.dto.UserUpdateRequest;
import cl.seguridad.vecinal.modelo.dto.UserResponseDto;
import cl.seguridad.vecinal.service.UserService;
import cl.seguridad.vecinal.dao.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ✅ MÉTODO HELPER: Obtener usuario actual autenticado
    private Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }

    // ========== DASHBOARD STATS ==========
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Usuario currentUser = getCurrentUser();
            Long villaId = currentUser.getRole() == Role.ADMIN_VILLA ? currentUser.getVillaId() : null;

            UserService.UserStats stats = userService.getUserStats(villaId);

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", stats.total);
            response.put("activeUsers", stats.active);
            response.put("verifiedUsers", stats.verified);
            response.put("adminUsers", stats.admins);
            response.put("pendingUsers", stats.pending);
            response.put("villaId", villaId);
            response.put("villaNombre", currentUser.getVillaNombre());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== CRUD USUARIOS ==========

    // ✅ OBTENER USUARIOS RECIENTES (CON FILTRO DE VILLA)
    @GetMapping("/users/recent")
    public ResponseEntity<Map<String, Object>> getRecentUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Usuario currentUser = getCurrentUser();
            Long villaId = currentUser.getRole() == Role.ADMIN_VILLA ? currentUser.getVillaId() : null;

            Sort sort = sortDir.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Usuario> userPage = userService.getUsersPaginated(pageable, villaId);

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

    // ✅ BÚSQUEDA GLOBAL DE USUARIOS (CON FILTRO DE VILLA)
    @GetMapping("/users/search-global")
    public ResponseEntity<Map<String, Object>> searchUsersGlobal(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Usuario currentUser = getCurrentUser();
            Long villaId = currentUser.getRole() == Role.ADMIN_VILLA ? currentUser.getVillaId() : null;

            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Término de búsqueda requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
            Page<Usuario> userPage = userService.searchUsers(query.trim(), pageable, villaId);

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

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error en la búsqueda: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // OBTENER USUARIO POR ID
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Integer id) {
        try {
            Usuario currentUser = getCurrentUser();
            Optional<Usuario> usuarioOpt = userService.getUserById(id);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // ✅ ADMIN_VILLA solo puede ver usuarios de su villa
                if (currentUser.getRole() == Role.ADMIN_VILLA) {
                    if (!usuario.getVillaId().equals(currentUser.getVillaId())) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("status", "error");
                        error.put("message", "No tienes permisos para ver este usuario");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }

                Map<String, Object> response = new HashMap<>();
                response.put("user", new UserResponseDto(usuario));
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

    // ✅ ACTUALIZAR USUARIO (CON VALIDACIÓN DE PERMISOS)
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest request) {

        try {
            Usuario currentUser = getCurrentUser();
            Usuario updatedUser = userService.updateUser(id, request, currentUser);

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

    // ✅ CAMBIAR VERIFICACIÓN DE USUARIO (CON SECTOR)
    @PutMapping("/users/{id}/verification")
    public ResponseEntity<Map<String, Object>> toggleVerification(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            Usuario currentUser = getCurrentUser();
            String sector = body != null ? body.get("sector") : null;

            Usuario user = userService.toggleVerification(id, sector, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(user));
            response.put("status", "success");
            response.put("message", "Estado de verificación actualizado");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
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

    // ✅ CAMBIAR ROL (SOLO SUPER_ADMIN)
    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        try {
            Usuario currentUser = getCurrentUser();
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
                error.put("message", "Rol inválido: " + roleStr);
                return ResponseEntity.badRequest().body(error);
            }

            Usuario usuario = userService.changeUserRole(id, role, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponseDto(usuario));
            response.put("status", "success");
            response.put("message", "Rol actualizado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al actualizar rol: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ✅ ELIMINAR USUARIO (CON VALIDACIÓN)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer id) {
        try {
            Usuario currentUser = getCurrentUser();
            userService.deleteUser(id, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Usuario eliminado permanentemente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    // ✅ OBTENER SECTORES DE LA VILLA DEL ADMIN
    @GetMapping("/sectores")
    public ResponseEntity<Map<String, Object>> obtenerSectores() {
        try {
            Usuario currentUser = getCurrentUser();
            Long villaId = currentUser.getRole() == Role.ADMIN_VILLA ? currentUser.getVillaId() : null;

            List<String> sectores = userService.getSectoresByVilla(villaId);

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

    // TEST ENDPOINT
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testConnection() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Conexión exitosa con la API Spring Boot");
        response.put("status", "OK");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(response);
    }

    // En AdminController.java, agregar:

    @GetMapping("/users/check-rut")
    public ResponseEntity<Map<String, Object>> checkRutExists(@RequestParam String rut) {
        boolean exists = userService.checkRut(rut);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("rut", rut);
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}