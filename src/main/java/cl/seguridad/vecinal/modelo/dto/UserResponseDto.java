package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import java.time.LocalDate;

public class UserResponseDto {
    private Integer usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private String rut;
    private boolean estadoCuenta;
    private LocalDate fechaRegistro;
    private String direccion;
    private Float latitud;
    private Float longitud;
    private boolean verificado;
    private String tokenVerificacion;
    private Role role;
    private String sector;

    // Constructor vacío
    public UserResponseDto() {}

    // Constructor desde entidad Usuario
    public UserResponseDto(Usuario usuario) {
        this.usuarioId = usuario.getUsuarioId();
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.email = usuario.getEmail();
        this.rut = usuario.getRut();
        this.estadoCuenta = usuario.isEstadoCuenta();
        this.fechaRegistro = usuario.getFechaRegistro();
        this.direccion = usuario.getDireccion();
        this.latitud = usuario.getLatitud();
        this.longitud = usuario.getLongitud();
        this.verificado = usuario.isVerificado();
        this.tokenVerificacion = usuario.getTokenVerificacion();
        this.role = usuario.getRole();
        this.sector = usuario.getSector();
    }

    // Constructor completo
    public UserResponseDto(Integer usuarioId, String nombre, String apellido, String email, String rut,
                           boolean estadoCuenta, LocalDate fechaRegistro, String direccion, Float latitud,
                           Float longitud, boolean verificado, String tokenVerificacion, Role role) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rut = rut;
        this.estadoCuenta = estadoCuenta;
        this.fechaRegistro = fechaRegistro;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.verificado = verificado;
        this.tokenVerificacion = tokenVerificacion;
        this.role = role;

    }

    // Getters y Setters
    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public boolean isEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(boolean estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Float getLatitud() {
        return latitud;
    }

    public void setLatitud(Float latitud) {
        this.latitud = latitud;
    }

    public Float getLongitud() {
        return longitud;
    }

    public void setLongitud(Float longitud) {
        this.longitud = longitud;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getTokenVerificacion() {
        return tokenVerificacion;
    }

    public void setTokenVerificacion(String tokenVerificacion) {
        this.tokenVerificacion = tokenVerificacion;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getEstadoTexto() {
        return estadoCuenta ? "Activo" : "Inactivo";
    }

    public String getVerificacionTexto() {
        return verificado ? "Verificado" : "Pendiente";
    }

    @Override
    public String toString() {
        return "UserResponseDto{" +
                "usuarioId=" + usuarioId +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", rut='" + rut + '\'' +
                ", estadoCuenta=" + estadoCuenta +
                ", fechaRegistro=" + fechaRegistro +
                ", direccion='" + direccion + '\'' +
                ", verificado=" + verificado +
                ", role=" + role +
                '}';
    }
}