package cl.seguridad.vecinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class DebugController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/encoder-info")
    public ResponseEntity<Map<String, Object>> getEncoderInfo() {
        Map<String, Object> info = new HashMap<>();

        // Info del encoder actual
        info.put("encoderClass", passwordEncoder.getClass().getName());
        info.put("encoderToString", passwordEncoder.toString());

        // Test 1: Hash conocido
        String testPassword = "admin123";
        String knownHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi2";
        boolean matchesKnownHash = passwordEncoder.matches(testPassword, knownHash);
        info.put("matchesKnownHash", matchesKnownHash);

        // Test 2: Generar nuevo hash
        String newHash = passwordEncoder.encode(testPassword);
        info.put("newHashGenerated", newHash);

        // Test 3: Verificar que el nuevo hash funcione
        boolean matchesNewHash = passwordEncoder.matches(testPassword, newHash);
        info.put("matchesNewHash", matchesNewHash);

        // Test 4: Crear BCrypt directamente
        BCryptPasswordEncoder directEncoder = new BCryptPasswordEncoder(10);
        boolean directMatch = directEncoder.matches(testPassword, knownHash);
        info.put("directBCryptMatch", directMatch);

        // Test 5: Comparar hashes
        String directHash = directEncoder.encode(testPassword);
        info.put("directHashGenerated", directHash);

        // Info adicional
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("springVersion", org.springframework.core.SpringVersion.getVersion());

        return ResponseEntity.ok(info);
    }

    @PostMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = request.get("hash");

        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", passwordEncoder.matches(password, hash));

        // Test con BCrypt directo
        BCryptPasswordEncoder directEncoder = new BCryptPasswordEncoder(10);
        result.put("directMatches", directEncoder.matches(password, hash));

        return ResponseEntity.ok(result);
    }
}