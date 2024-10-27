package shopsqs.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import shopsqs.demo.dto.CarritoDTO;
import shopsqs.demo.dto.ItemCarritoDTO;
import shopsqs.demo.dto.ItemPedidoDTO;
import shopsqs.demo.dto.PedidoSQSMessageDTO;
import shopsqs.demo.model.Pedido;
import shopsqs.demo.model.Producto;
import shopsqs.demo.model.ProductoCantidad;
import shopsqs.demo.model.Usuario;
import shopsqs.demo.repository.PedidoRepository;
import shopsqs.demo.repository.ProductoRepository;
import shopsqs.demo.repository.UsuarioRepository;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shopsqs.demo.security.JwtService;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    public Pedido agregaProducto(String token, String productoId, int cantidad) {
        // Extraer el userId desde el token JWT
        String usuarioId = jwtService.extractUserId(token);
    
        // Buscar el pedido en estado "CARRITO"
        Optional<Pedido> optionalPedido = pedidoRepository.findByUsuarioIdAndEstado(usuarioId, "CARRITO");
        Pedido carrito;
    
        if (optionalPedido.isPresent()) {
            // Si el carrito existe, lo usamos
            carrito = optionalPedido.get();
        } else {
            // Si no existe, creamos un nuevo carrito
            carrito = new Pedido();
            carrito.setId(UUID.randomUUID().toString());
            carrito.setUsuarioId(usuarioId);
            carrito.setEstado("CARRITO");
            carrito.setProductos(new ArrayList<>());
        }
    
        // Obtener los detalles del usuario (nombre y email) desde la tabla Usuarios
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(usuarioId);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            carrito.setUsuarioNombre(usuario.getNombre()); // Establecer el nombre del usuario
            carrito.setUsuarioEmail(usuario.getEmail());   // Establecer el email del usuario
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    
        carrito.setFecha(LocalDateTime.now()); // Establecer la fecha actual
    
        // Buscar el producto por su ID
        Optional<Producto> optionalProducto = productoRepository.findById(productoId);
        if (!optionalProducto.isPresent()) {
            throw new RuntimeException("Producto no encontrado");
        }
    
        Producto producto = optionalProducto.get();
    
        // Verificar si hay suficiente stock
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("No hay suficiente stock disponible para el producto: " + producto.getNombre());
        }
    
        // Verificar si el producto ya está en el carrito
        Optional<ProductoCantidad> productoEnCarrito = carrito.getProductos().stream()
            .filter(p -> p.getProductoId().equals(productoId)) // Comprueba si el productoId coincide
            .findFirst();

        if (productoEnCarrito.isPresent()) {
            // Si ya está en el carrito, actualizamos la cantidad
            ProductoCantidad productoExistente = productoEnCarrito.get();
            
            // Calcula la nueva cantidad
            int nuevaCantidad = productoExistente.getCantidad() + cantidad;

            // Verificar si hay suficiente stock para la nueva cantidad
            if (producto.getStock() < nuevaCantidad) {
                throw new RuntimeException("No hay suficiente stock disponible para el producto: " + producto.getNombre());
            }

            // Actualiza la cantidad del producto en el carrito
            productoExistente.setCantidad(nuevaCantidad); // Actualiza la cantidad
        } else {
            // Si no está en el carrito, lo añadimos como un nuevo producto
            ProductoCantidad nuevoProducto = new ProductoCantidad(productoId, cantidad); // Crea una nueva instancia de ProductoCantidad
            carrito.getProductos().add(nuevoProducto); // Añade el nuevo producto a la lista
        }

    
        // Guardar el carrito actualizado en la base de datos
        pedidoRepository.save(carrito);
    
        // Devolver el carrito actualizado
        return carrito;
    }
    


    public CarritoDTO obtenerCarritoUsuarioLogado(String token) {
        String usuarioId = jwtService.extractUserId(token);
    
        // Recuperar el pedido en estado "CARRITO" del usuario
        Pedido carrito = pedidoRepository.findByUsuarioIdAndEstado(usuarioId, "CARRITO")
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
    
        // Obtener detalles de los productos
        List<ItemCarritoDTO> productos = carrito.getProductos().stream()
                .map(productoCantidad -> {
                    String productoId = productoCantidad.getProductoId();
                    int cantidad = productoCantidad.getCantidad();
    
                    // Consultar el producto en la base de datos
                    Producto producto = productoRepository.findById(productoId)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    
                    // Crear el DTO de item del carrito
                    return ItemCarritoDTO.builder()
                            .productoId(productoId)
                            .nombreProducto(producto.getNombre())
                            .cantidad(cantidad)
                            .precio(producto.getPrecio())
                            .build();
                })
                .collect(Collectors.toList());
    
        // Calcular el precio total
        double precioTotal = productos.stream()
                .mapToDouble(item -> item.getPrecio() * item.getCantidad())
                .sum();
    
        // Retornar el carrito
        return CarritoDTO.builder()
                .productos(productos)
                .precioTotal(precioTotal)
                .build();
    }
    


    // Método para confirmar el pedido y enviar el mensaje a SQS
    public void confirmarPedido(String token) {
        // Extraer el usuarioId desde el token JWT
        String usuarioId = jwtService.extractUserId(token);

        // Buscar el pedido en estado "CARRITO"
        Pedido pedido = pedidoRepository.findByUsuarioIdAndEstado(usuarioId, "CARRITO")
                .orElseThrow(() -> new RuntimeException("No se encontró un pedido en estado CARRITO para este usuario"));

        // Cambiar el estado del pedido a "EN PROCESO"
        pedido.setEstado("EN PROCESO");
        pedido.setFecha(LocalDateTime.now()); // Actualizar la fecha de modificación del pedido
        pedidoRepository.save(pedido);

        // Obtener los detalles del usuario (nombre y email) desde la tabla Usuarios
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear un objeto para encapsular los datos del pedido que queremos enviar
        PedidoSQSMessageDTO mensajeSqs = new PedidoSQSMessageDTO();
        mensajeSqs.setPedidoId(pedido.getId());
        mensajeSqs.setUsuarioNombre(usuario.getNombre());
        mensajeSqs.setUsuarioEmail(usuario.getEmail());

        // Obtener los detalles de los productos
        List<ItemPedidoDTO> itemsPedido = pedido.getProductos().stream()
                .map(productoCantidad -> {
                    String productoId = productoCantidad.getProductoId();
                    int cantidad = productoCantidad.getCantidad();

                    // Consultar el producto en la base de datos
                    Producto producto = productoRepository.findById(productoId)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                    // Crear el objeto que representará cada item en el pedido
                    return new ItemPedidoDTO(
                            productoId,
                            producto.getNombre(),
                            cantidad,
                            producto.getPrecio()
                    );
                }).collect(Collectors.toList());

        // Calcular el precio total del pedido
        double precioTotal = itemsPedido.stream()
            .mapToDouble(ItemPedidoDTO::getPrecioTotal)
            .sum();

        // Añadir el listado de productos y el precio total al mensaje
        mensajeSqs.setItems(itemsPedido);
        mensajeSqs.setPrecioTotal(precioTotal);

        // Convertir el mensaje a JSON
        String messageBody = convertToJson(mensajeSqs);

        // Crear el mensaje para SQS y enviarlo
        /* 
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody)
                .withDelaySeconds(5); // Retraso opcional

        amazonSQS.sendMessage(sendMsgRequest);
        */
    }


    // Método para convertir el objeto PedidoSqsMessage a JSON
    private String convertToJson(PedidoSQSMessageDTO mensajeSqs) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(mensajeSqs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir el mensaje a JSON", e);
        }
    }


}


