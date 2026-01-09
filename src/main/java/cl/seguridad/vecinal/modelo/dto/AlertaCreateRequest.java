package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AlertaCreateRequest {
    @NotNull(message = "El ID del usuario es obligatorio")
    @Schema(description = "ID del usuario que genera la alerta", example = "15")
    private Integer usuarioId;

    @NotNull(message = "El tipo de alerta es obligatorio")
    @Schema(description = "Tipo de alerta", example = "SOSPECHA")
    private TipoAlertaEnum tipo;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres")
    @Schema(description = "Título resumen de la alerta", example = "Vehículo sospechoso en la entrada")
    private String titulo;

    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    @Schema(description = "Descripción detallada del incidente (Opcional)", example = "Auto negro sin patente rondando hace 15 minutos")
    private String descripcion;

    @NotNull(message = "La latitud es obligatoria")
    @Schema(description = "Latitud GPS", example = "-33.4489")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @Schema(description = "Longitud GPS", example = "-70.6693")
    private Double longitud;

    @NotBlank(message = "La dirección es obligatoria")
    @Schema(description = "Dirección aproximada del incidente", example = "Av. Siempre Viva 742")
    private String direccion;

    @Schema(description = "Sector de la villa (Opcional)", example = "Sector Norte")
    private String sector;

    @Schema(description = "Comuna (Opcional, se infiere del usuario si no se envía)", example = "Santiago")
    private String comuna;

    @Schema(description = "Ciudad (Opcional, se infiere del usuario si no se envía)", example = "Santiago")
    private String ciudad;

    @Schema(description = "Indica si es una alerta silenciosa (sin notificación sonora)", example = "false", defaultValue = "false")
    private Boolean silenciosa = false;
}