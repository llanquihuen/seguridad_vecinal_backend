package cl.seguridad.vecinal.security;

import cl.seguridad.vecinal.dao.UserMapper;
import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        //System.out.println(user.getPassword());
        authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));
        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(), authorities);

    }
}
