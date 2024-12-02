package shopsqs.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import shopsqs.demo.model.Producto;
import shopsqs.demo.repository.ProductoRepository;
import java.util.UUID;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository; 
    private final Cloudinary cloudinary;

    @Autowired
    public ProductoController(ProductoRepository productoRepository, Cloudinary cloudinary) {
        this.productoRepository = productoRepository; 
        this.cloudinary = cloudinary;
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
            @RequestParam("imagenes") MultipartFile[] imagenes) { 
        try {
            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. Falta el token JWT.");
            }

            // Subir imágenes a Cloudinary 
            List<String> urls = new ArrayList<>(); 
            for (MultipartFile imagen : imagenes) { 
                Map uploadResult = cloudinary.uploader().upload(imagen.getBytes(), ObjectUtils.emptyMap()); 
                urls.add(uploadResult.get("url").toString()); 
            }

            // Crear el producto
            Producto nuevoProducto = new Producto();
            nuevoProducto.setId(UUID.randomUUID().toString());
            nuevoProducto.setNombre(nombre);
            nuevoProducto.setDescripcion(descripcion);
            nuevoProducto.setPrecio(precio);
            nuevoProducto.setStock(stock);
            nuevoProducto.setImagenes(urls);
            
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
    public ResponseEntity<List<Producto>> listProductos(
        @CookieValue(value = "jwt", required = false) String jwtToken
        ) {
        // Obtener todos los productos de MongoDB
        List<Producto> productos = productoRepository.findAll();
        // Retornar la lista de productos
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Obtiene la información completa de un producto dado su identificador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto obtenido exitosamente."),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado.")
    })
    public ResponseEntity<Producto> getProductoById(
            @CookieValue(value = "jwt", required = false) String jwtToken,
            @PathVariable("id") String id) {
        Optional<Producto> productoData = productoRepository.findById(id);

        if (productoData.isPresent()) {
            return new ResponseEntity<>(productoData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
}
