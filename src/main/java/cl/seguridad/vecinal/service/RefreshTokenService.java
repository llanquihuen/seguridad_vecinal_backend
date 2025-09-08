package cl.seguridad.vecinal.service;

import cl.seguridad.vecinal.dao.RefreshTokenRepository;
import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.RefreshToken;
import cl.seguridad.vecinal.modelo.Usuario;
import cl.seguridad.vecinal.security.JwtTokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final UsuarioRepository usuarios;
    private final JwtTokenUtil jwt;

    public RefreshTokenService(RefreshTokenRepository repo, UsuarioRepository usuarios, JwtTokenUtil jwt) {
        this.repo = repo;
        this.usuarios = usuarios;
        this.jwt = jwt;
    }

    private String hash(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }

    public record Pair(String accessToken, String refreshToken, String jti, String username, String role, Boolean isAdmin) {}

    public Pair issuePair(Usuario user, String userAgent, String ip) {
        String jti = jwt.newJti();
        String refreshToken = jwt.generateRefreshToken(user.getEmail(), jti);
        String accessToken = jwt.generateAccessToken(
                user.getEmail(),
                Map.of(
                        "role", user.getRole() != null ? user.getRole().name() : null,
                        "isAdmin", user.getRole() != null && "ADMIN".equals(user.getRole().name())
                )
        );

        RefreshToken rt = new RefreshToken();
        rt.setUsuario(user);
        rt.setJti(jti);
        rt.setTokenHash(hash(refreshToken));
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(Instant.ofEpochMilli(jwt.getExpirationDateFromToken(refreshToken).getTime()));
        rt.setUserAgent(userAgent);
        rt.setIp(ip);
        repo.save(rt);

        String role = user.getRole() != null ? user.getRole().name() : null;
        Boolean isAdmin = user.getRole() != null && "ADMIN".equals(user.getRole().name());
        return new Pair(accessToken, refreshToken, jti, user.getEmail(), role, isAdmin);
    }

    public Pair rotate(String refreshTokenRaw, String userAgent, String ip) {
        String username = jwt.getUsernameFromToken(refreshTokenRaw);
        String jti = jwt.getJti(refreshTokenRaw);
        var entity = repo.findByJti(jti).orElseThrow(() -> new IllegalArgumentException("Refresh no encontrado"));

        if (entity.getRevokedAt() != null) throw new IllegalStateException("Refresh revocado");
        if (entity.getExpiresAt().isBefore(Instant.now())) throw new IllegalStateException("Refresh expirado");
        if (!entity.getTokenHash().equals(hash(refreshTokenRaw))) throw new IllegalStateException("Refresh inválido");

        // Revoke the current and chain to the new one
        entity.setRevokedAt(Instant.now());
        String newJti = jwt.newJti();
        entity.setReplacedByJti(newJti);
        repo.save(entity);

        Usuario user = usuarios.findByEmail(username).orElseThrow();
        String newRefresh = jwt.generateRefreshToken(username, newJti);
        String newAccess = jwt.generateAccessToken(
                username,
                Map.of(
                        "role", user.getRole() != null ? user.getRole().name() : null,
                        "isAdmin", user.getRole() != null && "ADMIN".equals(user.getRole().name())
                )
        );

        RefreshToken next = new RefreshToken();
        next.setUsuario(user);
        next.setJti(newJti);
        next.setTokenHash(hash(newRefresh));
        next.setCreatedAt(Instant.now());
        next.setExpiresAt(Instant.ofEpochMilli(jwt.getExpirationDateFromToken(newRefresh).getTime()));
        next.setUserAgent(userAgent);
        next.setIp(ip);
        repo.save(next);

        String role = user.getRole() != null ? user.getRole().name() : null;
        Boolean isAdmin = user.getRole() != null && "ADMIN".equals(user.getRole().name());
        return new Pair(newAccess, newRefresh, newJti, username, role, isAdmin);
    }

    public void revoke(String refreshTokenRaw) {
        String jti = jwt.getJti(refreshTokenRaw);
        var entity = repo.findByJti(jti).orElseThrow();
        entity.setRevokedAt(Instant.now());
        repo.save(entity);
    }

    public void revokeAll(Integer usuarioId) {
        repo.deleteAllByUsuario_UsuarioId(usuarioId);
    }
}
