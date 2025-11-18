package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    Optional<Comuna> findByNombre(String nombre);
    List<Comuna> findByActivoTrue();
    List<Comuna> findByCiudadId(Long ciudadId);

    @Query("SELECT c FROM Comuna c WHERE c.ciudad.id = :ciudadId AND c.activo = true")
    List<Comuna> findActiveByCiudadId(Long ciudadId);

    boolean existsByNombre(String nombre);
}