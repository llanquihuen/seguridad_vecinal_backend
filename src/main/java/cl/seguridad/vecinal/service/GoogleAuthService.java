package cl.seguridad.vecinal.service;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleAuthService {
    private final UsuarioRepository usuarios;

    @Value("${google.client-id}")
    private String clientId;

    public GoogleAuthService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario verifyOrCreateUser(String idTokenString) {
        try {
            String token = idTokenString == null ? "" : idTokenString.trim();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("ID Token de Google inválido");
            }

            // Validate via Google's tokeninfo endpoint
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new IllegalArgumentException("ID Token de Google inválido");
            }

            Map<String, Object> payload = resp.getBody();
            String audience = (String) payload.get("aud");

            // ✅ AGREGAR ESTOS LOGS DE DEBUG
            System.out.println("========== GOOGLE TOKEN DEBUG ==========");
            System.out.println("Token audience (aud): " + audience);
            System.out.println("Expected clientId from properties: " + clientId);
            System.out.println("Match: " + (audience != null && audience.equals(clientId)));
            System.out.println("========================================");

            if (audience == null || !audience.equals(clientId)) {
                throw new IllegalArgumentException("Google clientId no coincide");
            }

            String email = (String) payload.get("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("ID Token sin email");
            }

            Usuario usuario = usuarios.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Usuario no registrado. Debe registrarse antes."));

            if (!usuario.isEstadoCuenta()) {
                throw new IllegalStateException("Cuenta deshabilitada. Contacte al administrador.");
            }

            return usuario;
        } catch (Exception e) {
            // Propagate with a clear message while keeping the original cause for logging/debugging.
            throw new RuntimeException("Error verificando ID Token de Google", e);
        }
    }
}