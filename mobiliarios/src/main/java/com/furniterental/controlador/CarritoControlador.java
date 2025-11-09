package com.furniterental.controlador;

import com.furniterental.modelo.ItemCarrito;
import com.furniterental.modelo.Producto;
import com.furniterental.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/carrito")
public class CarritoControlador {

    @Autowired
    private ProductoRepositorio productoRepositorio;

    // Ver carrito
    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        System.out.println("=== DEBUG CARRITO ===");
        System.out.println("Cantidad de items en carrito: " + carrito.size());
        for (int i = 0; i < carrito.size(); i++) {
            ItemCarrito item = carrito.get(i);
            System.out.println("Item " + (i+1) + ": " + item.getNombreProducto() + 
                             " | ID: " + item.getProductoId() + 
                             " | Cantidad: " + item.getCantidad() +
                             " | Días: " + item.getDiasAlquiler());
        }
        System.out.println("=====================");

        double total = carrito.stream()
            .mapToDouble(ItemCarrito::getSubtotal)
            .sum();

        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        model.addAttribute("cantidadItems", carrito.size());

        return "pages/carrito";
    }

    // Agregar producto al carrito (API REST)
    @PostMapping("/agregar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(
            @RequestParam String productoId,
            @RequestParam(defaultValue = "1") Integer cantidad,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar parámetros
            if (productoId == null || productoId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de producto inválido");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
            
            if (!productoOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            Producto producto = productoOpt.get();
            
            // Verificar stock disponible
            Integer stockDisponible = producto.getStock();
            if (stockDisponible == null || stockDisponible <= 0) {
                response.put("success", false);
                response.put("message", "Producto sin stock disponible");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (cantidad > stockDisponible) {
                response.put("success", false);
                response.put("message", "Stock insuficiente. Solo hay " + stockDisponible + " unidades disponibles");
                return ResponseEntity.badRequest().body(response);
            }

            @SuppressWarnings("unchecked")
            List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
            
            if (carrito == null) {
                carrito = new ArrayList<>();
            }

            // Verificar si el producto ya está en el carrito
            boolean encontrado = false;
            for (ItemCarrito item : carrito) {
                if (item.getProductoId().equals(productoId)) {
                    int nuevaCantidad = item.getCantidad() + cantidad;
                    // Validar que la nueva cantidad no exceda el stock
                    if (nuevaCantidad > stockDisponible) {
                        response.put("success", false);
                        response.put("message", "Stock insuficiente. Solo hay " + stockDisponible + " unidades disponibles");
                        return ResponseEntity.badRequest().body(response);
                    }
                    item.setCantidad(nuevaCantidad);
                    // Mantener diasAlquiler en 1 (el precio final se calcula con las fechas)
                    item.setDiasAlquiler(1);
                    encontrado = true;
                    break;
                }
            }

            // Si no está, agregarlo
            if (!encontrado) {
                ItemCarrito nuevoItem = new ItemCarrito(
                    producto.getId(),
                    producto.getNombreProducto(),
                    producto.getImagenProducto(),
                    producto.getPrecioProducto(),
                    cantidad,
                    1  // Siempre 1 día por defecto, el precio final se calcula con las fechas
                );
                carrito.add(nuevoItem);
                System.out.println(">>> Producto agregado: " + producto.getNombreProducto());
            } else {
                System.out.println(">>> Producto actualizado: " + productoId);
            }

            session.setAttribute("carrito", carrito);
            System.out.println(">>> Total items en carrito después de agregar: " + carrito.size());

            double total = carrito.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();

            response.put("success", true);
            response.put("message", "Producto agregado al carrito");
            response.put("cantidadItems", carrito.size());
            response.put("total", total);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al agregar producto: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Actualizar cantidad o días
    @PostMapping("/actualizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarItem(
            @RequestParam String productoId,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) Integer diasAlquiler,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
            
            if (carrito == null) {
                response.put("success", false);
                response.put("message", "Carrito vacío");
                return ResponseEntity.badRequest().body(response);
            }

            for (ItemCarrito item : carrito) {
                if (item.getProductoId().equals(productoId)) {
                    if (cantidad != null) {
                        if (cantidad <= 0) {
                            carrito.remove(item);
                        } else {
                            item.setCantidad(cantidad);
                        }
                    }
                    if (diasAlquiler != null && diasAlquiler > 0) {
                        item.setDiasAlquiler(diasAlquiler);
                    }
                    break;
                }
            }

            session.setAttribute("carrito", carrito);

            double total = carrito.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();

            response.put("success", true);
            response.put("cantidadItems", carrito.size());
            response.put("total", total);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Eliminar producto del carrito
    @PostMapping("/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDelCarrito(
            @RequestParam String productoId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
            
            if (carrito != null) {
                carrito.removeIf(item -> item.getProductoId().equals(productoId));
                session.setAttribute("carrito", carrito);

                double total = carrito.stream()
                    .mapToDouble(ItemCarrito::getSubtotal)
                    .sum();

                response.put("success", true);
                response.put("message", "Producto eliminado");
                response.put("cantidadItems", carrito.size());
                response.put("total", total);
            } else {
                response.put("success", false);
                response.put("message", "Carrito vacío");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Vaciar carrito
    @PostMapping("/vaciar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vaciarCarrito(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            session.removeAttribute("carrito");
            response.put("success", true);
            response.put("message", "Carrito vaciado");
            response.put("cantidadItems", 0);
            response.put("total", 0.0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al vaciar carrito: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Obtener cantidad de items (para el contador del header)
    @GetMapping("/cantidad")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerCantidad(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        
        int cantidad = (carrito != null) ? carrito.size() : 0;
        
        response.put("cantidadItems", cantidad);
        return ResponseEntity.ok(response);
    }

    // Obtener datos completos del carrito (para la vista previa)
    @GetMapping("/datos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDatosCarrito(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        System.out.println(">>> /carrito/datos - Items en sesión: " + carrito.size());
        for (ItemCarrito item : carrito) {
            System.out.println("    - " + item.getNombreProducto());
        }

        double total = carrito.stream()
            .mapToDouble(ItemCarrito::getSubtotal)
            .sum();
        
        response.put("items", carrito);
        response.put("cantidadItems", carrito.size());
        response.put("total", total);
        
        return ResponseEntity.ok(response);
    }
}
