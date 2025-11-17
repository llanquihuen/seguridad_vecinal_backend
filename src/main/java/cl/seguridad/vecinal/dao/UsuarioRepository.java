package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    Optional<Usuario> findByRut(String rut);
    Optional<Usuario> findByEmail(String email);
    boolean existsUsuarioByEmail (String email);
    boolean existsUsuarioByRut (String rut);

    // ✅ FILTRAR POR VILLA (usando villa.id en lugar de villaId)
    @Query("SELECT u FROM Usuario u WHERE u.villa.id = :villaId")
    Page<Usuario> findByVillaId(@Param("villaId") Long villaId, Pageable pageable);

    // ✅ FILTRAR POR VILLA Y SECTOR
    @Query("SELECT u FROM Usuario u WHERE u.villa.id = :villaId AND u.sector = :sector")
    Page<Usuario> findByVillaIdAndSector(@Param("villaId") Long villaId, @Param("sector") String sector, Pageable pageable);

    // ✅ CONTAR POR VILLA
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.villa.id = :villaId")
    long countByVillaId(@Param("villaId") Long villaId);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.villa.id = :villaId AND u.verificado = true")
    long countByVillaIdAndVerificadoTrue(@Param("villaId") Long villaId);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.villa.id = :villaId AND u.estadoCuenta = true")
    long countByVillaIdAndEstadoCuentaTrue(@Param("villaId") Long villaId);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.villa.id = :villaId AND u.role = :role")
    long countByVillaIdAndRole(@Param("villaId") Long villaId, @Param("role") Role role);

    // ✅ BÚSQUEDA CON FILTRO DE VILLA
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:villaId IS NULL OR u.villa.id = :villaId) AND (" +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.rut) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.direccion) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "CAST(u.role AS string) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "(LOWER(:query) LIKE '%activ%' AND u.estadoCuenta = true) OR " +
            "(LOWER(:query) LIKE '%inactiv%' AND u.estadoCuenta = false) OR " +
            "(LOWER(:query) LIKE '%verificad%' AND u.verificado = true) OR " +
            "(LOWER(:query) LIKE '%pendiente%' AND u.verificado = false))")
    Page<Usuario> searchByTextAndVilla(@Param("query") String query, @Param("villaId") Long villaId, Pageable pageable);

    // Buscar usuarios por sector
    Page<Usuario> findBySector(String sector, Pageable pageable);

    // Obtener sectores únicos DE UNA VILLA
    @Query("SELECT DISTINCT u.sector FROM Usuario u WHERE u.villa.id = :villaId AND u.sector IS NOT NULL AND u.sector <> ''")
    List<String> findDistinctSectoresByVillaId(@Param("villaId") Long villaId);

    // Obtener sectores únicos GLOBALES
    @Query("SELECT DISTINCT u.sector FROM Usuario u WHERE u.sector IS NOT NULL AND u.sector <> ''")
    List<String> findDistinctSectores();
}