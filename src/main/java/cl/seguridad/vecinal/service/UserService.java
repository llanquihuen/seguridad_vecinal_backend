package cl.seguridad.vecinal.service;


import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UsuarioRepository usuarioRepository;

    public Usuario saveUser(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public Usuario getUser(Integer id){
        return usuarioRepository.findByUsuarioId(id);
    }

}
