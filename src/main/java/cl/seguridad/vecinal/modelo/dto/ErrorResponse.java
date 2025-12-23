package cl.seguridad.vecinal.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ErrorResponse implements ApiResponse {
    private String code;
    private String message;
}
