package cl.seguridad.vecinal.modelo.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CambioEstadoRequest {
    private String estado;
    private Integer adminId;
    private String notas;

}
