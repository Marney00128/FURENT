package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import com.furniterental.servicio.ActivityLogServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Locale;
import java.security.MessageDigest;
import java.util.Base64;

@Controller
public class AuthControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private ActivityLogServicio activityLogServicio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.furniterental.servicio.UsuarioServicio usuarioServicio;
    
    // Método para validar la fortaleza de la contraseña
    private String validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }
        
        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;
        boolean tieneCaracterEspecial = false;
        
        for (char c : contrasena.toCharArray()) {
            if (Character.isUpperCase(c)) {
                tieneMayuscula = true;
            } else if (Character.isLowerCase(c)) {
                tieneMinuscula = true;
            } else if (Character.isDigit(c)) {
                tieneNumero = true;
            } else if (!Character.isLetterOrDigit(c)) {
                tieneCaracterEspecial = true;
            }
        }
        
        if (!tieneMayuscula) {
            return "La contraseña debe contener al menos una letra mayúscula";
        }
        if (!tieneMinuscula) {
            return "La contraseña debe contener al menos una letra minúscula";
        }
        if (!tieneNumero) {
            return "La contraseña debe contener al menos un número";
        }
        if (!tieneCaracterEspecial) {
            return "La contraseña debe contener al menos un carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)";
        }
        
        return null; // Contraseña válida
    }

    // Mostrar página de registro
    @GetMapping("/register")
    public String mostrarRegistro(HttpSession session) {
        Object usuarioId = session != null ? session.getAttribute("usuarioId") : null;
        if (usuarioId != null) {
            return "redirect:/";
        }
        return "pages/register";
    }

    // Procesar registro
    @PostMapping("/register")
    public String procesarRegistro(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String contrasena,
            @RequestParam String confirmarContrasena,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("=== INICIO REGISTRO ===");
            System.out.println("Nombre: " + nombre);
            System.out.println("Correo (input): " + correo);

            // Normalizar correo a minúsculas y sin espacios
            correo = correo != null ? correo.trim().toLowerCase(Locale.ROOT) : null;
            System.out.println("Teléfono: " + telefono);
            
            // Verificar que el correo sea válido (@gmail.com o admin@furent.com)
            if (correo == null || (!correo.endsWith("@gmail.com") && !correo.equals("admin@furent.com"))) {
                System.out.println("ERROR: Dominio de correo no permitido");
                redirectAttributes.addFlashAttribute("error", "Solo se permiten cuentas de Gmail");
                return "redirect:/register";
            }
            
            // Validar fortaleza de la contraseña
            String errorContrasena = validarContrasena(contrasena);
            if (errorContrasena != null) {
                System.out.println("ERROR: " + errorContrasena);
                redirectAttributes.addFlashAttribute("error", errorContrasena);
                return "redirect:/register";
            }
            
            // Validar que las contraseñas coincidan
            if (!contrasena.equals(confirmarContrasena)) {
                System.out.println("ERROR: Las contraseñas no coinciden");
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/register";
            }

            // Verificar si el correo ya está registrado
            Optional<Usuario> usuarioExistente = usuarioRepositorio.findByCorreo(correo);
            if (usuarioExistente.isPresent()) {
                System.out.println("ERROR: El correo ya está registrado");
                redirectAttributes.addFlashAttribute("error", "Este correo ya está registrado");
                return "redirect:/register";
            }

            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setTelefono(telefono);
            // Encriptar contraseña con BCrypt
            String contrasenaEncriptada = passwordEncoder.encode(contrasena);
            nuevoUsuario.setContrasena(contrasenaEncriptada);
            nuevoUsuario.setRol("USER"); // Rol por defecto
            
            System.out.println("Intentando guardar usuario en MongoDB...");
            
            // Guardar en MongoDB
            Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);
            
            System.out.println("Usuario guardado exitosamente con ID: " + usuarioGuardado.getId());
            
            // Registrar log de creación de usuario
            activityLogServicio.logCreate(
                usuarioGuardado.getId(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getCorreo(),
                "Usuario: " + usuarioGuardado.getNombre(),
                "USUARIOS"
            );
            
            System.out.println("=== FIN REGISTRO ===");

            redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión");
            // Pasar parámetros para guardar la cuenta en localStorage
            return "redirect:/login/new?registered=true&nombre=" + 
                   java.net.URLEncoder.encode(nombre, "UTF-8") + 
                   "&correo=" + java.net.URLEncoder.encode(correo, "UTF-8");

        } catch (Exception e) {
            System.out.println("ERROR AL REGISTRAR: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al registrar usuario: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // Mostrar selector de cuentas guardadas
    @GetMapping("/login")
    public String mostrarSelectorCuentas(HttpSession session) {
        Object usuarioId = session != null ? session.getAttribute("usuarioId") : null;
        if (usuarioId != null) {
            return "redirect:/";
        }
        return "pages/account-selector";
    }

    // Mostrar página de login con correo preseleccionado
    @GetMapping("/login/new")
    public String mostrarLoginNuevo(HttpSession session) {
        Object usuarioId = session != null ? session.getAttribute("usuarioId") : null;
        if (usuarioId != null) {
            return "redirect:/";
        }
        return "pages/login";
    }

    // Procesar login
    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String correo,
            @RequestParam String contrasena,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        try {
            // Normalizar correo a minúsculas y sin espacios
            correo = correo != null ? correo.trim().toLowerCase(Locale.ROOT) : null;

            // Verificar que el correo sea válido (@gmail.com o admin@furent.com)
            if (correo == null || (!correo.endsWith("@gmail.com") && !correo.equals("admin@furent.com"))) {
                model.addAttribute("error", "Solo se permiten cuentas de Gmail");
                return "pages/login";
            }
            
            // Buscar usuario por correo
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);

            if (usuarioOpt.isEmpty()) {
                model.addAttribute("error", "Correo o contraseña incorrectos");
                return "pages/login";
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar contraseña encriptada con BCrypt
            if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
                model.addAttribute("error", "Correo o contraseña incorrectos");
                return "pages/login";
            }

            // Verificar si el usuario tiene 2FA habilitado
            if (usuario.isTwoFactorEnabled()) {
                // Verificar si este dispositivo es confiable (cookie válida)
                if (esDispositivoConfiable(request, usuario.getId())) {
                    // Dispositivo confiable, no solicitar 2FA
                    return completarLogin(usuario, session, request, response, model, false);
                }
                
                // Dispositivo no confiable, solicitar 2FA
                session.setAttribute("2fa_userId", usuario.getId());
                session.setAttribute("2fa_pending", true);
                
                // Redirigir a página de verificación 2FA
                return "pages/2fa-verify-login";
            }

            // Si no tiene 2FA, completar el login normalmente
            return completarLogin(usuario, session, request, response, model, false);

        } catch (Exception e) {
            model.addAttribute("error", "Error al iniciar sesión: " + e.getMessage());
            return "pages/login";
        }
    }
    
    /**
     * Verificar código 2FA durante el login
     */
    @PostMapping("/login/verify-2fa")
    public String verificar2FA(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "false") boolean recordarDispositivo,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        
        try {
            // Verificar que hay una verificación 2FA pendiente
            Boolean pending = (Boolean) session.getAttribute("2fa_pending");
            String userId = (String) session.getAttribute("2fa_userId");
            
            if (pending == null || !pending || userId == null) {
                model.addAttribute("error", "Sesión expirada. Inicia sesión nuevamente.");
                return "redirect:/login";
            }
            
            // Verificar el código 2FA
            // userId is guaranteed to be non-null after the check above
            @SuppressWarnings("null")
            boolean codigoValido = usuarioServicio.verificarCodigoTwoFactor(userId, code);
            
            if (!codigoValido) {
                model.addAttribute("error", "Código inválido. Intenta de nuevo.");
                return "pages/2fa-verify-login";
            }
            
            // Código válido - buscar usuario y completar login
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(userId);
            if (usuarioOpt.isEmpty()) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/login";
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Limpiar datos temporales de 2FA
            session.removeAttribute("2fa_pending");
            session.removeAttribute("2fa_userId");
            
            // Completar el login (marcar dispositivo como confiable si se solicitó)
            return completarLogin(usuario, session, request, response, model, recordarDispositivo);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al verificar código: " + e.getMessage());
            return "pages/2fa-verify-login";
        }
    }
    
    /**
     * Método auxiliar para completar el login
     */
    private String completarLogin(Usuario usuario, HttpSession session, HttpServletRequest request, 
                                  HttpServletResponse response, Model model, boolean marcarDispositivoConfiable) {
        // Actualizar último inicio de sesión
        usuario.setUltimoInicioSesion(LocalDateTime.now());
        usuarioRepositorio.save(usuario);
        
        // Registrar log de login
        activityLogServicio.logLogin(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            request
        );

        // Si se debe marcar como dispositivo confiable, crear cookie
        if (marcarDispositivoConfiable) {
            crearCookieDispositivoConfiable(response, usuario.getId());
        }

        // Guardar usuario en sesión
        session.setAttribute("usuario", usuario);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getNombre());
        session.setAttribute("usuarioCorreo", usuario.getCorreo());
        session.setAttribute("usuarioRol", usuario.getRol());
        session.setAttribute("esAdmin", "ADMIN".equals(usuario.getRol()));

        // Pasar datos a la página intermedia para actualizar localStorage
        model.addAttribute("userName", usuario.getNombre());
        model.addAttribute("userEmail", usuario.getCorreo());
        model.addAttribute("userRole", usuario.getRol());
        
        // Determinar URL de redirección según el rol
        String redirectUrl = "ADMIN".equals(usuario.getRol()) ? "/admin/dashboard" : "/";
        model.addAttribute("redirectUrl", redirectUrl);

        // Mostrar página intermedia que actualiza localStorage
        return "pages/login-success";
    }
    
    /**
     * Verifica si el dispositivo actual es confiable (tiene cookie válida)
     */
    private boolean esDispositivoConfiable(HttpServletRequest request, String userId) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        
        for (Cookie cookie : cookies) {
            if ("trusted_device".equals(cookie.getName())) {
                String cookieValue = cookie.getValue();
                String expectedValue = generarTokenDispositivo(userId);
                return cookieValue.equals(expectedValue);
            }
        }
        
        return false;
    }
    
    /**
     * Crea una cookie para marcar el dispositivo como confiable por 3 días
     */
    private void crearCookieDispositivoConfiable(HttpServletResponse response, String userId) {
        String tokenValue = generarTokenDispositivo(userId);
        Cookie cookie = new Cookie("trusted_device", tokenValue);
        cookie.setMaxAge(3 * 24 * 60 * 60); // 3 días en segundos
        cookie.setHttpOnly(true); // Seguridad: no accesible desde JavaScript
        cookie.setPath("/"); // Disponible en toda la aplicación
        cookie.setSecure(false); // Cambiar a true en producción con HTTPS
        response.addCookie(cookie);
        
        System.out.println("Dispositivo marcado como confiable por 3 días para usuario: " + userId);
    }
    
    /**
     * Genera un token único para el dispositivo basado en el userId
     */
    private String generarTokenDispositivo(String userId) {
        try {
            String data = userId + "_FURENT_TRUSTED_" + "SECRET_KEY_2024";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            System.err.println("Error al generar token de dispositivo: " + e.getMessage());
            return "";
        }
    }

    // Cerrar sesión (mantiene las cuentas guardadas en localStorage)
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensaje", "Sesión cerrada exitosamente");
        return "redirect:/login"; // Redirige al selector de cuentas
    }
}
