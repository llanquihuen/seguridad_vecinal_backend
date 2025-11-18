package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Villa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VillaRepository extends JpaRepository<Villa, Long> {
    Optional<Villa> findByNombre(String nombre);
    boolean existsByNombre(String nombre);

    List<Villa> findActiveByComunaId(Long comunaId);

    List<Villa> findByActivoTrue();
}