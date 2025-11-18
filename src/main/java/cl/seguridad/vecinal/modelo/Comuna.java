package cl.seguridad.vecinal.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comuna")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comuna_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id", nullable = false)
    private Ciudad ciudad;

    @OneToMany(mappedBy = "comuna", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Villa> villas = new ArrayList<>();

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Método auxiliar
    @Transient
    public String getCiudadNombre() {
        return ciudad != null ? ciudad.getNombre() : null;
    }
}