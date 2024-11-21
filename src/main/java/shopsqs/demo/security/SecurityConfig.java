package shopsqs.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import shopsqs.demo.repository.UsuarioRepository;
import shopsqs.demo.security.filter.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter, 
                          UsuarioRepository usuarioRepository, 
                          PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS
            .authorizeHttpRequests(auth -> auth // Usar authorizeHttpRequests en lugar de authorizeRequests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Permite OPTIONS
                // Rutas accesibles sin autenticación
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", 
                                 "/api/auth/login", "/api/auth/register", "/api/productos/list", "/actuator/prometheus").permitAll()
                // Rutas restringidas
                .requestMatchers("/api/productos/create").hasAuthority("ROLE_ADMIN") // Solo para admin
                .requestMatchers("/api/pedidos/agregar-producto", "/api/pedidos/carrito", "/api/pedidos/crear-pedido").authenticated() // Solo usuarios autenticados
                .anyRequest().authenticated() // Todas las demás peticiones requieren autenticación
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Añade el filtro JWT

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userDetailsService())
            .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> usuarioRepository.findByEmail(email)
            .map(CustomUserDetails::new)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3001")); // Origen permitido
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Incluye OPTIONS
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With")); // Encabezados permitidos
        configuration.setAllowCredentials(true); // Permite credenciales
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
}
