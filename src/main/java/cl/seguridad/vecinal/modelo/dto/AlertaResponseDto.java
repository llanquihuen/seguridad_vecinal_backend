// src/main/java/cl/seguridad/vecinal/modelo/dto/AlertaResponseDto.java
package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
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
    private Integer alertaId;
    private Integer usuarioId;
    private String nombreUsuario;
    private String apellidoUsuario;
    private TipoAlertaEnum tipo;
    private String tipoTitulo;
    private String tipoDescripcion;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String sector;
    private String comuna;
    private String ciudad;
    private String villaNombre;
    private EstadoAlerta estado;
    private Boolean silenciosa;
    private LocalDateTime fechaHora;
    private Integer atendidaPor;
    private LocalDateTime fechaAtencion;
    private String notasAtencion;


    // ✅ CONSTRUCTOR DESDE ENTIDAD ALERTA (AGREGAR ESTE)
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

        // ✅ CAMBIO CRÍTICO: Obtener comuna y ciudad desde Usuario si no existen
        this.comuna = alerta.getComuna() != null
                ? alerta.getComuna()
                : alerta.getUsuario().getComunaNombre();

        this.ciudad = alerta.getCiudad() != null
                ? alerta.getCiudad()
                : alerta.getUsuario().getCiudadNombre();

        // ✅ NUEVO: Obtener nombre de villa desde Usuario
        this.villaNombre = alerta.getUsuario().getVillaNombre();

        this.estado = alerta.getEstado();
        this.silenciosa = alerta.getSilenciosa();
        this.fechaHora = alerta.getFechaHora();
        this.atendidaPor = alerta.getAtendidaPor();
        this.fechaAtencion = alerta.getFechaAtencion();
        this.notasAtencion = alerta.getNotasAtencion();
    }
}

