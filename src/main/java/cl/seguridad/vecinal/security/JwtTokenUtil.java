package cl.seguridad.vecinal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    // New expirations for access and refresh tokens
    @Value("${jwt.access.expiration:300}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration:604800}")
    private long refreshExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Obtiene el rol (claim "role") desde el JWT. Puede ser SUPER_ADMIN, ADMIN_VILLA, etc.
     */
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    public String getJti(String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    /**
     * Valida el token únicamente contra la firma y expiración, sin requerir un UserDetails.
     * Útil como fallback cuando el usuario aún no existe en la base de datos pero el JWT es válido.
     */
    public Boolean validateToken(String token) {
        try {
            // getAllClaimsFromToken ya valida la firma con la clave HMAC configurada
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    public String generateAccessToken(String username, Map<String, Object> extraClaims) {
        return doGenerateToken(extraClaims, username, accessExpiration);
    }

    public String generateRefreshToken(String username, String jti) {
        return Jwts.builder()
                .setSubject(username)
                .setId(jti)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private String doGenerateToken(Map<String, Object> claims, String subject, long seconds) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + seconds * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String newJti() {
        return UUID.randomUUID().toString();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}