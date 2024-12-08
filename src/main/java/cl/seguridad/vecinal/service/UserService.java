package cl.seguridad.vecinal.service;


import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UsuarioRepository usuarioRepository;

    public Usuario saveUser(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> getUserByRut(String rut){
        return usuarioRepository.findByRut(rut);
    }
    public List<Usuario> getAllUsers(){
        return usuarioRepository.findAll();
    }

    public boolean checkRut (String rut) {return usuarioRepository.existsUsuarioByRut(rut);}

}
