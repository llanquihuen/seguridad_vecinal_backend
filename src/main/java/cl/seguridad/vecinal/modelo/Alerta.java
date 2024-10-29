package cl.seguridad.vecinal.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Alerta {
    @Id
    private int alertaId;
    private LocalDateTime fechaHora;
    private float latitud;
    private float longitud;
    private String descripcion;
    private String estado;
    private boolean silenciosa;
    private int tipoAlertaId;

//    @ManyToOne
//    @JoinColumn(name = "usuarioId")
//    private Usuario usuarioId;
}
