package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Alerta;
import cl.seguridad.vecinal.modelo.EstadoAlerta;
import cl.seguridad.vecinal.modelo.TipoAlertaEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Integer> {

    // Buscar por sector (sin paginación)
    List<Alerta> findBySector(String sector);

    // ✅ AGREGAR ESTE MÉTODO (con paginación)
    Page<Alerta> findBySector(String sector, Pageable pageable);

    // Buscar por sector y estado
    List<Alerta> findBySectorAndEstado(String sector, EstadoAlerta estado);

    // ✅ AGREGAR ESTE MÉTODO (con paginación)
    Page<Alerta> findBySectorAndEstado(String sector, EstadoAlerta estado, Pageable pageable);

    // Buscar por sector y estado ordenadas por fecha
    List<Alerta> findBySectorAndEstadoOrderByFechaHoraDesc(String sector, EstadoAlerta estado);

    // Buscar por tipo
    List<Alerta> findByTipo(TipoAlertaEnum tipo);

    // Buscar por usuario con paginación
    Page<Alerta> findByUsuario_UsuarioId(Integer usuarioId, Pageable pageable);

    // Buscar por estado con paginación
    Page<Alerta> findByEstado(EstadoAlerta estado, Pageable pageable);

    // Buscar alertas recientes
    @Query("SELECT a FROM Alerta a WHERE a.fechaHora > :fecha ORDER BY a.fechaHora DESC")
    List<Alerta> findAlertasRecientes(@Param("fecha") LocalDateTime fecha);

    // Buscar alertas activas
    @Query("SELECT a FROM Alerta a WHERE a.estado = 'ACTIVA' ORDER BY a.fechaHora DESC")
    List<Alerta> findAlertasActivas();

    // Buscar por rango de fechas
    List<Alerta> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    // Contar por estado
    Long countByEstado(EstadoAlerta estado);

    // Contar alertas de hoy
    @Query("SELECT COUNT(a) FROM Alerta a WHERE DATE(a.fechaHora) = CURRENT_DATE")
    Long countAlertasHoy();

    // Buscar alertas cercanas (dentro de un radio en km)
    @Query(value = "SELECT * FROM alerta " +
            "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(latitud)) * " +
            "cos(radians(longitud) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(latitud)))) < :radioKm " +
            "ORDER BY fecha_hora DESC",
            nativeQuery = true)
    List<Alerta> findByUbicacion(@Param("lat") Double latitud,
                                 @Param("lng") Double longitud,
                                 @Param("radioKm") Double radioKm);
}