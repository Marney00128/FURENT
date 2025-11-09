package com.furniterental.controlador;

import com.furniterental.servicio.PasswordResetServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class PasswordResetControlador {

    @Autowired
    private PasswordResetServicio passwordResetServicio;

    /**
     * Muestra la página para solicitar recuperación de contraseña
     */
    @GetMapping("/forgot-password")
    public String mostrarForgotPassword() {
        return "pages/forgot-password";
    }

    /**
     * Procesa la solicitud de recuperación de contraseña
     * Genera y envía un código de verificación al correo
     */
    @PostMapping("/forgot-password")
    public String procesarSolicitudRecuperacion(
            @RequestParam String correo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validar que el correo sea válido (@gmail.com o admin@furent.com)
            if (!correo.endsWith("@gmail.com") && !correo.equals("admin@furent.com")) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten cuentas de Gmail");
                return "redirect:/forgot-password";
            }

            // Iniciar proceso de recuperación
            boolean exito = passwordResetServicio.iniciarRecuperacion(correo);

            if (exito) {
                // Guardar correo en sesión para los siguientes pasos
                session.setAttribute("reset_email", correo);
                
                // Verificar si el usuario tiene 2FA habilitado
                boolean tiene2FA = passwordResetServicio.usuarioTiene2FA(correo);
                session.setAttribute("reset_requires_2fa", tiene2FA);

                redirectAttributes.addFlashAttribute("mensaje", 
                    "Se ha enviado un código de verificación a tu correo electrónico");
                return "redirect:/reset-password/verify";
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Error al procesar la solicitud. Intenta nuevamente");
                return "redirect:/forgot-password";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    /**
     * Muestra la página para verificar el código de recuperación
     */
    @GetMapping("/reset-password/verify")
    public String mostrarVerificarCodigo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Verificar que hay un proceso de recuperación en curso
        String correo = (String) session.getAttribute("reset_email");
        if (correo == null) {
            redirectAttributes.addFlashAttribute("error", "Sesión expirada. Inicia el proceso nuevamente");
            return "redirect:/forgot-password";
        }

        // Verificar si se requiere 2FA
        Boolean requires2FA = (Boolean) session.getAttribute("reset_requires_2fa");
        model.addAttribute("requires2FA", requires2FA != null && requires2FA);
        model.addAttribute("correo", correo);

        return "pages/reset-password-verify";
    }

    /**
     * Procesa la verificación del código de recuperación
     */
    @PostMapping("/reset-password/verify")
    public String procesarVerificacionCodigo(
            @RequestParam String codigo,
            @RequestParam(required = false) String codigo2FA,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Obtener correo de la sesión
            String correo = (String) session.getAttribute("reset_email");
            if (correo == null) {
                redirectAttributes.addFlashAttribute("error", "Sesión expirada. Inicia el proceso nuevamente");
                return "redirect:/forgot-password";
            }

            // Verificar código(s)
            boolean codigoValido = passwordResetServicio.verificarCodigo(correo, codigo, codigo2FA);

            if (codigoValido) {
                // Marcar código como verificado
                session.setAttribute("reset_code_verified", true);
                redirectAttributes.addFlashAttribute("mensaje", "Código verificado correctamente");
                return "redirect:/reset-password/new";
            } else {
                Boolean requires2FA = (Boolean) session.getAttribute("reset_requires_2fa");
                if (requires2FA != null && requires2FA && (codigo2FA == null || codigo2FA.trim().isEmpty())) {
                    redirectAttributes.addFlashAttribute("error", "Se requiere el código de autenticación de dos factores");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Código(s) incorrecto(s) o expirado(s)");
                }
                return "redirect:/reset-password/verify";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al verificar código: " + e.getMessage());
            return "redirect:/reset-password/verify";
        }
    }

    /**
     * Muestra la página para establecer una nueva contraseña
     */
    @GetMapping("/reset-password/new")
    public String mostrarNuevaContrasena(HttpSession session, RedirectAttributes redirectAttributes) {
        // Verificar que el código fue verificado
        Boolean codeVerified = (Boolean) session.getAttribute("reset_code_verified");
        String correo = (String) session.getAttribute("reset_email");

        if (correo == null || codeVerified == null || !codeVerified) {
            redirectAttributes.addFlashAttribute("error", "Sesión expirada o código no verificado");
            return "redirect:/forgot-password";
        }

        return "pages/reset-password-new";
    }

    /**
     * Procesa el restablecimiento de la contraseña
     */
    @PostMapping("/reset-password/new")
    public String procesarNuevaContrasena(
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Verificar que el código fue verificado
            Boolean codeVerified = (Boolean) session.getAttribute("reset_code_verified");
            String correo = (String) session.getAttribute("reset_email");

            if (correo == null || codeVerified == null || !codeVerified) {
                redirectAttributes.addFlashAttribute("error", "Sesión expirada o código no verificado");
                return "redirect:/forgot-password";
            }

            // Validar que las contraseñas coincidan
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/reset-password/new";
            }

            // Validar fortaleza de la contraseña
            String errorContrasena = passwordResetServicio.validarContrasena(nuevaContrasena);
            if (errorContrasena != null) {
                redirectAttributes.addFlashAttribute("error", errorContrasena);
                return "redirect:/reset-password/new";
            }

            // Restablecer contraseña
            boolean exito = passwordResetServicio.restablecerContrasena(correo, nuevaContrasena);

            if (exito) {
                // Limpiar sesión
                session.removeAttribute("reset_email");
                session.removeAttribute("reset_code_verified");
                session.removeAttribute("reset_requires_2fa");

                redirectAttributes.addFlashAttribute("mensaje", 
                    "¡Contraseña restablecida exitosamente! Ya puedes iniciar sesión");
                return "redirect:/login/new?email=" + java.net.URLEncoder.encode(correo, "UTF-8");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al restablecer contraseña");
                return "redirect:/reset-password/new";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al restablecer contraseña: " + e.getMessage());
            return "redirect:/reset-password/new";
        }
    }

    /**
     * Cancela el proceso de recuperación
     */
    @GetMapping("/reset-password/cancel")
    public String cancelarRecuperacion(HttpSession session, RedirectAttributes redirectAttributes) {
        // Limpiar sesión
        session.removeAttribute("reset_email");
        session.removeAttribute("reset_code_verified");
        session.removeAttribute("reset_requires_2fa");

        redirectAttributes.addFlashAttribute("mensaje", "Proceso de recuperación cancelado");
        return "redirect:/login";
    }
}
