package shopsqs.demo.repository;

import shopsqs.demo.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends MongoRepository<Producto, String> {
    // Puedes agregar métodos específicos para consultas si lo necesitas
}
