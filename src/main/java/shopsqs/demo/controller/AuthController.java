package shopsqs.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import shopsqs.demo.dto.LoginRequestDTO;
import shopsqs.demo.dto.RegistroDTO;
import shopsqs.demo.model.Usuario;
import shopsqs.demo.repository.UsuarioRepository;
import shopsqs.demo.service.AuthService;
import shopsqs.demo.security.JwtService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthService authService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User authenticated successfully, JWT token returned."),
        @ApiResponse(responseCode = "400", description = "Invalid credentials or user does not exist.")
    })
    public ResponseEntity<String> login(
        @Parameter(description = "Login request containing email and password", required = true)
        @RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {

        try {
            // Llama al método login del AuthService
            String token = authService.login(loginRequest);

            // Crear una cookie HttpOnly para almacenar el token JWT
            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true) // Protege contra XSS
                    .secure(false)  // Cambiar a true en producción (requiere HTTPS)
                    .sameSite("Lax") // Protege contra CSRF
                    .path("/")      // Disponible en todas las rutas
                    .maxAge(Duration.ofHours(1)) // Token expira en 1 hora
                    .build();

            // Agregar la cookie a la respuesta
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok("Login successful");
        } catch (RuntimeException e) {
            // Devolver un error 401 si las credenciales son inválidas o el usuario no existe
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Unauthorized.");        
        }
    }

    // Registro de usuarios
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User successfully registered."),
        @ApiResponse(responseCode = "400", description = "The email is already in use.")
    })
    public ResponseEntity<String> register(
        @Parameter(description = "Registration request containing user details", required = true)
        @RequestBody RegistroDTO registroDTO) {
        
        // Verificar si el correo ya existe
        if (usuarioRepository.findByEmail(registroDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("The email is already in use");
        }

        // Crear un nuevo usuario
        Usuario nuevoUsuario = new Usuario();

        String userId = UUID.randomUUID().toString(); // o cualquier método que uses para generar IDs
        nuevoUsuario.setId(userId);
        nuevoUsuario.setEmail(registroDTO.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setNombre(registroDTO.getNombre());
        nuevoUsuario.setAdmin(false);

        // Guardar en la base de datos
        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.ok("User successfully registered");
    }

    // Obtener información del usuario autenticado
    @GetMapping("/user")
    @Operation(summary = "Get authenticated user", description = "Returns the authenticated user's information.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information returned successfully."),
        @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    })
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        String jwtToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                }
            }
        }

        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        try {
            String username = jwtService.extractUsername(jwtToken);
            List<String> roles = jwtService.extractRoles(jwtToken); // Asegúrate de implementar este método

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("roles", roles);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logs out the user by clearing the authentication token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User logged out successfully."),
        @ApiResponse(responseCode = "400", description = "Logout failed.")
    })
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Limpiar la cookie JWT
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // Cambiar a true en producción
                .sameSite("Lax")
                .path("/")
                .maxAge(0) // Expira la cookie inmediatamente
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    
        return ResponseEntity.ok("Logout successful");
    }
    

}
