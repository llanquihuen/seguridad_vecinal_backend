// src/main/java/cl/seguridad/vecinal/modelo/TipoAlertaEnum.java
package cl.seguridad.vecinal.modelo;

public enum TipoAlertaEnum {
    PANICO("Pánico", "Se ha generado una alerta de pánico"),
    PANICO_SILENCIOSO("Pánico Silencioso", "Se ha generado una alerta silenciosa"),
    ASALTO("Asalto", "Se ha reportado un asalto en curso"),
    ROBO_CASA("Robo de Casa", "Se ha detectado un intento de robo a propiedad"),
    ROBO_VEHICULO("Robo de Vehículo", "Se ha reportado un robo de vehículo"),
    INCENDIO("Incendio", "Se ha reportado un incendio en la zona"),
    EMERGENCIA_MEDICA("Emergencia Médica", "Se requiere asistencia médica urgente"),
    PERSONA_SOSPECHOSA("Persona Sospechosa", "Se ha detectado actividad sospechosa");

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