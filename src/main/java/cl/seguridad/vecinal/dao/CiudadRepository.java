package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
    Optional<Ciudad> findByNombre(String nombre);
    List<Ciudad> findByActivoTrue();
    boolean existsByNombre(String nombre);
}