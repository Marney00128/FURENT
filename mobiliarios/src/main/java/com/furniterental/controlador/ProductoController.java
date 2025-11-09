package com.furniterental.controlador;

import com.furniterental.modelo.Producto;
import com.furniterental.modelo.Categoria;
import com.furniterental.servicio.ProductoService;
import com.furniterental.servicio.CategoriaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class ProductoController {
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private CategoriaServicio categoriaServicio;
    
    // Directorio donde se guardarán las imágenes
    private final String UPLOAD_DIR = "src/main/resources/static/img/productos/";
    
    @GetMapping("/products")
    public String showProductsPage(Model model) {
        List<Producto> productos = productoService.getAllProductos();
        List<Categoria> categorias = categoriaServicio.obtenerTodasCategorias();
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        return "admin/products";
    }
    
    @PostMapping("/products/save")
    @ResponseBody
    public ResponseEntity<?> saveProducto(
            @RequestParam String nombreProducto,
            @RequestParam String descripcionProducto,
            @RequestParam Double precioProducto,
            @RequestParam String categoriaProducto,
            @RequestParam(required = false) MultipartFile imagenProducto) {
        
        try {
            Producto producto = new Producto();
            producto.setNombreProducto(nombreProducto);
            producto.setDescripcionProducto(descripcionProducto);
            producto.setPrecioProducto(precioProducto);
            producto.setCategoriaProducto(categoriaProducto);
            
            // Procesar la imagen si se proporciona
            if (imagenProducto != null && !imagenProducto.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imagenProducto.getOriginalFilename();
                Path path = Path.of(UPLOAD_DIR + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, imagenProducto.getBytes());
                producto.setImagenProducto("/img/productos/" + fileName);
            }
            
            productoService.saveProducto(producto);
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Producto guardado correctamente\"}");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"message\": \"Error al guardar el producto: " + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping("/products/list")
    @ResponseBody
    public List<Producto> getProductos() {
        return productoService.getAllProductos();
    }
    
    @DeleteMapping("/products/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProducto(@PathVariable @NonNull String id) {
        try {
            productoService.deleteProducto(id);
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Producto eliminado correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"message\": \"Error al eliminar el producto: " + e.getMessage() + "\"}");
        }
    }
}