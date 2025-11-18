package cl.seguridad.vecinal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ✅ CRÍTICO: Usar BCrypt con strength 10 (default de Spring Security)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // Verificar que funcione correctamente al iniciar
        String testPassword = "admin123";
        String expectedHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi2";
        boolean matches = encoder.matches(testPassword, expectedHash);

        System.out.println("════════════════════════════════════════");
        System.out.println("🔐 PasswordEncoder Configuración");
        System.out.println("════════════════════════════════════════");
        System.out.println("Algorithm: BCrypt");
        System.out.println("Strength: 10");
        System.out.println("Test password: admin123");
        System.out.println("Expected hash: " + expectedHash);
        System.out.println("Test result: " + (matches ? "✅ CORRECTO" : "❌ ERROR"));

        if (!matches) {
            System.err.println("⚠️  ADVERTENCIA: PasswordEncoder NO funciona correctamente!");
            System.err.println("⚠️  El login fallará con este encoder");
        }

        System.out.println("════════════════════════════════════════");

        return encoder;
    }
}