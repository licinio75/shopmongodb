package shopsqs.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarritoDTO {

    private String productoId;
    private String nombreProducto;
    private int cantidad;
    private double precio;
}
