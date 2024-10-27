package shopsqs.demo.service;

import shopsqs.demo.model.Usuario;
import shopsqs.demo.dto.LoginRequestDTO;
import shopsqs.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import shopsqs.demo.security.JwtService;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public String login(LoginRequestDTO loginRequest) {

        // Buscar el usuario por email
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (optionalUsuario.isPresent()) {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // Obtener los detalles del usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generar el token JWT usando el método que acepta UserDetails
            return jwtService.generateToken(userDetails);
        } else {
            // Manejar el caso en que el usuario no existe
            throw new RuntimeException("User not found");
        }
    }
}
