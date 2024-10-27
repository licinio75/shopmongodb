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
@Document(collection = "Usuarios")  // Indica que esta clase se mapea a la colección 'Usuarios' en MongoDB
public class Usuario {
    @Id  // Define 'id' como la clave primaria en MongoDB
    private String id;
    private String nombre;
    private String email;
    private String password;
    private boolean isAdmin;
}
