package cl.seguridad.vecinal.service;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.dao.VillaRepository;
import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.Villa;
import cl.seguridad.vecinal.modelo.dto.AuthResponse;
import cl.seguridad.vecinal.modelo.dto.LoginRequest;
import cl.seguridad.vecinal.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService (UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Autowired
    private VillaRepository villaRepository;

    public String authenticateLegacy(String email, String password) {
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            return "PASSWORD";
        }
        Usuario usuario = optUsuario.get();
        if (passwordEncoder.matches(password, usuario.getPassword())){
            if (usuario.getRole() == Role.SUPER_ADMIN || usuario.getRole() == Role.ADMIN_VILLA){
                return "ADMIN";
            }
            if (usuario.isVerificado()){
                return "OK";
            } else {
                return "NO_VERIFICADO";
            }
        } else {
            return "PASSWORD";
        }

    }

    public void registerUser(Usuario usuario) {
        // 1. Hashear password
        String encodedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encodedPassword);

        // 2. ✅ ASIGNAR VILLA SI villaId VIENE EN EL OBJETO

        Long villaId = usuario.getVillaId();

        if (villaId != null) {
            Villa villa = villaRepository.findById(villaId)
                    .orElseThrow(() -> new RuntimeException("Villa no encontrada con ID: " + villaId));
            usuario.setVilla(villa);
        }

        // 3. ✅ ASEGURAR QUE fechaRegistro esté presente
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(LocalDate.now());
        }

        // 4. ✅ ASEGURAR que estadoCuenta esté presente (default false para nuevos vecinos)
        // Si viene del registro de app, debe ser verificado por admin
        if (usuario.getRole() == null) {
            usuario.setRole(Role.VECINO);
        }

        // 5. Guardar
        usuarioRepository.save(usuario);
    }

    public boolean existUser(Usuario usuario){
        return usuarioRepository.existsUsuarioByEmail(usuario.getEmail());
    }
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public AuthResponse authenticate(LoginRequest loginRequest, HttpServletRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        Usuario user = usuarioRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        var pair = refreshTokenService.issuePair(
                user,
                request.getHeader("User-Agent"),
                request.getRemoteAddr()
        );
        String role = user.getRole() != null ? user.getRole().name() : null;
        Boolean isAdmin = user.getRole() != null &&
                (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN_VILLA);

        return new AuthResponse(
                pair.accessToken(),
                pair.refreshToken(),
                user.getEmail(),
                role,
                isAdmin,
                user.getSector(),
                user.getUsuarioId(),
                user.getVillaId(),
                user.getVillaNombre(),
                user.getNombre(),
                user.getApellido()
        );
    }
}
