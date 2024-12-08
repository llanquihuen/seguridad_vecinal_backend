package cl.seguridad.vecinal.modelo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer usuarioId;

   private String nombre;
   private String apellido;
   @Column(unique = true)
   private String email;
   @Column(unique = true)
   private String rut;
   private String password;
   private boolean estadoCuenta;
   private LocalDate fechaRegistro;
   private String direccion;
   private Float latitud;
   private Float longitud;
   private boolean verificado;
   private String tokenVerificacion;
   @Enumerated(EnumType.STRING)
   private Role role;

//   @JsonManagedReference
//   @OneToMany(mappedBy = "usuarioId", fetch = FetchType.EAGER)
//   private List<Alerta> alerta;
}
