package com.furniterental.controlador;

import com.furniterental.dto.ResenaIndividualDTO;
import com.furniterental.modelo.Resena;
import com.furniterental.servicio.ResenaServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/resenas")
public class ResenaControlador {
    
    @Autowired
    private ResenaServicio resenaServicio;
    
    /**
     * Crear una nueva reseña
     */
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearResena(
            @RequestParam String alquilerId,
            @RequestParam String productoId,
            @RequestParam int calificacion,
            @RequestParam String comentario,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar sesión
            String usuarioId = (String) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión para dejar una reseña");
                return ResponseEntity.status(401).body(response);
            }
            
            // Validar calificación
            if (calificacion < 1 || calificacion > 5) {
                response.put("success", false);
                response.put("message", "La calificación debe estar entre 1 y 5 estrellas");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar comentario
            if (comentario == null || comentario.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "El comentario no puede estar vacío");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Crear la reseña
            Resena resena = resenaServicio.crearResena(alquilerId, usuarioId, productoId, calificacion, comentario);
            
            response.put("success", true);
            response.put("message", "¡Gracias por tu reseña! Será revisada antes de publicarse.");
            response.put("resena", resena);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al crear la reseña: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtener reseñas aprobadas de un producto
     */
    @GetMapping("/producto/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerResenasPorProducto(@PathVariable String productoId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Resena> resenas = resenaServicio.obtenerResenasAprobadasPorProducto(productoId);
            Map<String, Object> estadisticas = resenaServicio.obtenerEstadisticasProducto(productoId);
            
            response.put("success", true);
            response.put("resenas", resenas);
            response.put("estadisticas", estadisticas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener reseñas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtener reseñas del usuario actual
     */
    @GetMapping("/mis-resenas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerMisResenas(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return ResponseEntity.status(401).body(response);
            }
            
            List<Resena> resenas = resenaServicio.obtenerResenasPorUsuario(usuarioId);
            
            response.put("success", true);
            response.put("resenas", resenas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener reseñas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Verificar si el usuario puede dejar reseña para un alquiler
     */
    @GetMapping("/puede-resenar/{alquilerId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> puedeDejarResena(@PathVariable String alquilerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean puede = resenaServicio.puedeDejarResena(alquilerId);
            
            response.put("success", true);
            response.put("puedeResenar", puede);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtener IDs de productos que ya tienen reseña para un alquiler
     */
    @GetMapping("/productos-resenados/{alquilerId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerProductosResenados(@PathVariable String alquilerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Resena> resenas = resenaServicio.obtenerResenasPorAlquiler(alquilerId);
            List<String> productosResenados = new java.util.ArrayList<>();
            
            for (Resena resena : resenas) {
                productosResenados.add(resena.getProductoId());
            }
            
            response.put("success", true);
            response.put("productosResenados", productosResenados);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener productos reseñados: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtener estadísticas de un producto
     */
    @GetMapping("/estadisticas/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@PathVariable String productoId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> estadisticas = resenaServicio.obtenerEstadisticasProducto(productoId);
            
            response.put("success", true);
            response.put("estadisticas", estadisticas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // ========== ENDPOINTS DE ADMINISTRADOR ==========
    
    /**
     * Obtener conteo de reseñas pendientes (Admin)
     */
    @GetMapping("/admin/pendientes/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> contarResenasPendientes(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que sea administrador
            String usuarioId = (String) session.getAttribute("usuarioId");
            String rol = (String) session.getAttribute("usuarioRol");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "No hay sesión activa");
                response.put("count", 0);
                return ResponseEntity.status(401).body(response);
            }
            
            if (!"ADMIN".equals(rol)) {
                response.put("success", false);
                response.put("message", "Acceso denegado");
                response.put("count", 0);
                return ResponseEntity.status(403).body(response);
            }
            
            List<Resena> resenas = resenaServicio.obtenerResenasPendientes();
            
            response.put("success", true);
            response.put("count", resenas.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al contar reseñas: " + e.getMessage());
            response.put("count", 0);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtener todas las reseñas pendientes (Admin)
     */
    @GetMapping("/admin/pendientes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerResenasPendientes(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que sea administrador
            String usuarioId = (String) session.getAttribute("usuarioId");
            String rol = (String) session.getAttribute("usuarioRol");
            
            System.out.println("=== DEBUG RESENAS PENDIENTES ===");
            System.out.println("Usuario ID: " + usuarioId);
            System.out.println("Rol: " + rol);
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "No hay sesión activa. Por favor inicia sesión.");
                return ResponseEntity.status(401).body(response);
            }
            
            if (!"ADMIN".equals(rol)) {
                response.put("success", false);
                response.put("message", "Acceso denegado. Se requiere rol de administrador.");
                return ResponseEntity.status(403).body(response);
            }
            
            List<Resena> resenas = resenaServicio.obtenerResenasPendientes();
            System.out.println("Reseñas pendientes encontradas: " + resenas.size());
            
            response.put("success", true);
            response.put("resenas", resenas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al obtener reseñas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Aprobar una reseña (Admin)
     */
    @PostMapping("/admin/aprobar/{resenaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aprobarResena(
            @PathVariable String resenaId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que sea administrador
            String rol = (String) session.getAttribute("usuarioRol");
            if (!"ADMIN".equals(rol)) {
                response.put("success", false);
                response.put("message", "Acceso denegado");
                return ResponseEntity.status(403).body(response);
            }
            
            Resena resena = resenaServicio.aprobarResena(resenaId);
            
            response.put("success", true);
            response.put("message", "Reseña aprobada exitosamente");
            response.put("resena", resena);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al aprobar reseña: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Rechazar una reseña (Admin)
     */
    @PostMapping("/admin/rechazar/{resenaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rechazarResena(
            @PathVariable String resenaId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que sea administrador
            String rol = (String) session.getAttribute("usuarioRol");
            if (!"ADMIN".equals(rol)) {
                response.put("success", false);
                response.put("message", "Acceso denegado");
                return ResponseEntity.status(403).body(response);
            }
            
            Resena resena = resenaServicio.rechazarResena(resenaId);
            
            response.put("success", true);
            response.put("message", "Reseña rechazada");
            response.put("resena", resena);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al rechazar reseña: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Responder a una reseña (Admin)
     */
    @PostMapping("/admin/responder/{resenaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> responderResena(
            @PathVariable String resenaId,
            @RequestParam String respuesta,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que sea administrador
            String rol = (String) session.getAttribute("usuarioRol");
            if (!"ADMIN".equals(rol)) {
                response.put("success", false);
                response.put("message", "Acceso denegado");
                return ResponseEntity.status(403).body(response);
            }
            
            Resena resena = resenaServicio.responderResena(resenaId, respuesta);
            
            response.put("success", true);
            response.put("message", "Respuesta agregada exitosamente");
            response.put("resena", resena);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al responder reseña: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Crear reseña general para múltiples productos
     */
    @PostMapping("/crear-multiple")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearResenaMultiple(
            @RequestParam(required = false) String alquilerId,
            @RequestParam(required = false) Integer calificacion,
            @RequestParam(required = false) String comentario,
            @RequestParam(value = "productosIds", required = false) List<String> productosIds,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Debug: Imprimir parámetros recibidos
            System.out.println("=== DEBUG CREAR RESEÑA MÚLTIPLE ===");
            System.out.println("alquilerId: " + alquilerId);
            System.out.println("calificacion: " + calificacion);
            System.out.println("comentario: " + comentario);
            System.out.println("productosIds: " + productosIds);
            System.out.println("productosIds size: " + (productosIds != null ? productosIds.size() : "null"));
            
            // Verificar parámetros requeridos
            if (alquilerId == null || alquilerId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Required parameter 'alquilerId' is not present.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (calificacion == null) {
                response.put("success", false);
                response.put("message", "Required parameter 'calificacion' is not present.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (comentario == null || comentario.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Required parameter 'comentario' is not present.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (productosIds == null || productosIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "Required parameter 'productosIds' is not present.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar sesión
            String usuarioId = (String) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión para dejar una reseña");
                return ResponseEntity.status(401).body(response);
            }
            
            // Validar calificación
            if (calificacion < 1 || calificacion > 5) {
                response.put("success", false);
                response.put("message", "La calificación debe estar entre 1 y 5 estrellas");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar comentario
            if (comentario == null || comentario.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "El comentario no puede estar vacío");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar que haya productos
            if (productosIds == null || productosIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "Debe especificar al menos un producto");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Crear una reseña para cada producto con la misma calificación y comentario
            List<Resena> resenasCreadas = resenaServicio.crearResenaMultiple(
                alquilerId, usuarioId, productosIds, calificacion, comentario);
            
            response.put("success", true);
            response.put("message", "¡Gracias por tu reseña! Será revisada antes de publicarse.");
            response.put("resenasCreadas", resenasCreadas.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al crear las reseñas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Aprobar todas las reseñas pendientes (Admin)
     */
    @PostMapping("/admin/aprobar-todas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aprobarTodasLasResenas(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que sea administrador
            String rol = (String) session.getAttribute("usuarioRol");
            if (!"ADMIN".equals(rol)) {
                response.put("success", false);
                response.put("message", "Acceso denegado");
                return ResponseEntity.status(403).body(response);
            }
            
            List<Resena> resenasPendientes = resenaServicio.obtenerResenasPendientes();
            int aprobadas = 0;
            
            for (Resena resena : resenasPendientes) {
                try {
                    resenaServicio.aprobarResena(resena.getId());
                    aprobadas++;
                } catch (Exception e) {
                    System.err.println("Error al aprobar reseña " + resena.getId() + ": " + e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", "Se aprobaron " + aprobadas + " reseña" + (aprobadas != 1 ? "s" : ""));
            response.put("aprobadas", aprobadas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al aprobar reseñas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Rechazar todas las reseñas pendientes (Admin)
     */
    @PostMapping("/admin/rechazar-todas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rechazarTodasLasResenas(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que sea administrador
            String rol = (String) session.getAttribute("usuarioRol");
            if (!"ADMIN".equals(rol)) {
                response.put("success", false);
                response.put("message", "Acceso denegado");
                return ResponseEntity.status(403).body(response);
            }
            
            List<Resena> resenasPendientes = resenaServicio.obtenerResenasPendientes();
            int rechazadas = 0;
            
            for (Resena resena : resenasPendientes) {
                try {
                    resenaServicio.rechazarResena(resena.getId());
                    rechazadas++;
                } catch (Exception e) {
                    System.err.println("Error al rechazar reseña " + resena.getId() + ": " + e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", "Se rechazaron " + rechazadas + " reseña" + (rechazadas != 1 ? "s" : ""));
            response.put("rechazadas", rechazadas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al rechazar reseñas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Crear reseñas individuales para múltiples productos
     */
    @PostMapping("/crear-individuales")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearResenasIndividuales(
            @RequestBody ResenaIndividualDTO requestData,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar sesión
            String usuarioId = (String) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión para dejar una reseña");
                return ResponseEntity.status(401).body(response);
            }
            
            // Obtener datos del request
            String alquilerId = requestData.getAlquilerId();
            List<ResenaIndividualDTO.ResenaData> resenas = requestData.getResenas();
            
            // Validar que haya reseñas
            if (resenas == null || resenas.isEmpty()) {
                response.put("success", false);
                response.put("message", "Debe proporcionar al menos una reseña");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Crear cada reseña individual
            int resenasCreadas = 0;
            for (ResenaIndividualDTO.ResenaData resenaData : resenas) {
                String productoId = resenaData.getProductoId();
                Integer calificacion = resenaData.getCalificacion();
                String comentario = resenaData.getComentario();
                
                // Validaciones
                if (calificacion == null || calificacion < 1 || calificacion > 5) {
                    response.put("success", false);
                    response.put("message", "Todas las calificaciones deben estar entre 1 y 5 estrellas");
                    return ResponseEntity.badRequest().body(response);
                }
                
                if (comentario == null || comentario.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Todos los comentarios son obligatorios");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // Crear la reseña
                resenaServicio.crearResena(alquilerId, usuarioId, productoId, calificacion, comentario);
                resenasCreadas++;
            }
            
            response.put("success", true);
            response.put("message", "¡Gracias por tus " + resenasCreadas + " reseñas! Serán revisadas antes de publicarse.");
            response.put("resenasCreadas", resenasCreadas);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al crear las reseñas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
