package cl.seguridad.vecinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hash")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class HashGeneratorController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateHash(@RequestParam(defaultValue = "admin123") String password) {
        Map<String, Object> response = new HashMap<>();

        String hash = passwordEncoder.encode(password);

        response.put("password", password);
        response.put("hash", hash);
        response.put("hashLength", hash.length());
        response.put("sqlUpdate", String.format(
                "UPDATE usuario SET password = '%s' WHERE email = 'superadmin@seguridad.cl';",
                hash
        ));

        // Verificar que el hash funcione
        boolean works = passwordEncoder.matches(password, hash);
        response.put("verification", works ? "✅ Hash funciona" : "❌ Hash NO funciona");

        return ResponseEntity.ok(response);
    }
}