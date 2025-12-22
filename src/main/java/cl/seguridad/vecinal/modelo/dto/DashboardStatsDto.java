package cl.seguridad.vecinal.modelo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class DashboardStatsDto {
    private long totalAlertas;
    private Map<String, Long> alertasPorTipo;
    private Map<String, Long> alertasPorEstado;
    private Map<String, Long> alertasPorDia;
    private Map<Integer, Long> alertasPorHora;
    private List<Map<String, Object>> topSectores;
    private double porcentajeSilenciosas;

}
