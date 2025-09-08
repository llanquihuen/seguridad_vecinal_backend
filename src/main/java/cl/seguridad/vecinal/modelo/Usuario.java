package cl.seguridad.vecinal.modelo;

import cl.seguridad.vecinal.modelo.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    // AGREGAR estos nuevos campos:
    private Long villaId;
    private String sector;

    // Lombok generará automáticamente los getters/setters
    // pero si no usas Lombok, agrega manualmente:
    /*
    public Long getVillaId() { return villaId; }
    public void setVillaId(Long villaId) { this.villaId = villaId; }
    
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    */
}