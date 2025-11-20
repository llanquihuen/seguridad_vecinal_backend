package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.dto.AuthResponse;
import cl.seguridad.vecinal.modelo.dto.ErrorResponse;
import cl.seguridad.vecinal.modelo.dto.GoogleLoginRequest;
import cl.seguridad.vecinal.modelo.dto.LoginRequest;
import cl.seguridad.vecinal.security.JwtTokenUtil;
import cl.seguridad.vecinal.service.AuthService;
import cl.seguridad.vecinal.service.GoogleAuthService;
import cl.seguridad.vecinal.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private AuthService authService;

    // ✅ LOGIN CON LOGS DETALLADOS
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            System.out.println("========================================");
            System.out.println("🔐 INTENTO DE LOGIN");
            System.out.println("Email recibido: " + loginRequest.getEmail());
            System.out.println("Password recibido: " + (loginRequest.getPassword() != null ? "***" + loginRequest.getPassword().length() + " chars" : "null"));

            // Buscar usuario
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());

            if (usuarioOpt.isEmpty()) {
                System.out.println("❌ Usuario NO encontrado en BD");
                System.out.println("========================================");
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("code", "USER_NOT_FOUND");
                error.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("✅ Usuario encontrado en BD:");
            System.out.println("   - ID: " + usuario.getUsuarioId());
            System.out.println("   - Email: " + usuario.getEmail());
            System.out.println("   - Rol: " + usuario.getRole());
            System.out.println("   - Estado cuenta: " + usuario.isEstadoCuenta());
            System.out.println("   - Verificado: " + usuario.isVerificado());
            System.out.println("   - Hash almacenado: " + usuario.getPassword().substring(0, 20) + "...");

            // ✅ VALIDACIÓN 1: Solo ADMIN_VILLA y SUPER_ADMIN pueden acceder al dashboard web
           /* if (usuario.getRole() == Role.VECINO) {
                System.out.println("❌ VECINO intentó acceder al dashboard web");
                System.out.println("========================================");
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("code", "VECINO_NO_WEB_ACCESS");
                error.put("message", "Los usuarios VECINO no tienen acceso al dashboard web. Por favor, usa la aplicación móvil.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }*/

            // ✅ VALIDACIÓN 2: Verificar estadoCuenta (excepto SUPER_ADMIN)
            if (!usuario.isEstadoCuenta() && usuario.getRole() != Role.SUPER_ADMIN) {
                System.out.println("❌ Cuenta desactivada (no es SUPER_ADMIN)");
                System.out.println("========================================");
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("code", "ACCOUNT_DISABLED");
                error.put("message", "Tu cuenta está desactivada. Contacta al administrador.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // ✅ VERIFICAR PASSWORD MANUALMENTE PRIMERO
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword());
            System.out.println("🔑 Password match: " + passwordMatches);

            if (!passwordMatches) {
                System.out.println("❌ PASSWORD INCORRECTA");
                System.out.println("   Password enviada: " + loginRequest.getPassword());
                System.out.println("   Hash esperado: " + usuario.getPassword());
                System.out.println("========================================");
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Email o contraseña incorrectos");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            System.out.println("✅ Password correcta, intentando autenticar...");

            // Autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            System.out.println("✅ Autenticación exitosa");

            // Generar tokens
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getRemoteAddr();

            RefreshTokenService.Pair pair = refreshTokenService.issuePair(usuario, userAgent, ip);

            // Crear respuesta
            AuthResponse response = new AuthResponse(
                    pair.accessToken(),
                    pair.refreshToken(),
                    usuario.getEmail(),
                    usuario.getRole().name(),
                    usuario.getRole() == Role.SUPER_ADMIN || usuario.getRole() == Role.ADMIN_VILLA,
                    usuario.getSector(),
                    usuario.getUsuarioId(),
                    usuario.getVillaId(),
                    usuario.getVillaNombre(),
                    usuario.getNombre(),
                    usuario.getApellido()
            );

            System.out.println("✅ Login exitoso: " + usuario.getEmail() + " - Rol: " + usuario.getRole());
            System.out.println("========================================");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error en login: " + e.getClass().getName());
            System.err.println("   Mensaje: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");

            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Email o contraseña incorrectos");
            error.put("debug", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Sesión cerrada exitosamente");
        return ResponseEntity.ok(response);
    }

    // ✅ REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Refresh token es requerido",
                        "code", "MISSING_REFRESH_TOKEN"
                ));
            }

            // Validar refresh token
            if (!jwtTokenUtil.validateToken(refreshToken, null)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "Refresh token inválido o expirado",
                        "code", "INVALID_REFRESH_TOKEN"
                ));
            }

            String email = jwtTokenUtil.getUsernameFromToken(refreshToken);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // ✅ Validar que la cuenta siga activa (excepto SUPER_ADMIN)
            if (!usuario.isEstadoCuenta() && usuario.getRole() != Role.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Cuenta desactivada",
                        "code", "ACCOUNT_DISABLED"
                ));
            }

            // Generar nuevo access token
            String newAccessToken = jwtTokenUtil.generateAccessToken(
                    email,
                    Map.of(
                            "role", usuario.getRole().name(),
                            "isAdmin", usuario.getRole() == Role.SUPER_ADMIN || usuario.getRole() == Role.ADMIN_VILLA
                    )
            );

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "message", "Token renovado exitosamente"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al renovar token: " + e.getMessage(),
                    "code", "REFRESH_ERROR"
            ));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest body, HttpServletRequest httpReq) {
        try {
            Usuario user = googleAuthService.verifyOrCreateUser(body.getIdToken());
            var pair = refreshTokenService.issuePair(user, httpReq.getHeader("User-Agent"), httpReq.getRemoteAddr());
            String role = user.getRole() != null ? user.getRole().name() : null;
            Boolean isAdmin = user.getRole() != null && "ADMIN".equals(user.getRole().name());
            return ResponseEntity.ok(new AuthResponse(pair.accessToken(), pair.refreshToken(), user.getEmail(), role, isAdmin));
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause();
            String msg = cause != null && cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
            if (msg == null) msg = "Error al validar el inicio de sesión con Google";

            if (msg.contains("Usuario no registrado")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("USER_NOT_REGISTERED", "Usuario no registrado. Debe registrarse antes."));
            }
            if (msg.contains("Cuenta deshabilitada")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("ACCOUNT_DISABLED", "Usuario en espera de verificación. Contacte al administrador."));
            }
            if (msg.contains("ID Token de Google inválido") || msg.contains("Google clientId no coincide") || msg.contains("ID Token sin email")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("INVALID_ID_TOKEN", msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("GOOGLE_LOGIN_FAILED", "No se pudo validar el token de Google"));
        }
    }

    @PostMapping("/register")
    public  ResponseEntity<String> register(@RequestBody Usuario usuario){
        if (authService.existUser(usuario)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario ya registrado");
        }
        try{
            authService.registerUser(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con exito");
        } catch (RuntimeException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al registrar usuario: " + ex.getMessage());
        }
    }
}