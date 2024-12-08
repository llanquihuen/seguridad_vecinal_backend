package cl.seguridad.vecinal.configuracion;

import cl.seguridad.vecinal.security.JwtAuthFilter;
import cl.seguridad.vecinal.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests((auth) -> auth
            .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated()
            );
//            .sessionManagement(session -> session
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            );

//                .authorizeRequests()
//                .antMatchers("/api/**").hasAnyAuthority("ADMIN")
//                .antMatchers("/home/**").hasAnyAuthority("USER", "ADMIN")
//                .anyRequest().authenticated()
//                .and()
//                .usernameParameter("usuario")
//                .passwordParameter("pass");

        return http.build();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

//    @Autowired
//    private CustomAuthenticationSuccessHandler authSuccess;

    @Autowired
    private UserDetailsServiceImpl authService;
}