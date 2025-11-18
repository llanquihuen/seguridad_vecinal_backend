package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.TipoAlertaEnum;

public class AlertaRequest {
    private String titulo;
    private String descripcion;
    private String sector;
    private TipoAlertaEnum tipo;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String imagenUrl;

    // Constructors
    public AlertaRequest() {}

    public AlertaRequest(String titulo, String descripcion, String sector,
                         TipoAlertaEnum tipo, Double latitud, Double longitud) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.sector = sector;
        this.tipo = tipo;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public TipoAlertaEnum getTipo() { return tipo; }
    public void setTipo(TipoAlertaEnum tipo) { this.tipo = tipo; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}