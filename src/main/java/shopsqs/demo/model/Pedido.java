package shopsqs.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Document(collection = "Pedidos")  // Define que esta clase se mapea a la colección 'Pedidos' en MongoDB
public class Pedido {

    @Id  // Indica el campo 'id' como la clave primaria en MongoDB
    private String id; // ID del pedido
    
    private String usuarioId; // ID del usuario que realizó el pedido
    private String usuarioNombre; // Nombre del usuario que realizó el pedido
    private String usuarioEmail; // Email del usuario que realizó el pedido

    private List<ProductoCantidad> productos; // Lista de productos y sus cantidades

    private LocalDateTime fecha; // Almacenada como LocalDateTime
    private String estado; // Estado del pedido (por ejemplo, "CARRITO", "EN PROCESO", "COMPLETADO")

    // Constructor para establecer la fecha actual
    public Pedido() {
        this.fecha = LocalDateTime.now();
    }
}
