package cl.seguridad.vecinal.security;

import cl.seguridad.vecinal.dao.UsuarioRepository;
import cl.seguridad.vecinal.modelo.Role;
import cl.seguridad.vecinal.modelo.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;

    // Constructor injection es segura y recomendada; evita nulls y facilita testing
    @Autowired
    public JwtAuthFilter(JwtTokenUtil jwtTokenUtil,
                         UserDetailsService userDetailsService,
                         UsuarioRepository usuarioRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                String username = jwtTokenUtil.getUsernameFromToken(jwt);

                if (StringUtils.hasText(username)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    DatabaseAuthResult dbResult = authenticateUsingDatabase(jwt, username, request, response);

                    if (dbResult == DatabaseAuthResult.FORBIDDEN) {
                        return; // respuesta ya escrita
                    }

                    if (dbResult == DatabaseAuthResult.NOT_APPLICABLE) {
                        authenticateFromClaims(jwt, username, request);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // ======================= Métodos auxiliares para reducir complejidad =======================

    private enum DatabaseAuthResult { AUTHENTICATED, FORBIDDEN, NOT_APPLICABLE }

    private DatabaseAuthResult authenticateUsingDatabase(String jwt,
                                                         String username,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (Boolean.FALSE.equals(jwtTokenUtil.validateToken(jwt, userDetails))) {
                return DatabaseAuthResult.NOT_APPLICABLE;
            }

            // Verificación adicional: estado de cuenta salvo SUPER_ADMIN
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);
            if (usuarioOpt.isPresent() && isAccountAccessBlocked(usuarioOpt.get())) {
                logger.warn("⚠️ Intento de acceso con cuenta desactivada: " + username);
                writeForbiddenResponse(response);
                return DatabaseAuthResult.FORBIDDEN;
            }

            authenticateWithUserDetails(userDetails, request);
            return DatabaseAuthResult.AUTHENTICATED;

        } catch (Exception ex) {
            // No se pudo cargar desde BD (usuario aún no existe o error). Se intenta fallback por token.
            logger.debug("Fallback a autenticación por claims del JWT para usuario: " + username, ex);
            return DatabaseAuthResult.NOT_APPLICABLE;
        }
    }

    private boolean isAccountAccessBlocked(Usuario usuario) {
        return !usuario.isEstadoCuenta() && usuario.getRole() != Role.SUPER_ADMIN;
    }

    private void authenticateWithUserDetails(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateFromClaims(String jwt, String username, HttpServletRequest request) {
        if (Boolean.FALSE.equals(jwtTokenUtil.validateToken(jwt))) {
            return;
        }

        String role = jwtTokenUtil.getRoleFromToken(jwt);
        List<GrantedAuthority> authorities = (role != null && !role.isBlank())
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                : Collections.emptyList();

        User principal = new User(username, "", true, true, true, true, authorities);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void writeForbiddenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + "Cuenta desactivada" + "\",\"code\":\"" + "ACCOUNT_DISABLED" + "\"}");
    }
}