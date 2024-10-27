package shopsqs.demo.repository;

import shopsqs.demo.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    // Método para encontrar un usuario por email
    Optional<Usuario> findByEmail(String email);
}
