package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti);
    void deleteAllByUsuario_UsuarioId(Integer usuarioId);
}
