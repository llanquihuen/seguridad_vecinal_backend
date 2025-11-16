// src/main/java/cl/seguridad/vecinal/modelo/dto/AlertaResponseDto.java
package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;

import java.time.LocalDateTime;

public class AlertaResponseDto {
    private Integer alertaId;
    private Integer usuarioId;
    private String nombreUsuario;
    private String apellidoUsuario;
    private TipoAlertaEnum tipo;
    private String tipoTitulo;
    private String tipoDescripcion;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String sector;
    private String comuna;
    private String ciudad;
    private EstadoAlerta estado;
    private Boolean silenciosa;
    private LocalDateTime fechaHora;
    private Integer atendidaPor;
    private LocalDateTime fechaAtencion;
    private String notasAtencion;

    // Constructor desde entidad
    public AlertaResponseDto(Alerta alerta) {
        this.alertaId = alerta.getId(); // ✅ getId() de Alerta
        this.usuarioId = alerta.getUsuario().getUsuarioId(); // ✅ getUsuarioId() existe en Usuario
        this.nombreUsuario = alerta.getUsuario().getNombre();
        this.apellidoUsuario = alerta.getUsuario().getApellido();
        this.tipo = alerta.getTipo();
        this.tipoTitulo = alerta.getTipo().getTitulo();
        this.tipoDescripcion = alerta.getTipo().getDescripcion();
        this.descripcion = alerta.getDescripcion();
        this.latitud = alerta.getLatitud();
        this.longitud = alerta.getLongitud();
        this.direccion = alerta.getDireccion();
        this.sector = alerta.getSector();
        this.comuna = alerta.getComuna();
        this.ciudad = alerta.getCiudad();
        this.estado = alerta.getEstado();
        this.silenciosa = alerta.getSilenciosa();
        this.fechaHora = alerta.getFechaHora();
        this.atendidaPor = alerta.getAtendidaPor();
        this.fechaAtencion = alerta.getFechaAtencion();
        this.notasAtencion = alerta.getNotasAtencion();
    }

    // Getters y Setters
    public Integer getAlertaId() { return alertaId; }
    public void setAlertaId(Integer alertaId) { this.alertaId = alertaId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getApellidoUsuario() { return apellidoUsuario; }
    public void setApellidoUsuario(String apellidoUsuario) { this.apellidoUsuario = apellidoUsuario; }

    public TipoAlertaEnum getTipo() { return tipo; }
    public void setTipo(TipoAlertaEnum tipo) { this.tipo = tipo; }

    public String getTipoTitulo() { return tipoTitulo; }
    public void setTipoTitulo(String tipoTitulo) { this.tipoTitulo = tipoTitulo; }

    public String getTipoDescripcion() { return tipoDescripcion; }
    public void setTipoDescripcion(String tipoDescripcion) { this.tipoDescripcion = tipoDescripcion; }

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

    public EstadoAlerta getEstado() { return estado; }
    public void setEstado(EstadoAlerta estado) { this.estado = estado; }

    public Boolean getSilenciosa() { return silenciosa; }
    public void setSilenciosa(Boolean silenciosa) { this.silenciosa = silenciosa; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getAtendidaPor() { return atendidaPor; }
    public void setAtendidaPor(Integer atendidaPor) { this.atendidaPor = atendidaPor; }

    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public String getNotasAtencion() { return notasAtencion; }
    public void setNotasAtencion(String notasAtencion) { this.notasAtencion = notasAtencion; }
}