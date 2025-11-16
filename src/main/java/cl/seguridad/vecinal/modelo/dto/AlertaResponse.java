package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;

import java.time.LocalDateTime;

public class AlertaResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private String sector;
    private TipoAlertaEnum tipo;
    private EstadoAlerta estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String imagenUrl;
    private UsuarioBasicInfo usuario;

    // Clase interna para info básica del usuario
    public static class UsuarioBasicInfo {
        private Long id;
        private String nombre;
        private String apellido;
        private String email;

        public UsuarioBasicInfo(Long id, String nombre, String apellido, String email) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
        }

        // Getters
        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getEmail() { return email; }
    }

    // Constructor vacío
    public AlertaResponse() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public TipoAlertaEnum getTipo() { return tipo; }
    public void setTipo(TipoAlertaEnum tipo) { this.tipo = tipo; }

    public EstadoAlerta getEstado() { return estado; }
    public void setEstado(EstadoAlerta estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public UsuarioBasicInfo getUsuario() { return usuario; }
    public void setUsuario(UsuarioBasicInfo usuario) { this.usuario = usuario; }
}