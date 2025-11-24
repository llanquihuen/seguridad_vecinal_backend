package cl.seguridad.vecinal.modelo.dto;

import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    private Long villaId;
    private String villaNombre;

    private Long comunaId;
    private String comunaNombre;

    private Long ciudadId;
    private String ciudadNombre;

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

        // ✅ NUEVO: Incluir villa en respuesta
        this.villaId = usuario.getVillaId();
        this.villaNombre = usuario.getVillaNombre();
        this.comunaId = usuario.getComunaId();
        this.comunaNombre = usuario.getComunaNombre();
        this.ciudadId = usuario.getCiudadId();
        this.ciudadNombre = usuario.getCiudadNombre();
    }


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
                ", sector='" + sector + '\'' +
                ", villaId=" + villaId +
                ", villaNombre='" + villaNombre + '\'' +
                '}';
    }
}