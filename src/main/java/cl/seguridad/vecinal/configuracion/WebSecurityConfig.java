package cl.seguridad.vecinal.configuracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.configuracion.authentication.AuthenticationManager;
import org.springframework.configuracion.authentication.dao.DaoAuthenticationProvider;
import org.springframework.configuracion.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.configuracion.config.annotation.web.builders.HttpSecurity;
import org.springframework.configuracion.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.configuracion.config.http.SessionCreationPolicy;
import org.springframework.configuracion.core.userdetails.UserDetailsService;
import org.springframework.configuracion.crypto.password.PasswordEncoder;
import org.springframework.configuracion.web.SecurityFilterChain;
import org.springframework.configuracion.web.authentication.UsernamePasswordAuthenticationFilter;
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
                        // ========== ENDPOINTS PÚBLICOS ==========

                        // Autenticación (login, registro, Google login, refresh token)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Geografía (DEBE ser público para registro de usuarios)
                        .requestMatchers("/api/geografia/**").permitAll()  // ✅ CAMBIADO

                        // Testing y debugging
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/debug/**").permitAll()
                        .requestMatchers("/api/hash/**").permitAll()

                        // Verificación de RUT (necesario para registro)
                        .requestMatchers("/api/usuarios/rut/**").permitAll()  // ✅ AGREGADO
                        .requestMatchers("/api/user/findRut/**").permitAll()  // ✅ AGREGADO (si existe este endpoint)

                        // Registro de usuarios
                        .requestMatchers("/api/usuarios/register").permitAll()  // ✅ AGREGADO

                        // ========== ENDPOINTS PROTEGIDOS ==========

                        // Admin
                        .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN_VILLA")

                        // Usuarios (excepto registro que ya está en permitAll)
                        .requestMatchers("/api/usuarios/**").authenticated()

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("════════════════════════════════════════");
        System.out.println("🔐 SECURITY CONFIGURATION");
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ Endpoints públicos configurados:");
        System.out.println("   - /api/auth/** (login, registro, Google)");
        System.out.println("   - /api/geografia/** (ciudades, comunas, villas)");
        System.out.println("   - /api/usuarios/rut/** (verificación RUT)");
        System.out.println("   - /api/usuarios/register (registro usuarios)");
        System.out.println("════════════════════════════════════════");

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Permitir orígenes (agregar tu dominio si tienes)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:8080",
                "http://10.0.2.2:8082",  // ✅ Android emulator
                "*"  // ✅ Permitir todos durante desarrollo (remover en producción)
        ));

        // ✅ Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // ✅ Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // ✅ Permitir credenciales
        configuration.setAllowCredentials(false);  // ✅ CAMBIAR a false cuando usas "*" en origins

        // ✅ Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));

        // ✅ Max age para preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        System.out.println("════════════════════════════════════════");
        System.out.println("🌐 CORS CONFIGURATION");
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ CORS configurado para:");
        System.out.println("   - Orígenes: localhost + Android emulator + todos");
        System.out.println("   - Métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS");
        System.out.println("   - Headers: Todos permitidos");
        System.out.println("════════════════════════════════════════");

        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        System.out.println("════════════════════════════════════════");
        System.out.println("🔐 AUTHENTICATION PROVIDER");
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ AuthenticationProvider configurado");
        System.out.println("   - UserDetailsService: " + userDetailsService.getClass().getSimpleName());
        System.out.println("   - PasswordEncoder: " + passwordEncoder.getClass().getSimpleName());
        System.out.println("════════════════════════════════════════");

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}