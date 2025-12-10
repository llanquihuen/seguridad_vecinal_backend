package cl.seguridad.vecinal.security;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("[UserDetailsService] Looking up user by email: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("[UserDetailsService] User not found: {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        logger.info("[UserDetailsService] User found: {}", usuario.getEmail());
        logger.debug("[UserDetailsService] - Role: {}", usuario.getRole());
        logger.debug("[UserDetailsService] - Account enabled: {}", usuario.isEstadoCuenta());
        // Avoid logging password/hash for security reasons

        List<GrantedAuthority> authorities = new ArrayList<>();
        // Opción A: usar roles con prefijo ROLE_ para compatibilidad con hasRole/hasAnyRole
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));

        UserDetails userDetails = new User(
                usuario.getEmail(),
                usuario.getPassword(),  // ✅ Este es el hash que Spring Security comparará
                usuario.isEstadoCuenta(), // enabled
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                authorities
        );

        logger.debug("[UserDetailsService] UserDetails created successfully for {}", usuario.getEmail());

        return userDetails;
    }
}