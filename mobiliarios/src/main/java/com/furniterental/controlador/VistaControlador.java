package com.furniterental.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.furniterental.repositorio.ProductoRepositorio;
import com.furniterental.modelo.Producto;
import com.furniterental.modelo.Usuario;
import com.furniterental.modelo.Categoria;
import com.furniterental.servicio.CategoriaServicio;
import com.furniterental.servicio.UsuarioServicio;
import com.furniterental.servicio.ActivityLogServicio;
import com.furniterental.servicio.EmailServicio;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class VistaControlador {

    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    @Autowired
    private CategoriaServicio categoriaServicio;
    
    @Autowired
    private UsuarioServicio usuarioServicio;
    
    @Autowired
    private ActivityLogServicio activityLogServicio;
    
    @Autowired
    private EmailServicio emailServicio;

    @GetMapping("/")
    public String index(Model model) {
        List<Producto> productos = productoRepositorio.findAll();
        
        // Ordenar productos: primero los que tienen stock, luego los agotados
        productos = productos.stream()
            .sorted(Comparator.comparing((Producto p) -> {
                Integer stock = p.getStock();
                return (stock == null || stock <= 0) ? 1 : 0; // 0 = con stock (primero), 1 = sin stock (último)
            }))
            .collect(Collectors.toList());
        
        List<Categoria> categorias = categoriaServicio.obtenerTodasCategorias();
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        return "index";  // Este archivo está en la raíz de templates
    }
    
    @GetMapping("/about")
    public String about() {
        return "pages/about";  // Este archivo está en la subcarpeta pages
    }
    
    @GetMapping("/store")
    public String store(Model model) {
        List<Producto> productos = productoRepositorio.findAll();
        
        // Ordenar productos: primero los que tienen stock, luego los agotados
        productos = productos.stream()
            .sorted(Comparator.comparing((Producto p) -> {
                Integer stock = p.getStock();
                return (stock == null || stock <= 0) ? 1 : 0; // 0 = con stock (primero), 1 = sin stock (último)
            }))
            .collect(Collectors.toList());
        
        List<Categoria> categorias = categoriaServicio.obtenerTodasCategorias();
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        return "pages/store";  // Este archivo está en la subcarpeta pages
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "pages/contact";  // Este archivo está en la subcarpeta pages
    }
    
    // Ruta /carrito movida a CarritoControlador para manejar la lógica del carrito
    
    @GetMapping("/user-account")
    public String userAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar si hay sesión activa
        if (session.getAttribute("usuario") == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para acceder a tu cuenta");
            return "redirect:/login";
        }
        return "pages/user-account";  // Este archivo está en la subcarpeta pages
    }
    
    // Las rutas /login y /register están en AuthControlador
    
    @GetMapping("/user-account/editar")
    public String editarPerfil(HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar si hay sesión activa
        if (session.getAttribute("usuario") == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para acceder a tu cuenta");
            return "redirect:/login";
        }
        return "pages/editar-perfil";
    }
    
    @PostMapping("/user-account/actualizar")
    public String actualizarPerfil(
            @RequestParam String nombre,
            @RequestParam String telefono,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        try {
            // usuarioId is guaranteed to be non-null after the check above
            @SuppressWarnings("null")
            Usuario usuarioActualizado = usuarioServicio.actualizarPerfil(usuarioId, nombre, telefono);
            
            if (usuarioActualizado != null) {
                // Actualizar la sesión con los nuevos datos
                session.setAttribute("usuario", usuarioActualizado);
                session.setAttribute("usuarioNombre", usuarioActualizado.getNombre());
                
                // Registrar log de actualización
                String usuarioCorreo = (String) session.getAttribute("usuarioCorreo");
                if (usuarioCorreo != null) {
                    activityLogServicio.logUpdate(
                        usuarioId,
                        nombre,
                        usuarioCorreo,
                        "Perfil de usuario",
                        "USUARIOS",
                        "Actualizó su información personal"
                    );
                }
                
                redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el perfil");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }
        
        return "redirect:/user-account";
    }
    
    @PostMapping("/user-account/solicitar-codigo-verificacion")
    public String solicitarCodigoVerificacion(
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        try {
            // usuarioId is guaranteed to be non-null after the check above
            boolean enviado = usuarioServicio.generarYEnviarCodigoVerificacion(usuarioId);
            
            if (enviado) {
                redirectAttributes.addFlashAttribute("mensajeContrasena", 
                    "Código de verificación enviado a tu correo. Revisa tu bandeja de entrada.");
            } else {
                redirectAttributes.addFlashAttribute("errorContrasena", 
                    "No se pudo enviar el código de verificación");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorContrasena", 
                "Error al enviar el código: " + e.getMessage());
        }
        
        return "redirect:/user-account/editar";
    }
    
    @PostMapping("/user-account/cambiar-contrasena")
    public String cambiarContrasena(
            @RequestParam String codigoVerificacion,
            @RequestParam(required = false) String codigo2FA,
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        // Validar que las contraseñas coincidan
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            redirectAttributes.addFlashAttribute("errorContrasena", "Las contraseñas no coinciden");
            return "redirect:/user-account/editar";
        }
        
        // Validar longitud mínima
        if (nuevaContrasena.length() < 6) {
            redirectAttributes.addFlashAttribute("errorContrasena", "La contraseña debe tener al menos 6 caracteres");
            return "redirect:/user-account/editar";
        }
        
        try {
            // Verificar si el usuario tiene 2FA habilitado
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario != null && usuario.isTwoFactorEnabled()) {
                // Si tiene 2FA, validar el código de Google Authenticator
                if (codigo2FA == null || codigo2FA.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorContrasena", "Debes ingresar el código de Google Authenticator");
                    return "redirect:/user-account/editar";
                }
                
                // Verificar el código 2FA
                boolean codigo2FAValido = usuarioServicio.verificarCodigoTwoFactor(usuarioId, codigo2FA);
                
                if (!codigo2FAValido) {
                    redirectAttributes.addFlashAttribute("errorContrasena", "Código de Google Authenticator inválido");
                    return "redirect:/user-account/editar";
                }
            }
            
            // Verificar el código de email y cambiar contraseña
            // usuarioId is guaranteed non-null after the check at line 203
            @SuppressWarnings("null")
            boolean actualizado = usuarioServicio.cambiarContrasenaConCodigo(usuarioId, codigoVerificacion, nuevaContrasena);
            
            if (actualizado) {
                // Registrar log de cambio de contraseña
                String usuarioNombre = (String) session.getAttribute("usuarioNombre");
                String usuarioCorreo = (String) session.getAttribute("usuarioCorreo");
                
                if (usuarioNombre != null && usuarioCorreo != null) {
                    String descripcion = usuario != null && usuario.isTwoFactorEnabled() 
                        ? "Cambió su contraseña usando código de email + Google Authenticator"
                        : "Cambió su contraseña usando código de verificación";
                    
                    activityLogServicio.logUpdate(
                        usuarioId,
                        usuarioNombre,
                        usuarioCorreo,
                        "Contraseña",
                        "USUARIOS",
                        descripcion
                    );
                    
                    // Enviar correo de confirmación de cambio de contraseña
                    emailServicio.enviarConfirmacionCambioContrasena(
                        usuarioCorreo,
                        usuarioNombre
                    );
                }
                
                redirectAttributes.addFlashAttribute("mensajeContrasena", "Contraseña actualizada exitosamente. Se ha enviado un correo de confirmación.");
            } else {
                redirectAttributes.addFlashAttribute("errorContrasena", "Código de email inválido o expirado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorContrasena", "Error al cambiar la contraseña: " + e.getMessage());
        }
        
        return "redirect:/user-account/editar";
    }
    
    /**
     * Vista de gestión de reseñas para administradores
     */
    @GetMapping("/admin/resenas")
    public String gestionResenas(HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar que haya sesión activa
        String usuarioId = (String) session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("usuarioRol");
        
        System.out.println("=== DEBUG VISTA RESENAS ===");
        System.out.println("Usuario ID: " + usuarioId);
        System.out.println("Rol: " + rol);
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        if (!"ADMIN".equals(rol)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Se requiere rol de administrador.");
            return "redirect:/admin/dashboard";
        }
        
        return "admin/resenas";
    }
    
    // Comentar o eliminar este método para evitar el conflicto de mapeo
    /*
    @GetMapping("/admin-store")
    public String adminStore(Model model) {
        try {
            // Obtener los productos del panel de administrador
            List<Producto> productosAdmin = productoService.getAllProductos();
            
            // Agregar los productos al modelo
            model.addAttribute("productosAdmin", productosAdmin);
            
            // Retornar la vista
            return "pages/admin-store";
        } catch (Exception e) {
            // Manejar cualquier error que pueda ocurrir
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
            return "pages/error";
        }
    }
    */
}
