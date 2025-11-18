package cl.seguridad.vecinal.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alerta_id")
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String sector;

    @Column
    private String comuna;

    @Column
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlertaEnum tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAlerta estado;

    @Column(nullable = false, name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "atendida_por")
    private Integer atendidaPor; // ID del admin que atendió

    @Column(name = "notas_atencion", length = 1000)
    private String notasAtencion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean silenciosa = false; // Para alertas de pánico silencioso

    // Constructors
    public Alerta() {
        this.fechaHora = LocalDateTime.now();
        this.estado = EstadoAlerta.ACTIVA;
        this.silenciosa = false;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public TipoAlertaEnum getTipo() { return tipo; }
    public void setTipo(TipoAlertaEnum tipo) { this.tipo = tipo; }

    public EstadoAlerta getEstado() { return estado; }
    public void setEstado(EstadoAlerta estado) { this.estado = estado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Integer getAtendidaPor() { return atendidaPor; }
    public void setAtendidaPor(Integer atendidaPor) { this.atendidaPor = atendidaPor; }

    public String getNotasAtencion() { return notasAtencion; }
    public void setNotasAtencion(String notasAtencion) { this.notasAtencion = notasAtencion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Boolean getSilenciosa() { return silenciosa; }
    public void setSilenciosa(Boolean silenciosa) { this.silenciosa = silenciosa; }
}