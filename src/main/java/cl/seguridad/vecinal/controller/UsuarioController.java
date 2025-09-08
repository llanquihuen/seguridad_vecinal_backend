package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Controller
public class UsuarioController {
    @Autowired
    UserService userService;

    @GetMapping("/findrut")
    public ResponseEntity<String> existUserByRut(@RequestParam String rut) {
        boolean existRut = userService.checkRut(rut);
        if (existRut){
            return ResponseEntity.status(HttpStatus.OK).body("Usuario con rut "+rut+ " encontrado");
        } else
            return ResponseEntity.status(HttpStatus.OK).body("Usuario no existe");
    }

    //    @GetMapping("/user/{id}")
//    public ResponseEntity<Usuario> getUserById(@PathVariable Integer id) {
//        Usuario optionalUser = Optional.ofNullable(userService.getUser(id));
//
//        return optionalUser.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

}
