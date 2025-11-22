package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.dto.*;
import cl.seguridad.vecinal.service.AuthService;
import cl.seguridad.vecinal.service.GoogleAuthService;
import cl.seguridad.vecinal.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request, HttpServletRequest httpReq) {
        var pair = refreshTokenService.rotate(request.getRefreshToken(), httpReq.getHeader("User-Agent"), httpReq.getRemoteAddr());
        return ResponseEntity.ok(new AuthResponse(pair.accessToken(), pair.refreshToken(), pair.username(), pair.role(), pair.isAdmin()));
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