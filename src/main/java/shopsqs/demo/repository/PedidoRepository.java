package shopsqs.demo.repository;

import shopsqs.demo.model.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {

   // Buscar todos los pedidos para un usuario en un estado específico
   Optional<Pedido> findByUsuarioIdAndEstado(String usuarioId, String estado);
}
