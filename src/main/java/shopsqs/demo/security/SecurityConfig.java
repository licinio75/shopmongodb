package shopsqs.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import shopsqs.demo.repository.UsuarioRepository;
import shopsqs.demo.security.filter.JwtAuthenticationFilter;

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

    // Configura la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
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

    // Configura el AuthenticationManager y lo registra con el PasswordEncoder
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userDetailsService()) // Establece el servicio de detalles del usuario
            .passwordEncoder(passwordEncoder); // Establece el codificador de contraseñas
        return authenticationManagerBuilder.build();
    }

    // Servicio para cargar los detalles del usuario desde la base de datos
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> usuarioRepository.findByEmail(email)
            .map(CustomUserDetails::new)
            .orElseThrow(() -> new RuntimeException("User not found")); // Mensaje genérico de error
    }

}
