package cl.seguridad.vecinal.modelo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "villa")
public class Villa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "villa_id")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Lob
    @Column(name = "direccion")
    private String direccion;

    @Size(max = 10)
    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    // Permite dígitos, espacios y símbolos comunes de teléfono
    @Size(max = 15)
    //@Pattern(regexp = "^[0-9()+\\-\\s]{0,15}$", message = "Teléfono inválido")
    @Column(name = "telefono_contacto", length = 15)
    private String telefonoContacto;

    @Email
    @Size(max = 100)
    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "activo", nullable = false)
   // @Builder.Default
    private Boolean activo = true;
}
