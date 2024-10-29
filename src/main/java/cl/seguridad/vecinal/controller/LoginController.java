package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Controller
public class LoginController {
    @Autowired
    UserService userService;

    @PostMapping("/user/new")
    public ResponseEntity<Usuario> newUser(@RequestBody Usuario usuario){
        Usuario savedUser =  userService.saveUser(usuario);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Usuario> getUserById(@PathVariable Integer id) {
        Optional<Usuario> optionalUser = Optional.ofNullable(userService.getUser(id));

        return optionalUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
