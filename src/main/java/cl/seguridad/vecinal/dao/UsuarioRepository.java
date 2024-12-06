package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    Optional<Usuario> findByUsuarioId(Integer id);
    Optional<Usuario> findByEmail(String email);
    boolean existsUsuarioByEmail (String email);


}
