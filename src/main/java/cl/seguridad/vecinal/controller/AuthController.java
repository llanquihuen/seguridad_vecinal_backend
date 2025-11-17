package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.dto.*;
import cl.seguridad.vecinal.security.JwtTokenUtil;
import cl.seguridad.vecinal.service.AuthService;
import cl.seguridad.vecinal.service.GoogleAuthService;
import cl.seguridad.vecinal.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import cl.seguridad.vecinal.modelo.Role;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest req) {
        try {
            AuthResponse authResponse = authService.authenticate(loginRequest, req);
            return ResponseEntity.ok(authResponse);
        } catch (Exception ex) {
            // Fallback to legacy messages if authentication fails, but return structured ErrorResponse similar to /google
            String legacy = authService.authenticateLegacy(loginRequest.getEmail(), loginRequest.getPassword());

            if ("NO_VERIFICADO".equals(legacy)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("ACCOUNT_DISABLED", "Usuario en espera de verificación. Contacte al administrador."));
            }
            if ("OK".equals(legacy)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("AUTHENTICATION_FAILED", "Usuario verificado, pero no se pudo completar el inicio de sesión"));
            }
            if ("ADMIN".equals(legacy)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("ADMIN_LOGIN_FAILED", "Administrador autenticado, pero no se pudo generar token"));
            }
            // PASSWORD or any other legacy outcome -> invalid credentials
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("INVALID_CREDENTIALS", "Credenciales Incorrectas"));
        }
    }

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

            // Validar que el token no esté expirado y obtener el username
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

            Usuario usuario = usuarioRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Generar nuevo access token
            String newAccessToken = jwtTokenUtil.generateAccessToken(
                    username,
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Refresh token inválido o expirado",
                    "code", "INVALID_REFRESH_TOKEN"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest body, HttpServletRequest httpReq) {
        try {
            Usuario user = googleAuthService.verifyOrCreateUser(body.getIdToken());
            var pair = refreshTokenService.issuePair(user, httpReq.getHeader("User-Agent"), httpReq.getRemoteAddr());
            String role = user.getRole() != null ? user.getRole().name() : null;
            Boolean isAdmin = user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().name());
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

    @PostMapping("/authenticate")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> authenticate(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        try {
            AuthResponse response = authService.authenticate(loginRequest, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
