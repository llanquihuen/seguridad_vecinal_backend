package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    Usuario findByUsuarioId(Integer id);

}
