package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    Optional<Usuario> findByRut(String rut);
    Optional<Usuario> findByEmail(String email);
    boolean existsUsuarioByEmail (String email);
    boolean existsUsuarioByRut (String rut);

    // NUEVO MÉTODO DE BÚSQUEDA
    @Query("SELECT u FROM Usuario u WHERE " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.rut) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.direccion) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "CAST(u.role AS string) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "(LOWER(:query) LIKE '%activ%' AND u.estadoCuenta = true) OR " +
            "(LOWER(:query) LIKE '%inactiv%' AND u.estadoCuenta = false) OR " +
            "(LOWER(:query) LIKE '%verificad%' AND u.verificado = true) OR " +
            "(LOWER(:query) LIKE '%pendiente%' AND u.verificado = false) OR " +
            "(LOWER(:query) LIKE '%admin%' AND u.role = 'ADMIN') OR " +
            "(LOWER(:query) LIKE '%usuario%' AND u.role = 'USER')")
    Page<Usuario> searchByText(@Param("query") String query, Pageable pageable);
}