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

    // ✅ RELACIÓN CON VILLA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "villa_id", referencedColumnName = "villa_id")
    private Villa villa;

    // ✅ SECTOR DENTRO DE LA VILLA
    private String sector;

    // ✅ MÉTODOS AUXILIARES
    @Transient
    public Long getVillaId() {
        return villa != null ? villa.getId() : null;
    }

    @Transient
    public String getVillaNombre() {
        return villa != null ? villa.getNombre() : null;
    }

    public void setVillaId(Long villaId) {

    }



}