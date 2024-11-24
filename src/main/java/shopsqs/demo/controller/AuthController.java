package shopsqs.demo.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import shopsqs.demo.dto.LoginRequestDTO;
import shopsqs.demo.dto.RegistroDTO;
import shopsqs.demo.model.Usuario;
import shopsqs.demo.repository.UsuarioRepository;
import shopsqs.demo.service.AuthService;

import java.time.Duration;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
            return ResponseEntity.badRequest().body("User Unauthorized.");
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
}
