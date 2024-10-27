package shopsqs.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Productos")  // Define que esta clase se mapea a la colección 'Productos' en MongoDB
public class Producto {

    @Id  // Indica el campo 'id' como la clave primaria en MongoDB
    private String id;

    private String nombre;
    private String descripcion;
    private double precio;
    private byte[] imagen; // Imagen guardada en MongoDB como array de bytes
    private int stock;
}
