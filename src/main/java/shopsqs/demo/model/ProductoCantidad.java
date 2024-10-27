package shopsqs.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoCantidad {
    private String productoId; // ID del producto
    private int cantidad; // Cantidad del producto
}
