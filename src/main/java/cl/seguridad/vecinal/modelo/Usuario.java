package cl.seguridad.vecinal.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usuarioId;

    private String nombre;
    private String apellido;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String rut;

    private String password;
    private boolean estadoCuenta;
    private LocalDate fechaRegistro;
    private String direccion;
    private Float latitud;
    private Float longitud;
    private boolean verificado;
    private String tokenVerificacion;

    @Enumerated(EnumType.STRING)
    private Role role;

    // ✅ RELACIÓN CON VILLA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "villa_id", referencedColumnName = "villa_id")
    @JsonIgnore // ✅ Ignorar la entidad completa en JSON
    private Villa villa;

    // ✅ SECTOR DENTRO DE LA VILLA
    private String sector;

    // ✅ CAMPO TEMPORAL PARA RECIBIR villaId DEL JSON
    @Transient
    private Long tempVillaId;

    // ✅ GETTER PARA SERIALIZAR (Usuario → JSON)
    // Se usa cuando ENVIAMOS el usuario como respuesta
    @JsonProperty("villaId")
    public Long getVillaId() {
        if (villa != null) {
            return villa.getId();
        }
        return tempVillaId;
    }

    // ✅ SETTER PARA DESERIALIZAR (JSON → Usuario)
    // Se usa cuando RECIBIMOS el JSON del registro
    @JsonProperty("villaId")
    public void setVillaId(Long villaId) {
        this.tempVillaId = villaId;
    }

    // ✅ METODO AUXILIAR PARA OBTENER NOMBRE DE VILLA
    @Transient
    @JsonProperty("villaNombre")
    public String getVillaNombre() {
        return villa != null ? villa.getNombre() : null;
    }

    // ========== COMUNA (NUEVA) ==========

    /**
     * Obtiene el ID de la comuna desde la jerarquía Villa -> Comuna
     * Esto se serializa automáticamente en el JSON como "comunaId"
     */
    @Transient
    @JsonProperty("comunaId")
    public Long getComunaId() {
        if (villa != null && villa.getComuna() != null) {
            return villa.getComuna().getId();
        }
        return null;
    }

    /**
     * Obtiene el nombre de la comuna desde la jerarquía Villa -> Comuna
     * Esto se serializa automáticamente en el JSON como "comunaNombre"
     */
    @Transient
    @JsonProperty("comunaNombre")
    public String getComunaNombre() {
        if (villa != null && villa.getComuna() != null) {
            return villa.getComuna().getNombre();
        }
        return null;
    }

    // ========== CIUDAD (NUEVA) ==========

    /**
     * Obtiene el ID de la ciudad desde la jerarquía Villa -> Comuna -> Ciudad
     * Esto se serializa automáticamente en el JSON como "ciudadId"
     */
    @Transient
    @JsonProperty("ciudadId")
    public Long getCiudadId() {
        if (villa != null && villa.getComuna() != null && villa.getComuna().getCiudad() != null) {
            return villa.getComuna().getCiudad().getId();
        }
        return null;
    }

    /**
     * Obtiene el nombre de la ciudad desde la jerarquía Villa -> Comuna -> Ciudad
     * Esto se serializa automáticamente en el JSON como "ciudadNombre"
     */
    @Transient
    @JsonProperty("ciudadNombre")
    public String getCiudadNombre() {
        if (villa != null && villa.getComuna() != null && villa.getComuna().getCiudad() != null) {
            return villa.getComuna().getCiudad().getNombre();
        }
        return null;
    }

}