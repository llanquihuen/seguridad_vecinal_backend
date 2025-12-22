package cl.seguridad.vecinal.modelo.mapper;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.dto.AlertaResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertaMapper {

    public AlertaResponseDto toDto(Alerta alerta) {
        return new AlertaResponseDto(alerta);
    }

    public List<AlertaResponseDto> toDtoList(List<Alerta> alertas) {
        return alertas.stream().map(this::toDto).toList();
    }

    public Page<AlertaResponseDto> toDtoPage(Page<Alerta> page) {
        return page.map(this::toDto);
    }
}
