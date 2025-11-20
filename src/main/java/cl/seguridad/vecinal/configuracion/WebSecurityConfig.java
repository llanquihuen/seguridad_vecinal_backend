package cl.seguridad.vecinal.configuracion;  // ✅ CORREGIDO: Coincide con la carpeta

import cl.seguridad.vecinal.security.JwtAuthFilter;  // ✅ Import correcto
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // ========== ENDPOINTS PÚBLICOS (Sin autenticación) ==========
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/debug/**").permitAll()
                        .requestMatchers("/api/hash/**").permitAll()
                        .requestMatchers("/api/admin/test").permitAll()

                        // ✅ GEOGRAFÍA - PÚBLICO para registro de usuarios
                        .requestMatchers("/api/geografia/**").permitAll()

                        // ✅ USUARIOS - Endpoints públicos para registro
                        .requestMatchers("/api/usuarios/rut/**").permitAll()
                        .requestMatchers("/api/users/findrut/**").permitAll()
                        .requestMatchers("/api/usuarios/register").permitAll()

                        // ========== ENDPOINTS PROTEGIDOS ==========
                        .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN_VILLA")
                        .requestMatchers("/api/usuarios/**").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("════════════════════════════════════════");
        System.out.println("🔐 SECURITY CONFIGURATION LOADED");
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ Endpoints públicos:");
        System.out.println("   - /api/auth/** (login, Google, refresh)");
        System.out.println("   - /api/geografia/** (jerarquía, ciudades, comunas)");
        System.out.println("   - /api/usuarios/rut/** (verificación RUT)");
        System.out.println("   - /api/usuarios/register (registro)");
        System.out.println("════════════════════════════════════════");

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://10.0.2.2:3000",  // ✅ Android emulator
                "http://10.0.2.2:3001",  // ✅ Android emulator
                "https://seguridadvecinalchile.cl"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        System.out.println("✅ AuthenticationProvider configurado");
        System.out.println("   - UserDetailsService: " + userDetailsService.getClass().getSimpleName());
        System.out.println("   - PasswordEncoder: " + passwordEncoder.getClass().getSimpleName());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}