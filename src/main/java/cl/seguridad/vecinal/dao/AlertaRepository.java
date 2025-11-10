// src/main/java/cl/seguridad/vecinal/dao/AlertaRepository.java
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

    // Alertas por usuario
    Page<Alerta> findByUsuario_UsuarioId(Integer usuarioId, Pageable pageable);

    // Alertas por estado
    Page<Alerta> findByEstado(EstadoAlerta estado, Pageable pageable);

    // Alertas activas (para el dashboard)
    @Query("SELECT a FROM Alerta a WHERE a.estado = 'ACTIVA' OR a.estado = 'EN_PROCESO' ORDER BY a.fechaHora DESC")
    List<Alerta> findAlertasActivas();

    // Alertas recientes (últimas 24 horas)
    @Query("SELECT a FROM Alerta a WHERE a.fechaHora >= :fecha ORDER BY a.fechaHora DESC")
    List<Alerta> findAlertasRecientes(@Param("fecha") LocalDateTime fecha);

    // Alertas por tipo
    List<Alerta> findByTipo(TipoAlertaEnum tipo);

    // Alertas por ubicación (radio en km)
    @Query("SELECT a FROM Alerta a WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(a.latitud)) * " +
            "cos(radians(a.longitud) - radians(:lon)) + sin(radians(:lat)) * " +
            "sin(radians(a.latitud)))) <= :radio " +
            "ORDER BY a.fechaHora DESC")
    List<Alerta> findByUbicacion(@Param("lat") Double latitud,
                                 @Param("lon") Double longitud,
                                 @Param("radio") Double radioKm);

    // Contar alertas por estado
    Long countByEstado(EstadoAlerta estado);

    // Contar alertas del día
    @Query("SELECT COUNT(a) FROM Alerta a WHERE DATE(a.fechaHora) = CURRENT_DATE")
    Long countAlertasHoy();

    // Alertas por rango de fechas
    @Query("SELECT a FROM Alerta a WHERE a.fechaHora BETWEEN :inicio AND :fin ORDER BY a.fechaHora DESC")
    List<Alerta> findByFechaHoraBetween(@Param("inicio") LocalDateTime inicio,
                                        @Param("fin") LocalDateTime fin);
}