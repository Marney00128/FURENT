package com.furniterental.controlador;

import com.furniterental.modelo.Favorito;
import com.furniterental.modelo.Producto;
import com.furniterental.repositorio.FavoritoRepositorio;
import com.furniterental.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/favoritos")
public class FavoritoControlador {

    @Autowired
    private FavoritoRepositorio favoritoRepositorio;
    
    @Autowired
    private ProductoRepositorio productoRepositorio;

    // Ver página de favoritos
    @GetMapping
    public String verFavoritos(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para ver tus favoritos");
            return "redirect:/login";
        }

        List<Favorito> favoritos = favoritoRepositorio.findByUsuarioIdOrderByFechaAgregadoDesc(usuarioId);
        model.addAttribute("favoritos", favoritos);

        return "pages/favoritos";
    }

    // Agregar o quitar de favoritos (toggle)
    @PostMapping("/toggle")
    @ResponseBody
    public Map<String, Object> toggleFavorito(
            @RequestParam String productoId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validar que el productoId no sea nulo
        if (productoId == null || productoId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "ID de producto inválido");
            return response;
        }
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Debes iniciar sesión para agregar favoritos");
            response.put("requireLogin", true);
            return response;
        }

        try {
            // Verificar si ya existe el favorito
            Optional<Favorito> favoritoExistente = favoritoRepositorio.findByUsuarioIdAndProductoId(usuarioId, productoId);
            
            if (favoritoExistente.isPresent()) {
                // Si existe, eliminarlo
                Favorito favorito = Objects.requireNonNull(favoritoExistente.orElseThrow());
                favoritoRepositorio.delete(favorito);
                response.put("success", true);
                response.put("action", "removed");
                response.put("message", "Producto eliminado de favoritos");
            } else {
                // Si no existe, agregarlo
                Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
                
                if (!productoOpt.isPresent()) {
                    response.put("success", false);
                    response.put("message", "Producto no encontrado");
                    return response;
                }
                
                Producto producto = productoOpt.get();
                Favorito nuevoFavorito = new Favorito(usuarioId, productoId);
                
                // Guardar datos del producto para consultas rápidas
                nuevoFavorito.setNombreProducto(producto.getNombreProducto());
                nuevoFavorito.setImagenProducto(producto.getImagenProducto());
                nuevoFavorito.setPrecioProducto(producto.getPrecioProducto());
                nuevoFavorito.setCategoriaProducto(producto.getCategoriaProducto());
                
                favoritoRepositorio.save(nuevoFavorito);
                
                response.put("success", true);
                response.put("action", "added");
                response.put("message", "Producto agregado a favoritos");
            }
            
            // Devolver el conteo actualizado
            long cantidadFavoritos = favoritoRepositorio.countByUsuarioId(usuarioId);
            response.put("count", cantidadFavoritos);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar favorito: " + e.getMessage());
        }
        
        return response;
    }

    // Verificar si un producto está en favoritos
    @GetMapping("/check/{productoId}")
    @ResponseBody
    public Map<String, Object> checkFavorito(
            @PathVariable String productoId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("isFavorite", false);
            return response;
        }

        boolean esFavorito = favoritoRepositorio.existsByUsuarioIdAndProductoId(usuarioId, productoId);
        response.put("isFavorite", esFavorito);
        
        return response;
    }

    // Obtener lista de IDs de productos favoritos del usuario
    @GetMapping("/list-ids")
    @ResponseBody
    public Map<String, Object> getFavoritosIds(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("ids", new String[0]);
            return response;
        }

        List<Favorito> favoritos = favoritoRepositorio.findByUsuarioIdOrderByFechaAgregadoDesc(usuarioId);
        String[] ids = favoritos.stream()
                .map(Favorito::getProductoId)
                .toArray(String[]::new);
        
        response.put("ids", ids);
        return response;
    }
}
