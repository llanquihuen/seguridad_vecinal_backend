package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Controller
public class AdminUserController {
    @Autowired
    UserService userService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/grantaccess")
    public ResponseEntity<String> giveAccessyRut(@RequestParam String rut) {
        boolean existRut = userService.checkRut(rut);
        if (existRut){
            Optional<Usuario> optionalUser = userService.getUserByRut(rut);
            Usuario usuario = optionalUser.get();
            usuario.setVerificado(true);
            usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.OK).body("Se le ha dado acceso al usuario "+rut);
        } else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no existe");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Usuario>> getAllUsers() {
        List<Usuario> listUsers = userService.getAllUsers();
        if (listUsers.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else
            return new ResponseEntity<>(listUsers,HttpStatus.OK);
    }
}
