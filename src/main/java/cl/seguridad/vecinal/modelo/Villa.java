package cl.seguridad.vecinal.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Size(max = 15)
    @Column(name = "telefono_contacto", length = 15)
    private String telefonoContacto;

    @Email
    @Size(max = 100)
    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ✅ NUEVA RELACIÓN CON COMUNA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    // ✅ LISTA DE SECTORES (guardados como JSON o texto separado por comas)
    @Column(name = "sectores", length = 500)
    private String sectores; // Ej: "Sector A,Sector B,Sector C,Sector Norte,Sector Sur"

    @OneToMany(mappedBy = "villa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();

    // Métodos auxiliares
    @Transient
    public String getComunaNombre() {
        return comuna != null ? comuna.getNombre() : null;
    }

    @Transient
    public String getCiudadNombre() {
        return comuna != null && comuna.getCiudad() != null ? comuna.getCiudad().getNombre() : null;
    }

    @Transient
    public List<String> getSectoresList() {
        if (sectores == null || sectores.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(sectores.split(","));
    }

    public void setSectoresList(List<String> sectoresList) {
        if (sectoresList == null || sectoresList.isEmpty()) {
            this.sectores = null;
        } else {
            this.sectores = String.join(",", sectoresList);
        }
    }
}