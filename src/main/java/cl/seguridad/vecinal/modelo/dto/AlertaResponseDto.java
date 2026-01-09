package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponseDto {
    @NotNull
    private Integer alertaId;
    @NotNull
    private Integer usuarioId;
    private String nombreUsuario;
    private String apellidoUsuario;
    @NotNull
    private TipoAlertaEnum tipo;
    private String tipoTitulo;
    private String tipoDescripcion;
    private String descripcion;
    @NotNull
    private Double latitud;
    @NotNull
    private Double longitud;
    private String direccion;
    private String sector;
    private String comuna;
    private String ciudad;
    private String villaNombre;
    @NotNull
    private EstadoAlerta estado;
    @NotNull
    private Boolean silenciosa;
    @NotNull
    private LocalDateTime fechaHora;
    private Integer atendidaPor;
    private LocalDateTime fechaAtencion;
    private String notasAtencion;

    // CONSTRUCTOR DE ENTIDAD ALERTA
    public AlertaResponseDto(Alerta alerta) {
        this.alertaId = alerta.getId();
        this.usuarioId = alerta.getUsuario().getUsuarioId();
        this.nombreUsuario = alerta.getUsuario().getNombre();
        this.apellidoUsuario = alerta.getUsuario().getApellido();
        this.tipo = alerta.getTipo();
        this.tipoTitulo = alerta.getTipo().getTitulo();
        this.tipoDescripcion = alerta.getTipo().getDescripcion();
        this.descripcion = alerta.getDescripcion();
        this.latitud = alerta.getLatitud();
        this.longitud = alerta.getLongitud();
        this.direccion = alerta.getDireccion();
        this.sector = alerta.getSector();

        this.comuna = alerta.getComuna() != null
                ? alerta.getComuna()
                : alerta.getUsuario().getComunaNombre();

        this.ciudad = alerta.getCiudad() != null
                ? alerta.getCiudad()
                : alerta.getUsuario().getCiudadNombre();

        this.villaNombre = alerta.getUsuario().getVillaNombre();
        this.estado = alerta.getEstado();
        this.silenciosa = alerta.getSilenciosa();
        this.fechaHora = alerta.getFechaHora();
        this.atendidaPor = alerta.getAtendidaPor();
        this.fechaAtencion = alerta.getFechaAtencion();
        this.notasAtencion = alerta.getNotasAtencion();
    }
}

