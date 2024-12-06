package cl.seguridad.vecinal.controller;

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
@Controller
public class LoginController {
    @Autowired
    UserService userService;

    @PostMapping("/user/new")
    public ResponseEntity<Usuario> newUser(@RequestBody Usuario usuario){
        Usuario savedUser =  userService.saveUser(usuario);
        return ResponseEntity.ok(savedUser);
    }

//    @GetMapping("/user/{id}")
//    public ResponseEntity<Usuario> getUserById(@PathVariable Integer id) {
//        Usuario optionalUser = Optional.ofNullable(userService.getUser(id));
//
//        return optionalUser.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> getAllUsers() {
        List<Usuario> listUsers = userService.getAllUsers();
        if (listUsers.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else
            return new ResponseEntity<>(listUsers,HttpStatus.OK);
    }
}
