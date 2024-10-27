package shopsqs.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import shopsqs.demo.dto.CarritoDTO;
import shopsqs.demo.model.Pedido;
import shopsqs.demo.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Método para agregar un producto al carrito
    @PostMapping("/agregar-producto")
    @Operation(summary = "Agregar producto al carrito", description = "Agrega un producto al carrito del usuario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto agregado al carrito."),
        @ApiResponse(responseCode = "400", description = "Error al agregar el producto.")
    })
    public ResponseEntity<?> agregarProductoAlCarrito(@RequestHeader("Authorization") String token,
                                                       @RequestParam String productoId,
                                                       @RequestParam int cantidad) {
        try {
            Pedido pedidoActualizado = pedidoService.agregaProducto(token, productoId, cantidad);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Método para obtener el carrito del usuario logado
    @GetMapping("/carrito")
    @Operation(summary = "Obtener carrito", description = "Obtiene el carrito del usuario logado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrito obtenido con éxito."),
        @ApiResponse(responseCode = "400", description = "Error al obtener el carrito.")
    })
    public ResponseEntity<?> obtenerCarrito(@RequestHeader("Authorization") String token) {
        try {
            CarritoDTO carrito = pedidoService.obtenerCarritoUsuarioLogado(token);
            return ResponseEntity.ok(carrito);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Crea pedido, pasando su estado de "CARRITO" a "EN PROCESO"
    @PostMapping("/crear-pedido")
    @Operation(summary = "Crear pedido", description = "Confirma el pedido y lo envía a la cola SQS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido confirmado y mensaje enviado a la cola SQS."),
        @ApiResponse(responseCode = "400", description = "Error al confirmar el pedido.")
    })
    public ResponseEntity<String> crearPedido(@RequestHeader("Authorization") String token) {
        try {
            pedidoService.confirmarPedido(token);
            return ResponseEntity.ok("Pedido confirmado y mensaje enviado a la cola SQS.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
