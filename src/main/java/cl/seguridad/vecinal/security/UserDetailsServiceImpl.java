package cl.seguridad.vecinal.security;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 [UserDetailsService] Buscando usuario: " + email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ [UserDetailsService] Usuario no encontrado: " + email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        System.out.println("✅ [UserDetailsService] Usuario encontrado: " + usuario.getEmail());
        System.out.println("   - Rol: " + usuario.getRole());
        System.out.println("   - Estado cuenta: " + usuario.isEstadoCuenta());
        System.out.println("   - Password hash: " + usuario.getPassword().substring(0, 20) + "...");

        List<GrantedAuthority> authorities = new ArrayList<>();
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

        System.out.println("✅ [UserDetailsService] UserDetails creado correctamente");

        return userDetails;
    }
}