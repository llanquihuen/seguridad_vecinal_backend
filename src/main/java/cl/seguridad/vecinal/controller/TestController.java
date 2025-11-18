package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.dao.VillaRepository;
import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.Villa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class TestController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VillaRepository villaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ ENDPOINT TEMPORAL PARA CREAR USUARIOS DE PRUEBA
    @PostMapping("/create-admin")
    public ResponseEntity<Map<String, Object>> createTestAdmin() {
        try {
            // Verificar si ya existe
            if (usuarioRepository.findByEmail("admin@test.cl").isPresent()) {
                usuarioRepository.deleteById(
                        usuarioRepository.findByEmail("admin@test.cl").get().getUsuarioId()
                );
            }

            // Crear usuario
            Usuario admin = new Usuario();
            admin.setNombre("Super");
            admin.setApellido("Admin");
            admin.setEmail("admin@test.cl");
            admin.setRut("11111111-1");
            admin.setPassword(passwordEncoder.encode("admin123")); // ✅ Hasheada correctamente
            admin.setRole(Role.SUPER_ADMIN);
            admin.setEstadoCuenta(true);
            admin.setVerificado(true);
            admin.setFechaRegistro(LocalDate.now());

            Usuario saved = usuarioRepository.save(admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario creado correctamente");
            response.put("credentials", Map.of(
                    "email", "admin@test.cl",
                    "password", "admin123",
                    "hashedPassword", saved.getPassword()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ✅ ENDPOINT PARA CREAR ADMIN_VILLA
    @PostMapping("/create-admin-villa")
    public ResponseEntity<Map<String, Object>> createTestAdminVilla() {
        try {
            // Verificar si ya existe
            if (usuarioRepository.findByEmail("admin.villa@test.cl").isPresent()) {
                usuarioRepository.deleteById(
                        usuarioRepository.findByEmail("admin.villa@test.cl").get().getUsuarioId()
                );
            }

            // Buscar primera villa disponible
            Villa villa = villaRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No hay villas disponibles"));

            // Crear usuario
            Usuario adminVilla = new Usuario();
            adminVilla.setNombre("Admin");
            adminVilla.setApellido("Villa");
            adminVilla.setEmail("admin.villa@test.cl");
            adminVilla.setRut("22222222-2");
            adminVilla.setPassword(passwordEncoder.encode("admin123"));
            adminVilla.setRole(Role.ADMIN_VILLA);
            adminVilla.setEstadoCuenta(true);
            adminVilla.setVerificado(true);
            adminVilla.setFechaRegistro(LocalDate.now());
            adminVilla.setVilla(villa);
            adminVilla.setSector("Sector A");

            Usuario saved = usuarioRepository.save(adminVilla);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Admin Villa creado correctamente");
            response.put("credentials", Map.of(
                    "email", "admin.villa@test.cl",
                    "password", "admin123",
                    "villa", villa.getNombre()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ✅ ENDPOINT PARA VERIFICAR HASH
    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            return ResponseEntity.ok(Map.of("error", "Usuario no encontrado"));
        }

        boolean matches = passwordEncoder.matches(password, usuario.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("passwordMatches", matches);
        response.put("storedHash", usuario.getPassword());
        response.put("role", usuario.getRole());
        response.put("estadoCuenta", usuario.isEstadoCuenta());

        return ResponseEntity.ok(response);
    }
}