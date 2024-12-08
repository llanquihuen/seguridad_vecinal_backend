package cl.seguridad.vecinal.controller;

import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.modelo.dto.LoginRequest;
import cl.seguridad.vecinal.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Controller
public class AuthController {


    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        if (authService.authenticate(loginRequest.getEmail(),loginRequest.getPassword())){
            return ResponseEntity.ok("Login exitoso");
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
        }
    }

    @PostMapping("/register")
    public  ResponseEntity<String> register(@RequestBody Usuario usuario){
        if (authService.existUser(usuario)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario ya registrado");
        }
        try{
            authService.registerUser(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con exito");
        } catch (RuntimeException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al registrar usuario: " + ex.getMessage());
        }
    }



}
