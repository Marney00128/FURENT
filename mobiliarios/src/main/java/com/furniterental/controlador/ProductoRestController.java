package com.furniterental.controlador;

import com.furniterental.modelo.Producto;
import com.furniterental.servicio.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoService productoService;

    private final Path rutaImagenes = Path.of("src/main/resources/static/img/productos");

    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoService.getAllProductos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable @NonNull String id) {
        return productoService.getProductoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody @NonNull Producto producto) {
        try {
            Producto guardado = productoService.saveProducto(producto);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @NonNull String id) {
        try {
            productoService.deleteProducto(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> subirImagen(@RequestParam("file") MultipartFile archivo) {
        try {
            // Crear directorio si no existe
            if (!Files.exists(rutaImagenes)) {
                Files.createDirectories(rutaImagenes);
            }

            // Generar nombre único para el archivo
            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            
            // Guardar archivo
            Files.copy(archivo.getInputStream(), rutaImagenes.resolve(nombreArchivo));

            return ResponseEntity.ok("/img/productos/" + nombreArchivo);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error al subir imagen: " + e.getMessage());
        }
    }
}