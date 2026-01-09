package cl.seguridad.vecinal.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alerta_id")
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String sector;

    @Column
    private String comuna;

    @Column
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlertaEnum tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAlerta estado;

    @Column(nullable = false, name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "atendida_por")
    private Integer atendidaPor; // ID del admin que atendió

    @Column(name = "notas_atencion", length = 1000)
    private String notasAtencion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean silenciosa = false; // Para alertas de pánico silencioso

    // Constructors
    public Alerta() {
        this.fechaHora = LocalDateTime.now();
        this.estado = EstadoAlerta.ACTIVA;
        this.silenciosa = false;
    }

}