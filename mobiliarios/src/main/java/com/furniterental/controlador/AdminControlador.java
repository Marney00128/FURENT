package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.modelo.Producto;
import com.furniterental.modelo.Alquiler;
import com.furniterental.modelo.ItemCarrito;
import com.furniterental.modelo.Categoria;
import com.furniterental.modelo.Resena;
import com.furniterental.modelo.ActivityLog;
import com.furniterental.repositorio.UsuarioRepositorio;
import com.furniterental.repositorio.ProductoRepositorio;
import com.furniterental.repositorio.AlquilerRepositorio;
import com.furniterental.repositorio.ResenaRepositorio;
import com.furniterental.repositorio.ActivityLogRepositorio;
import com.furniterental.servicio.CategoriaServicio;
import com.furniterental.servicio.ActivityLogServicio;
import com.furniterental.servicio.EmailServicio;
import com.furniterental.servicio.AlquilerEstadoValidacionServicio;
import com.furniterental.servicio.CsrfTokenServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    @Autowired
    private AlquilerRepositorio alquilerRepositorio;
    
    @Autowired
    private CategoriaServicio categoriaServicio;
    
    @Autowired
    private ActivityLogServicio activityLogServicio;
    
    @Autowired
    private EmailServicio emailServicio;
    
    @Autowired
    private AlquilerEstadoValidacionServicio estadoValidacionServicio;
    
    @Autowired
    private CsrfTokenServicio csrfTokenServicio;
    
    @Autowired
    private ResenaRepositorio resenaRepositorio;
    
    @Autowired
    private ActivityLogRepositorio activityLogRepositorio;

    // Verificar si el usuario es administrador
    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
    
    // Validar token CSRF
    private boolean validarCsrf(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return csrfTokenServicio.validarToken(session, token);
    }

    // Dashboard principal
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        // Estadísticas
        long totalUsuarios = usuarioRepositorio.count();
        long totalProductos = productoRepositorio.count();
        long totalAdmins = usuarioRepositorio.countByRol("ADMIN");
        long totalUsers = usuarioRepositorio.countByRol("USER");

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalUsers", totalUsers);

        // Últimos usuarios registrados (obtener todos y tomar los últimos 5)
        List<Usuario> todosUsuarios = usuarioRepositorio.findAll();
        List<Usuario> ultimosUsuarios = todosUsuarios.size() > 5 
            ? todosUsuarios.subList(Math.max(0, todosUsuarios.size() - 5), todosUsuarios.size())
            : todosUsuarios;
        model.addAttribute("ultimosUsuarios", ultimosUsuarios);
        
        // Actividad reciente (últimos 10 logs)
        List<ActivityLog> actividadReciente = activityLogRepositorio.findTop10ByOrderByTimestampDesc();
        model.addAttribute("actividadReciente", actividadReciente);
        
        // Estadísticas de actividad por tipo de acción
        List<ActivityLog> todosLogs = activityLogRepositorio.findAll();
        long totalLogins = todosLogs.stream().filter(log -> "LOGIN".equals(log.getAction())).count();
        long totalCreaciones = todosLogs.stream().filter(log -> "CREATE".equals(log.getAction())).count();
        long totalActualizaciones = todosLogs.stream().filter(log -> "UPDATE".equals(log.getAction())).count();
        long totalEliminaciones = todosLogs.stream().filter(log -> "DELETE".equals(log.getAction())).count();
        long totalAlquileres = todosLogs.stream().filter(log -> "RENTAL".equals(log.getAction())).count();
        long totalPagos = todosLogs.stream().filter(log -> "PAYMENT".equals(log.getAction())).count();
        
        model.addAttribute("totalLogins", totalLogins);
        model.addAttribute("totalCreaciones", totalCreaciones);
        model.addAttribute("totalActualizaciones", totalActualizaciones);
        model.addAttribute("totalEliminaciones", totalEliminaciones);
        model.addAttribute("totalAlquileresLog", totalAlquileres);
        model.addAttribute("totalPagos", totalPagos);

        return "admin/dashboard";
    }

    // Gestión de Usuarios
    @GetMapping("/usuarios")
    public String usuarios(HttpSession session, Model model, RedirectAttributes redirectAttributes,
                          jakarta.servlet.http.HttpServletResponse response) {
        System.out.println("=== ACCEDIENDO A /admin/usuarios ===");
        
        // Configurar headers anti-caché
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        if (!esAdmin(session)) {
            System.out.println("ERROR: Usuario no es admin");
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        System.out.println("Usuario es admin, obteniendo lista de usuarios...");
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        System.out.println("Total usuarios encontrados: " + usuarios.size());
        
        // Contar usuarios por rol
        long totalUsers = usuarioRepositorio.countByRol("USER");
        long totalAdmins = usuarioRepositorio.countByRol("ADMIN");
        
        // Imprimir información de sesión para debug
        System.out.println("Usuario en sesión: " + session.getAttribute("usuarioNombre"));
        System.out.println("Rol en sesión: " + session.getAttribute("usuarioRol"));
        System.out.println("Total Users: " + totalUsers);
        System.out.println("Total Admins: " + totalAdmins);
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAdmins", totalAdmins);
        System.out.println("Retornando vista admin/usuarios");
        return "admin/usuarios";
    }

    // Eliminar usuario
    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        if (id == null || id.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ID de usuario inválido");
            return "redirect:/admin/usuarios";
        }

        try {
            // Obtener datos del usuario antes de eliminarlo
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String nombreUsuario = usuario.getNombre();
                
                // Eliminar usuario
                usuarioRepositorio.deleteById(id);
                
                // Registrar log de eliminación
                activityLogServicio.logDelete(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    nombreUsuario,
                    "USUARIOS"
                );
                
                redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    // Cambiar rol de usuario
    @PostMapping("/usuarios/cambiar-rol/{id}")
    public String cambiarRol(@PathVariable String id, @RequestParam String nuevoRol, 
                            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        if (id == null || id.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ID de usuario inválido");
            return "redirect:/admin/usuarios";
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String rolAnterior = usuario.getRol();
                usuario.setRol(nuevoRol);
                usuarioRepositorio.save(usuario);
                
                // Registrar log de actualización
                activityLogServicio.logUpdate(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    usuario.getNombre(),
                    "USUARIOS",
                    "Cambió rol de " + rolAnterior + " a " + nuevoRol
                );
                
                redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar rol: " + e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    // Obtener detalles de usuario (API REST)
    @GetMapping("/usuarios/{id}/detalles")
    @ResponseBody
    public java.util.Map<String, Object> obtenerDetallesUsuario(@PathVariable @NonNull String id, HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("error", "No tienes permisos de administrador");
            return response;
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                response.put("id", usuario.getId());
                response.put("nombre", usuario.getNombre());
                response.put("correo", usuario.getCorreo());
                response.put("telefono", usuario.getTelefono());
                response.put("rol", usuario.getRol());
                response.put("ultimoInicioSesion", usuario.getUltimoInicioSesion() != null ? usuario.getUltimoInicioSesion().toString() : "Nunca");
                
                // Contar alquileres del usuario
                long totalAlquileres = alquilerRepositorio.countByUsuarioId(id);
                response.put("totalAlquileres", totalAlquileres);
            } else {
                response.put("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            response.put("error", "Error al obtener detalles: " + e.getMessage());
        }

        return response;
    }

    // Gestión de Productos
    @GetMapping("/productos")
    public String productos(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        List<Producto> productos = productoRepositorio.findAll();
        List<Categoria> categorias = categoriaServicio.obtenerTodasCategorias();
        
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        return "admin/productos";
    }

    // Eliminar producto
    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        if (id == null || id.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ID de producto inválido");
            return "redirect:/admin/productos";
        }

        try {
            // Obtener datos del producto antes de eliminarlo
            Optional<Producto> productoOpt = productoRepositorio.findById(id);
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                String nombreProducto = producto.getNombreProducto();
                
                // Eliminar producto
                productoRepositorio.deleteById(id);
                
                // Registrar log de eliminación
                activityLogServicio.logDelete(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    nombreProducto,
                    "PRODUCTOS"
                );
                
                redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // Cambiar estado de producto
    @PostMapping("/productos/cambiar-estado/{id}")
    public String cambiarEstadoProducto(@PathVariable String id, 
                                       @RequestParam String nuevoEstado,
                                       HttpSession session, 
                                       RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        if (id == null || id.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ID de producto inválido");
            return "redirect:/admin/productos";
        }

        try {
            Optional<Producto> productoOpt = productoRepositorio.findById(id);
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                String estadoAnterior = producto.getEstado() != null ? producto.getEstado() : "SIN ESTADO";
                producto.setEstado(nuevoEstado);
                productoRepositorio.save(producto);
                
                // Registrar log de cambio de estado
                activityLogServicio.logUpdate(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    producto.getNombreProducto(),
                    "PRODUCTOS",
                    "Cambió estado de " + estadoAnterior + " a " + nuevoEstado
                );
                
                redirectAttributes.addFlashAttribute("mensaje", "Estado del producto actualizado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // Eliminar múltiples productos
    @PostMapping("/productos/eliminar-multiples")
    public String eliminarMultiplesProductos(@RequestParam("ids") List<String> ids, 
                                            HttpSession session, 
                                            RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se seleccionaron productos para eliminar");
            return "redirect:/admin/productos";
        }

        try {
            int eliminados = 0;
            for (String id : ids) {
                if (id != null && !id.trim().isEmpty()) {
                    productoRepositorio.deleteById(id);
                    eliminados++;
                }
            }
            redirectAttributes.addFlashAttribute("mensaje", 
                eliminados + " producto(s) eliminado(s) exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al eliminar productos: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // Agregar producto
    @GetMapping("/productos/nuevo")
    public String nuevoProducto(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }
        return "admin/producto-form";
    }

    // Guardar producto (nuevo o editar)
    @PostMapping("/productos/guardar")
    public String guardarProducto(@RequestParam(required = false) String id,
                                 @RequestParam(required = false) String nombreProducto,
                                 @RequestParam(required = false) String descripcionProducto,
                                 @RequestParam(required = false) Double precioProducto,
                                 @RequestParam(required = false) String categoriaProducto,
                                 @RequestParam(required = false) String imagenProducto,
                                 @RequestParam(required = false) Integer stock,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        // Validar campos requeridos
        if (nombreProducto == null || nombreProducto.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre del producto es requerido");
            return "redirect:/admin/productos";
        }
        if (descripcionProducto == null || descripcionProducto.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La descripción del producto es requerida");
            return "redirect:/admin/productos";
        }
        if (precioProducto == null || precioProducto <= 0) {
            redirectAttributes.addFlashAttribute("error", "El precio debe ser mayor a 0");
            return "redirect:/admin/productos";
        }
        if (categoriaProducto == null || categoriaProducto.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La categoría es requerida");
            return "redirect:/admin/productos";
        }
        if (stock == null || stock < 0) {
            redirectAttributes.addFlashAttribute("error", "El stock debe ser 0 o mayor");
            return "redirect:/admin/productos";
        }

        try {
            Producto producto;
            boolean esNuevo = (id == null || id.trim().isEmpty());
            String accionLog = "";
            
            // Si hay ID, es una edición - buscar el producto existente
            if (!esNuevo && id != null) {
                Optional<Producto> productoExistente = productoRepositorio.findById(id);
                if (productoExistente.isPresent()) {
                    producto = productoExistente.get();
                    accionLog = "UPDATE";
                    redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/admin/productos";
                }
            } else {
                // Si no hay ID, es un producto nuevo
                producto = new Producto();
                accionLog = "CREATE";
                redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
                
                // Validar que los productos nuevos tengan imagen
                if (imagenProducto == null || imagenProducto.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "La imagen es requerida para productos nuevos");
                    return "redirect:/admin/productos";
                }
            }
            
            // Actualizar los datos del producto
            producto.setNombreProducto(nombreProducto);
            producto.setDescripcionProducto(descripcionProducto);
            producto.setPrecioProducto(precioProducto);
            producto.setCategoriaProducto(categoriaProducto);
            
            // Solo actualizar la imagen si se proporciona una nueva
            if (imagenProducto != null && !imagenProducto.trim().isEmpty()) {
                producto.setImagenProducto(imagenProducto);
            }
            
            producto.setStock(stock);

            productoRepositorio.save(producto);
            
            // Registrar log
            if ("CREATE".equals(accionLog)) {
                activityLogServicio.logCreate(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    nombreProducto,
                    "PRODUCTOS"
                );
            } else {
                activityLogServicio.logUpdate(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    nombreProducto,
                    "PRODUCTOS",
                    "Actualizó información del producto"
                );
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar producto: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }
    
    // Ver pedidos por usuario (Admin)
    @GetMapping("/pedidos")
    public String verPedidosAdmin(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        // Obtener todos los alquileres agrupados por usuario
        List<Alquiler> todosAlquileres = alquilerRepositorio.findAllByOrderByFechaAlquilerDesc();
        
        // Crear un mapa de usuarios con sus alquileres
        java.util.Map<String, java.util.List<Alquiler>> alquileresPorUsuario = new java.util.LinkedHashMap<>();
        
        for (Alquiler alquiler : todosAlquileres) {
            String usuarioKey = alquiler.getUsuarioId() + "|" + alquiler.getUsuarioNombre() + "|" + alquiler.getUsuarioCorreo();
            alquileresPorUsuario.computeIfAbsent(usuarioKey, k -> new java.util.ArrayList<>()).add(alquiler);
        }
        
        model.addAttribute("alquileresPorUsuario", alquileresPorUsuario);
        
        // Agregar token CSRF para protección
        String csrfToken = csrfTokenServicio.obtenerToken(session);
        model.addAttribute("csrfToken", csrfToken);

        return "admin/pedidos";
    }
    
    // Cambiar estado de alquiler
    @PostMapping("/alquiler/cambiar-estado")
    @ResponseBody
    public java.util.Map<String, Object> cambiarEstadoAlquiler(
            @RequestParam String alquilerId,
            @RequestParam String nuevoEstado,
            @RequestParam(required = false) String csrfToken,
            HttpSession session,
            HttpServletRequest request) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos de administrador");
            return response;
        }
        
        // Validar token CSRF
        if (!validarCsrf(session, csrfToken)) {
            response.put("success", false);
            response.put("message", "Token de seguridad inválido. Por favor, recarga la página.");
            return response;
        }
        
        // Validar que alquilerId no sea nulo
        if (alquilerId == null || alquilerId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "ID de alquiler inválido");
            return response;
        }
        
        try {
            java.util.Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            
            if (alquilerOpt.isPresent()) {
                Alquiler alquiler = alquilerOpt.get();
                String estadoAnterior = alquiler.getEstado();
                
                // Validar que la transición de estado sea válida
                if (!estadoValidacionServicio.esTransicionValida(estadoAnterior, nuevoEstado)) {
                    String mensajeError = estadoValidacionServicio.obtenerMensajeError(estadoAnterior, nuevoEstado);
                    response.put("success", false);
                    response.put("message", mensajeError);
                    return response;
                }
                
                // Si se cambia a CANCELADO, devolver stock
                if ("CANCELADO".equals(nuevoEstado) && !"CANCELADO".equals(estadoAnterior)) {
                    for (ItemCarrito item : alquiler.getItems()) {
                        String productoId = item.getProductoId();
                        if (productoId != null) {
                            Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
                            if (productoOpt.isPresent()) {
                                Producto producto = productoOpt.get();
                                producto.setStock(producto.getStock() + item.getCantidad());
                                productoRepositorio.save(producto);
                                System.out.println(">>> Stock devuelto para " + producto.getNombreProducto() + 
                                                 ": +" + item.getCantidad() + " (nuevo stock: " + producto.getStock() + ")");
                            }
                        }
                    }
                }
                
                alquiler.setEstado(nuevoEstado);
                
                // Si el estado cambia a CONFIRMADO, calcular montos de pago (50% y 50%)
                if ("CONFIRMADO".equals(nuevoEstado)) {
                    alquiler.calcularMontosPago();
                    System.out.println(">>> Montos de pago calculados para alquiler " + alquilerId + 
                                     ": Parcial=$" + alquiler.getMontoPagoParcial() + 
                                     ", Final=$" + alquiler.getMontoSaldoPendiente());
                }
                
                alquilerRepositorio.save(alquiler);
                
                // Registrar log de cambio de estado
                String adminId = (String) session.getAttribute("usuarioId");
                String adminNombre = (String) session.getAttribute("usuarioNombre");
                String adminCorreo = (String) session.getAttribute("usuarioCorreo");
                
                if (adminId != null && adminNombre != null && adminCorreo != null) {
                    activityLogServicio.logStatusChange(
                        adminId,
                        adminNombre,
                        adminCorreo,
                        alquilerId,
                        estadoAnterior,
                        nuevoEstado,
                        alquiler.getUsuarioNombre() + " (" + alquiler.getUsuarioCorreo() + ")",
                        request
                    );
                }
                
                // Enviar notificación por correo al usuario
                try {
                    String detallesAlquiler = construirDetallesAlquiler(alquiler);
                    emailServicio.enviarNotificacionCambioEstado(
                        alquiler.getUsuarioCorreo(),
                        alquiler.getUsuarioNombre(),
                        alquilerId,
                        estadoAnterior,
                        nuevoEstado,
                        detallesAlquiler
                    );
                    System.out.println("Notificación enviada a: " + alquiler.getUsuarioCorreo());
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación por correo: " + e.getMessage());
                    // No fallar la operación si el correo falla
                }
                
                response.put("success", true);
                response.put("message", "Estado actualizado correctamente");
                response.put("nuevoEstado", nuevoEstado);
            } else {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el estado: " + e.getMessage());
        }
        
        return response;
    }
    
    // Obtener estados permitidos para un alquiler
    @GetMapping("/alquiler/estados-permitidos")
    @ResponseBody
    public java.util.Map<String, Object> obtenerEstadosPermitidos(
            @RequestParam String estadoActual,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos de administrador");
            return response;
        }
        
        try {
            List<String> estadosPermitidos = estadoValidacionServicio.obtenerEstadosPermitidos(estadoActual);
            String siguienteEstado = estadoValidacionServicio.obtenerSiguienteEstado(estadoActual);
            boolean esEstadoFinal = estadoValidacionServicio.esEstadoFinal(estadoActual);
            
            response.put("success", true);
            response.put("estadoActual", estadoActual);
            response.put("estadosPermitidos", estadosPermitidos);
            response.put("siguienteEstado", siguienteEstado);
            response.put("esEstadoFinal", esEstadoFinal);
            response.put("flujoCompleto", estadoValidacionServicio.obtenerFlujoCompleto());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener estados permitidos: " + e.getMessage());
        }
        
        return response;
    }
    
    // Eliminar alquiler
    @PostMapping("/alquiler/eliminar-pedido")
    @ResponseBody
    public java.util.Map<String, Object> eliminarAlquiler(
            @RequestParam String alquilerId,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos de administrador");
            return response;
        }
        
        // Validar que alquilerId no sea nulo
        if (alquilerId == null || alquilerId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "ID de alquiler inválido");
            return response;
        }
        
        try {
            java.util.Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            
            if (alquilerOpt.isPresent()) {
                Alquiler alquiler = alquilerOpt.get();
                
                // Devolver stock antes de eliminar
                if (!"CANCELADO".equals(alquiler.getEstado())) {
                    for (ItemCarrito item : alquiler.getItems()) {
                        String productoId = item.getProductoId();
                        if (productoId != null) {
                            Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
                            if (productoOpt.isPresent()) {
                                Producto producto = productoOpt.get();
                                producto.setStock(producto.getStock() + item.getCantidad());
                                productoRepositorio.save(producto);
                                System.out.println(">>> Stock devuelto al eliminar alquiler para " + producto.getNombreProducto() + 
                                                 ": +" + item.getCantidad() + " (nuevo stock: " + producto.getStock() + ")");
                            }
                        }
                    }
                }
                
                // Registrar log de eliminación antes de borrar
                String usuarioId = (String) session.getAttribute("usuarioId");
                String usuarioNombre = (String) session.getAttribute("usuarioNombre");
                String usuarioCorreo = (String) session.getAttribute("usuarioCorreo");
                
                if (usuarioId != null && usuarioNombre != null && usuarioCorreo != null) {
                    activityLogServicio.logDelete(
                        usuarioId,
                        usuarioNombre,
                        usuarioCorreo,
                        "Alquiler #" + alquilerId + " - Usuario: " + alquiler.getUsuarioNombre(),
                        "ALQUILERES"
                    );
                }
                
                // Eliminar el alquiler
                alquilerRepositorio.deleteById(alquilerId);
                
                response.put("success", true);
                response.put("message", "Alquiler eliminado correctamente");
            } else {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el alquiler: " + e.getMessage());
        }
        
        return response;
    }
    
    // ========== GESTIÓN DE CATEGORÍAS ==========
    
    // Listar categorías
    @GetMapping("/categorias")
    public String categorias(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        List<Categoria> categorias = categoriaServicio.obtenerTodasCategorias();
        long totalCategorias = categoriaServicio.contarCategorias();
        
        model.addAttribute("categorias", categorias);
        model.addAttribute("totalCategorias", totalCategorias);
        
        return "admin/categorias";
    }
    
    // Crear categoría
    @PostMapping("/categorias/crear")
    public String crearCategoria(@RequestParam @NonNull String nombre,
                                @RequestParam @NonNull String descripcion,
                                @RequestParam(required = false, defaultValue = "bi-tag") String icono,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        try {
            // Verificar si la categoría ya existe
            if (categoriaServicio.existeCategoria(nombre)) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una categoría con ese nombre");
                return "redirect:/admin/categorias";
            }
            
            Categoria categoria = new Categoria(nombre, descripcion, icono);
            categoriaServicio.crearCategoria(categoria);
            
            // Registrar log de creación
            activityLogServicio.logCreate(
                (String) session.getAttribute("usuarioId"),
                (String) session.getAttribute("usuarioNombre"),
                (String) session.getAttribute("usuarioCorreo"),
                nombre,
                "CATEGORIAS"
            );
            
            redirectAttributes.addFlashAttribute("mensaje", "Categoría creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear categoría: " + e.getMessage());
        }

        return "redirect:/admin/categorias";
    }
    
    // Editar categoría
    @PostMapping("/categorias/editar/{id}")
    public String editarCategoria(@PathVariable @NonNull String id,
                                 @RequestParam String nombre,
                                 @RequestParam String descripcion,
                                 @RequestParam(required = false, defaultValue = "bi-tag") String icono,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        try {
            // Obtener la categoría anterior para el log
            Optional<Categoria> categoriaAnterior = categoriaServicio.obtenerCategoriaPorId(id);
            String nombreAnterior = categoriaAnterior.isPresent() ? categoriaAnterior.get().getNombre() : "Desconocido";
            
            Categoria categoriaActualizada = new Categoria(nombre, descripcion, icono);
            Categoria resultado = categoriaServicio.actualizarCategoria(id, categoriaActualizada);
            
            if (resultado != null) {
                // Registrar log de actualización
                activityLogServicio.logUpdate(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    nombre,
                    "CATEGORIAS",
                    "Actualizó categoría '" + nombreAnterior + "'"
                );
                
                redirectAttributes.addFlashAttribute("mensaje", "Categoría actualizada exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar categoría: " + e.getMessage());
        }

        return "redirect:/admin/categorias";
    }
    
    // Eliminar categoría
    @PostMapping("/categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable @NonNull String id, 
                                   HttpSession session, 
                                   RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        try {
            // Obtener datos de la categoría antes de eliminarla
            Optional<Categoria> categoriaOpt = categoriaServicio.obtenerCategoriaPorId(id);
            String nombreCategoria = categoriaOpt.isPresent() ? categoriaOpt.get().getNombre() : "Desconocido";
            
            boolean eliminado = categoriaServicio.eliminarCategoria(id);
            if (eliminado) {
                // Registrar log de eliminación
                activityLogServicio.logDelete(
                    (String) session.getAttribute("usuarioId"),
                    (String) session.getAttribute("usuarioNombre"),
                    (String) session.getAttribute("usuarioCorreo"),
                    nombreCategoria,
                    "CATEGORIAS"
                );
                
                redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar la categoría porque tiene productos asociados");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar categoría: " + e.getMessage());
        }

        return "redirect:/admin/categorias";
    }
    
    // Ver productos de una categoría
    @GetMapping("/categorias/{id}/productos")
    public String verProductosCategoria(@PathVariable @NonNull String id,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }

        try {
            Optional<Categoria> categoriaOpt = categoriaServicio.obtenerCategoriaPorId(id);
            if (categoriaOpt.isPresent()) {
                Categoria categoria = categoriaOpt.get();
                List<Producto> productos = productoRepositorio.findByCategoriaProducto(categoria.getNombre());
                
                model.addAttribute("categoria", categoria);
                model.addAttribute("productos", productos);
                model.addAttribute("totalProductos", productos.size());
                
                return "admin/productos-categoria";
            } else {
                redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
                return "redirect:/admin/categorias";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar productos: " + e.getMessage());
            return "redirect:/admin/categorias";
        }
    }
    
    /**
     * Construye los detalles del alquiler para el correo electrónico
     */
    private String construirDetallesAlquiler(Alquiler alquiler) {
        StringBuilder detalles = new StringBuilder();
        
        if (alquiler.getItems() != null && !alquiler.getItems().isEmpty()) {
            detalles.append("<strong>Productos alquilados:</strong><br>");
            for (var item : alquiler.getItems()) {
                detalles.append("• ").append(item.getNombreProducto())
                       .append(" (x").append(item.getCantidad()).append(")")
                       .append(" - $").append(String.format("%.2f", item.getSubtotal()))
                       .append("<br>");
            }
        }
        
        detalles.append("<br><strong>Total:</strong> $")
               .append(String.format("%.2f", alquiler.getTotal()));
        
        if (alquiler.getFechaInicio() != null) {
            detalles.append("<br><strong>Fecha de inicio:</strong> ")
                   .append(alquiler.getFechaInicio().toLocalDate());
        }
        
        if (alquiler.getFechaFin() != null) {
            detalles.append("<br><strong>Fecha de fin:</strong> ")
                   .append(alquiler.getFechaFin().toLocalDate());
        }
        
        if (alquiler.getDireccionEntrega() != null && !alquiler.getDireccionEntrega().isEmpty()) {
            detalles.append("<br><strong>Dirección de entrega:</strong> ")
                   .append(alquiler.getDireccionEntrega());
        }
        
        return detalles.toString();
    }
    
    // Aceptar propuesta de transporte del usuario (Admin)
    @PostMapping("/alquiler/aceptar-transporte-usuario")
    @ResponseBody
    public java.util.Map<String, Object> aceptarCostoTransporteUsuario(
            @RequestParam String alquilerId,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos de administrador");
            return response;
        }
        
        // Validar alquilerId
        if (alquilerId == null || alquilerId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "ID de alquiler inválido");
            return response;
        }
        
        try {
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que hay una propuesta del usuario
            if (!"PROPUESTA_USUARIO".equals(alquiler.getEstadoTransporte())) {
                response.put("success", false);
                response.put("message", "No hay propuesta del usuario para aceptar");
                return response;
            }
            
            // Aceptar propuesta del usuario
            alquiler.setCostoTransporteAceptado(alquiler.getCostoTransporteUsuario());
            alquiler.setEstadoTransporte("ACEPTADO");
            alquiler.recalcularTotalConTransporte();
            
            alquilerRepositorio.save(alquiler);
            
            response.put("success", true);
            response.put("message", "Propuesta de transporte del usuario aceptada");
            response.put("costoTransporteAceptado", alquiler.getCostoTransporteAceptado());
            response.put("totalNuevo", alquiler.getTotal());
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al aceptar propuesta: " + e.getMessage());
            return response;
        }
    }
    
    // Rechazar propuesta de transporte del usuario y contraproponer (Admin)
    @PostMapping("/alquiler/contraproponer-transporte")
    @ResponseBody
    public java.util.Map<String, Object> contraproponerCostoTransporte(
            @RequestParam String alquilerId,
            @RequestParam Double costoTransporte,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos de administrador");
            return response;
        }
        
        // Validar alquilerId
        if (alquilerId == null || alquilerId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "ID de alquiler inválido");
            return response;
        }
        
        // Validar costo
        if (costoTransporte == null || costoTransporte < 0) {
            response.put("success", false);
            response.put("message", "El costo de transporte debe ser mayor o igual a 0");
            return response;
        }
        
        try {
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Contraproponer costo de transporte
            alquiler.setCostoTransporteAdmin(costoTransporte);
            alquiler.setEstadoTransporte("PROPUESTA_ADMIN");
            alquiler.setQuienPropuso("ADMIN");
            alquiler.setFechaPropuestaTransporte(LocalDateTime.now());
            
            alquilerRepositorio.save(alquiler);
            
            response.put("success", true);
            response.put("message", "Contrapropuesta enviada al usuario");
            response.put("costoTransporte", costoTransporte);
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al contraproponer: " + e.getMessage());
            return response;
        }
    }
    
    // Rechazar propuesta de transporte del usuario sin contraproponer (Admin)
    @PostMapping("/alquiler/rechazar-transporte-usuario")
    @ResponseBody
    public java.util.Map<String, Object> rechazarCostoTransporteUsuario(
            @RequestParam String alquilerId,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos de administrador");
            return response;
        }
        
        // Validar alquilerId
        if (alquilerId == null || alquilerId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "ID de alquiler inválido");
            return response;
        }
        
        try {
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Rechazar propuesta del usuario
            alquiler.setEstadoTransporte("RECHAZADO");
            alquiler.setCostoTransporteUsuario(null);
            
            alquilerRepositorio.save(alquiler);
            
            response.put("success", true);
            response.put("message", "Propuesta de transporte rechazada");
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al rechazar propuesta: " + e.getMessage());
            return response;
        }
    }
    
    // Panel de Reportes Analíticos
    @GetMapping("/reportes-analytics")
    public String reportesAnalytics(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }
        
        // Obtener todos los alquileres
        List<Alquiler> alquileres = alquilerRepositorio.findAll();
        
        // Calcular métricas
        long totalAlquileres = alquileres.size();
        long alquileresCompletados = alquileres.stream()
            .filter(a -> "COMPLETADO".equals(a.getEstado()))
            .count();
        long alquileresCancelados = alquileres.stream()
            .filter(a -> "CANCELADO".equals(a.getEstado()))
            .count();
        
        // Calcular ingresos totales (solo alquileres completados)
        double ingresosTotales = alquileres.stream()
            .filter(a -> "COMPLETADO".equals(a.getEstado()))
            .mapToDouble(Alquiler::getTotal)
            .sum();
        
        // Tasa de cancelación
        double tasaCancelacion = totalAlquileres > 0 
            ? (alquileresCancelados * 100.0 / totalAlquileres) 
            : 0.0;
        
        // Productos más alquilados (contar items en alquileres)
        Map<String, Integer> productosContador = new HashMap<>();
        Map<String, String> productosNombres = new HashMap<>();
        
        for (Alquiler alquiler : alquileres) {
            if (alquiler.getItems() != null) {
                for (ItemCarrito item : alquiler.getItems()) {
                    String productoId = item.getProductoId();
                    String productoNombre = item.getNombreProducto();
                    productosContador.put(productoId, 
                        productosContador.getOrDefault(productoId, 0) + item.getCantidad());
                    productosNombres.put(productoId, productoNombre);
                }
            }
        }
        
        // Ordenar productos por cantidad y tomar top 5
        List<Map.Entry<String, Integer>> topProductos = productosContador.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(5)
            .collect(Collectors.toList());
        
        // Crear lista de productos con nombres
        List<Map<String, Object>> topProductosConNombres = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topProductos) {
            Map<String, Object> producto = new HashMap<>();
            producto.put("nombre", productosNombres.get(entry.getKey()));
            producto.put("cantidad", entry.getValue());
            topProductosConNombres.add(producto);
        }
        
        // Ingresos por mes (últimos 6 meses)
        LocalDate ahora = LocalDate.now();
        List<Map<String, Object>> ingresosPorMes = new ArrayList<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = ahora.minusMonths(i);
            int mesNum = mes.getMonthValue();
            int anio = mes.getYear();
            
            double ingresosMes = alquileres.stream()
                .filter(a -> "COMPLETADO".equals(a.getEstado()) && a.getFechaAlquiler() != null)
                .filter(a -> {
                    LocalDate fechaAlquiler = a.getFechaAlquiler().toLocalDate();
                    return fechaAlquiler.getMonthValue() == mesNum && fechaAlquiler.getYear() == anio;
                })
                .mapToDouble(Alquiler::getTotal)
                .sum();
            
            Map<String, Object> mesData = new HashMap<>();
            mesData.put("mes", mes.getMonth().toString().substring(0, 3));
            mesData.put("ingresos", ingresosMes);
            ingresosPorMes.add(mesData);
        }
        
        // Nivel de satisfacción (calculado desde reseñas aprobadas)
        List<Resena> resenasAprobadas = resenaRepositorio.findByEstado("APROBADA");
        double nivelSatisfaccion = 0.0;
        
        if (!resenasAprobadas.isEmpty()) {
            double sumaCalificaciones = resenasAprobadas.stream()
                .mapToInt(Resena::getCalificacion)
                .sum();
            nivelSatisfaccion = sumaCalificaciones / resenasAprobadas.size();
            // Redondear a 1 decimal
            nivelSatisfaccion = Math.round(nivelSatisfaccion * 10.0) / 10.0;
        } else {
            // Si no hay reseñas, mostrar 0.0
            nivelSatisfaccion = 0.0;
        }
        
        // Agregar datos al modelo
        model.addAttribute("totalAlquileres", totalAlquileres);
        model.addAttribute("alquileresCompletados", alquileresCompletados);
        model.addAttribute("alquileresCancelados", alquileresCancelados);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("tasaCancelacion", String.format("%.1f", tasaCancelacion));
        model.addAttribute("topProductos", topProductosConNombres);
        model.addAttribute("ingresosPorMes", ingresosPorMes);
        model.addAttribute("nivelSatisfaccion", nivelSatisfaccion);
        model.addAttribute("totalResenas", resenasAprobadas.size());
        
        return "admin/reportes-analytics";
    }
    
    // Endpoint AJAX para obtener ingresos por rango de tiempo
    @GetMapping("/ingresos-por-periodo")
    @ResponseBody
    public Map<String, Object> obtenerIngresosPorPeriodo(
            @RequestParam String periodo,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (!esAdmin(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos de administrador");
            return response;
        }
        
        List<Alquiler> alquileres = alquilerRepositorio.findAll();
        LocalDate ahora = LocalDate.now();
        List<Map<String, Object>> ingresosPorPeriodo = new ArrayList<>();
        
        switch (periodo.toUpperCase()) {
            case "SEMANA":
                // Últimos 7 días
                for (int i = 6; i >= 0; i--) {
                    LocalDate dia = ahora.minusDays(i);
                    
                    double ingresosDia = alquileres.stream()
                        .filter(a -> "COMPLETADO".equals(a.getEstado()) && a.getFechaAlquiler() != null)
                        .filter(a -> {
                            LocalDate fechaAlquiler = a.getFechaAlquiler().toLocalDate();
                            return fechaAlquiler.equals(dia);
                        })
                        .mapToDouble(Alquiler::getTotal)
                        .sum();
                    
                    Map<String, Object> diaData = new HashMap<>();
                    // Formato: Lun 18, Mar 19, etc.
                    String diaNombre = dia.getDayOfWeek().toString().substring(0, 3);
                    diaData.put("mes", diaNombre + " " + dia.getDayOfMonth());
                    diaData.put("ingresos", ingresosDia);
                    ingresosPorPeriodo.add(diaData);
                }
                break;
                
            case "MES":
                // Últimas 4 semanas (28 días agrupados por semana)
                for (int i = 3; i >= 0; i--) {
                    LocalDate inicioSemana = ahora.minusWeeks(i);
                    LocalDate finSemana = inicioSemana.plusDays(6);
                    
                    double ingresosSemana = alquileres.stream()
                        .filter(a -> "COMPLETADO".equals(a.getEstado()) && a.getFechaAlquiler() != null)
                        .filter(a -> {
                            LocalDate fechaAlquiler = a.getFechaAlquiler().toLocalDate();
                            return !fechaAlquiler.isBefore(inicioSemana) && !fechaAlquiler.isAfter(finSemana);
                        })
                        .mapToDouble(Alquiler::getTotal)
                        .sum();
                    
                    Map<String, Object> semanaData = new HashMap<>();
                    semanaData.put("mes", "Sem " + (i + 1));
                    semanaData.put("ingresos", ingresosSemana);
                    ingresosPorPeriodo.add(semanaData);
                }
                break;
                
            case "6MESES":
                // Últimos 6 meses
                for (int i = 5; i >= 0; i--) {
                    LocalDate mes = ahora.minusMonths(i);
                    int mesNum = mes.getMonthValue();
                    int anio = mes.getYear();
                    
                    double ingresosMes = alquileres.stream()
                        .filter(a -> "COMPLETADO".equals(a.getEstado()) && a.getFechaAlquiler() != null)
                        .filter(a -> {
                            LocalDate fechaAlquiler = a.getFechaAlquiler().toLocalDate();
                            return fechaAlquiler.getMonthValue() == mesNum && fechaAlquiler.getYear() == anio;
                        })
                        .mapToDouble(Alquiler::getTotal)
                        .sum();
                    
                    Map<String, Object> mesData = new HashMap<>();
                    mesData.put("mes", mes.getMonth().toString().substring(0, 3));
                    mesData.put("ingresos", ingresosMes);
                    ingresosPorPeriodo.add(mesData);
                }
                break;
                
            case "ANO":
                // Últimos 12 meses
                for (int i = 11; i >= 0; i--) {
                    LocalDate mes = ahora.minusMonths(i);
                    int mesNum = mes.getMonthValue();
                    int anio = mes.getYear();
                    
                    double ingresosMes = alquileres.stream()
                        .filter(a -> "COMPLETADO".equals(a.getEstado()) && a.getFechaAlquiler() != null)
                        .filter(a -> {
                            LocalDate fechaAlquiler = a.getFechaAlquiler().toLocalDate();
                            return fechaAlquiler.getMonthValue() == mesNum && fechaAlquiler.getYear() == anio;
                        })
                        .mapToDouble(Alquiler::getTotal)
                        .sum();
                    
                    Map<String, Object> mesData = new HashMap<>();
                    mesData.put("mes", mes.getMonth().toString().substring(0, 3));
                    mesData.put("ingresos", ingresosMes);
                    ingresosPorPeriodo.add(mesData);
                }
                break;
                
            default:
                response.put("success", false);
                response.put("message", "Periodo no válido");
                return response;
        }
        
        response.put("success", true);
        response.put("data", ingresosPorPeriodo);
        return response;
    }
    
    // Página de Configuración
    @GetMapping("/configuracion")
    public String configuracion(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }
        
        return "admin/configuracion";
    }
}