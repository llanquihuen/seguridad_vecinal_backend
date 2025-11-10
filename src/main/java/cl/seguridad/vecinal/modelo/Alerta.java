// src/main/java/cl/seguridad/vecinal/modelo/Alerta.java
package cl.seguridad.vecinal.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "alerta")
public class Alerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ Asegura AUTO_INCREMENT
    @Column(name = "alerta_id", nullable = false)
    private Integer alertaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAlerta estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, length = 50)
    private TipoAlertaEnum tipo;

    @Column(name = "silenciosa", nullable = false)
    private Boolean silenciosa;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "comuna", length = 100)
    private String comuna;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "atendida_por")
    private Integer atendidaPor;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    @Column(name = "notas_atencion", length = 1000)
    private String notasAtencion;

    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoAlerta.ACTIVA;
        }
        if (silenciosa == null) {
            silenciosa = false;
        }
    }
}