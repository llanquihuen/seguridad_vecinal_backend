package cl.seguridad.vecinal.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_alerta",
        uniqueConstraints = @UniqueConstraint(name = "uk_tipo_alerta_nombre", columnNames = "nombre"))
public class TipoAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_alerta_id")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Size(max = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un HEX como #RRGGBB")
    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "icono", length = 50)
    private String icono;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 8) // BAJA/MEDIA/ALTA/CRITICA
    private PrioridadAlerta prioridad = PrioridadAlerta.MEDIA;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}