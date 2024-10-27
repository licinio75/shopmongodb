package shopsqs.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoSQSMessageDTO {
    private String pedidoId;
    private String usuarioNombre;
    private String usuarioEmail;
    private List<ItemPedidoDTO> items;
    private double precioTotal;

}
