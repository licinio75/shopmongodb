package shopsqs.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import shopsqs.demo.model.Usuario;

import java.security.Key;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Genera una clave segura

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(UserDetails userDetails) {
        // Asumimos que el campo "isAdmin" está en los roles o authorities del usuario
        Map<String, Object> claims = new HashMap<>();
        
        // Extraemos el Usuario y su ID del CustomUserDetails
        if (userDetails instanceof CustomUserDetails) {
            Usuario usuario = ((CustomUserDetails) userDetails).getUsuario();
            claims.put("userId", usuario.getId()); // Añadimos el userId al token
        }

        // Agrega los roles a los claims
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        // Verificamos si el usuario tiene el rol de ADMIN
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        claims.put("isAdmin", isAdmin);

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationTime);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername()) // Aquí usamos el email
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(key, SignatureAlgorithm.HS256) // Usa el Key en lugar de secretKey
            .compact();
    }

    // Método para eliminar el prefijo "Bearer " si está presente
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Elimina el prefijo "Bearer "
        }
        return bearerToken; // Retorna el token original si no tiene el prefijo
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder() // Usa parserBuilder en lugar de parser
                .setSigningKey(key)
                .build() // Construye el parser
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class); // Extrae el userId del token
    }

    public List<String> extractRoles(String token) {
        return ((List<?>) Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles")).stream()
                .map(role -> (String) role)
                .collect(Collectors.toList());
    }
    
    

    public boolean isTokenValid(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder() // Usa parserBuilder en lugar de parser
                .setSigningKey(key)
                .build() // Construye el parser
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public Authentication getAuthentication(String token, UserDetails userDetails) {
        if (isTokenValid(token, userDetails.getUsername())) {
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        }
        return null;
    }
}
