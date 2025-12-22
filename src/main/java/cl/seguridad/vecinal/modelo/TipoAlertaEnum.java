package cl.seguridad.vecinal.modelo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum TipoAlertaEnum {
    EMERGENCIA("Emergencia", "Situación crítica que requiere atención inmediata"),
    ROBO("Robo", "Robo en progreso o reciente"),
    SOSPECHOSO("Actividad Sospechosa", "Persona o actividad sospechosa"),
    VANDALISMO("Vandalismo", "Daño a propiedad"),
    ACCIDENTE("Accidente", "Accidente vehicular o personal"),
    INCENDIO("Incendio", "Fuego o humo"),
    RUIDO("Ruido", "Disturbios o ruidos molestos"),
    MASCOTA_PERDIDA("Mascota Perdida", "Mascota extraviada"),
    OTRO("Otro", "Otras alertas");

    private final String titulo;
    private final String descripcion;

    TipoAlertaEnum(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    // Utilidad: convertir a lista de mapas simples para exponer por API
    public static List<Map<String, String>> asListOfMaps() {
        List<Map<String, String>> list = new ArrayList<>();
        for (TipoAlertaEnum t : TipoAlertaEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("name", t.name());
            map.put("titulo", t.getTitulo());
            map.put("descripcion", t.getDescripcion());
            list.add(map);
        }
        return list;
    }
}