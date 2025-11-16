package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.TipoAlertaEnum;

public class AlertaCreateRequest {
    private Integer usuarioId;
    private TipoAlertaEnum tipo;
    private String titulo; // ✅ AGREGAR ESTE CAMPO
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String sector;
    private String comuna;
    private String ciudad;
    private Boolean silenciosa;

    // Constructor vacío
    public AlertaCreateRequest() {}

    // Getters y Setters
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public TipoAlertaEnum getTipo() { return tipo; }
    public void setTipo(TipoAlertaEnum tipo) { this.tipo = tipo; }

    // ✅ AGREGAR GETTER Y SETTER PARA TITULO
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public Boolean getSilenciosa() { return silenciosa; }
    public void setSilenciosa(Boolean silenciosa) { this.silenciosa = silenciosa; }
}