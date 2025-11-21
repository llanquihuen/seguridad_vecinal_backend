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
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/debug/**").permitAll()
                        .requestMatchers("/api/hash/**").permitAll()
                        .requestMatchers("/api/admin/test").permitAll()

                        // ✅ AGREGAR: Geografía pública para registro de usuarios
                        .requestMatchers("/api/geografia/ciudades").permitAll()
                        .requestMatchers("/api/geografia/comunas").permitAll()
                        .requestMatchers("/api/geografia/villas").permitAll()
                        .requestMatchers("/api/geografia/villas/*/sectores").permitAll()
                        .requestMatchers("/api/geografia/jerarquia").permitAll()  // Opcional



                        // Endpoints protegidos
                        .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN_VILLA")
                        // ✅ ELIMINAR esta línea: .requestMatchers("/api/geografia/**").hasAnyRole("SUPER_ADMIN", "ADMIN_VILLA")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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