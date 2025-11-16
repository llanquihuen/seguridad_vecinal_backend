package cl.seguridad.vecinal.modelo;

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

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}