package shopsqs.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import shopsqs.demo.model.Producto;
import shopsqs.demo.repository.ProductoRepository;
import java.util.UUID;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository; 

    @Autowired
    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository; 
    }

    // Endpoint para crear productos solo para administradores
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/create")
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto creado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Error al crear el producto.")
    })
    public ResponseEntity<?> createProducto(
            @CookieValue(value = "jwt", required = false) String jwtToken,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") double precio,
            @RequestParam("stock") int stock,
            @RequestParam("imagen") MultipartFile imagen) { // Cambiado a MultipartFile

        try {
            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. Falta el token JWT.");
            }
            // Crear el producto
            Producto nuevoProducto = new Producto();
            nuevoProducto.setId(UUID.randomUUID().toString());
            nuevoProducto.setNombre(nombre);
            nuevoProducto.setDescripcion(descripcion);
            nuevoProducto.setPrecio(precio);
            nuevoProducto.setStock(stock);
            nuevoProducto.setImagen(imagen.getBytes()); // Convertir MultipartFile a byte[]

            // Guardar en MongoDB
            Producto productoGuardado = productoRepository.save(nuevoProducto);
            return ResponseEntity.ok(productoGuardado);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Endpoint para listar todos los productos (disponible para todos los usuarios)
    @GetMapping("/list")
    @Operation(summary = "Listar productos", description = "Obtiene la lista de todos los productos disponibles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente."),
        @ApiResponse(responseCode = "400", description = "Error al obtener la lista de productos.")
    })
    public ResponseEntity<List<Producto>> listProductos() {
        // Obtener todos los productos de MongoDB
        List<Producto> productos = productoRepository.findAll();
        // Retornar la lista de productos
        return ResponseEntity.ok(productos);
    }
}
