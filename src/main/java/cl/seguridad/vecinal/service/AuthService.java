package cl.seguridad.vecinal.service;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public AuthService (UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticate(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email).get();
            if (passwordEncoder.matches(password, usuario.getPassword())){
                if (usuario.isVerificado()){
                    return "OK";
                }else{
                    return "NO_VERIFICADO";
                }
            } else {
                return "PASSWORD";
            }

    }

    public void registerUser(Usuario usuario) {
        String encodedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encodedPassword);
        usuarioRepository.save(usuario);
    }

    public boolean existUser(Usuario usuario){
        return usuarioRepository.existsUsuarioByEmail(usuario.getEmail());
    }

//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;

//    public AuthResponse authenticate(LoginRequest loginRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getUsername(),
//                        loginRequest.getPassword()
//                )
//        );
//
//        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
//        final String token = jwtTokenUtil.generateToken(userDetails);
//
//        return new AuthResponse(token, userDetails.getUsername());
//    }


}
