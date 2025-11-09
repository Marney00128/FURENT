package com.furniterental.controlador;

import com.furniterental.servicio.TwoFactorAuthServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
public class TwoFactorAuthControlador {
    
    @Autowired
    private TwoFactorAuthServicio twoFactorAuthServicio;
    
    /**
     * Mostrar página de verificación 2FA para reportes
     */
    @GetMapping("/admin/reportes/verify")
    public String mostrarVerificacion(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Verificar que el usuario sea admin
        String rol = (String) session.getAttribute("usuarioRol");
        if (!"ADMIN".equals(rol)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }
        
        // Verificar si ya está autenticado con 2FA
        Boolean is2FAVerified = (Boolean) session.getAttribute("reportes2FAVerified");
        if (Boolean.TRUE.equals(is2FAVerified)) {
            return "redirect:/admin/reportes";
        }
        
        return "admin/2fa-verify";
    }
    
    /**
     * Procesar verificación del código 2FA
     */
    @PostMapping("/admin/reportes/verify")
    public String verificarCodigo(@RequestParam String code, 
                                  HttpSession session, 
                                  RedirectAttributes redirectAttributes) {
        // Verificar que el usuario sea admin
        String rol = (String) session.getAttribute("usuarioRol");
        if (!"ADMIN".equals(rol)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }
        
        // Verificar el código
        boolean isValid = twoFactorAuthServicio.verifyCode(code);
        
        if (isValid) {
            // Marcar como verificado en la sesión
            session.setAttribute("reportes2FAVerified", true);
            redirectAttributes.addFlashAttribute("mensaje", "Verificación exitosa");
            return "redirect:/admin/reportes";
        } else {
            redirectAttributes.addFlashAttribute("error", "Código inválido. Por favor, intenta de nuevo.");
            return "redirect:/admin/reportes/verify";
        }
    }
    
    /**
     * Página de configuración inicial del 2FA (mostrar QR)
     */
    @GetMapping("/admin/reportes/setup-2fa")
    public String setupTwoFactor(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Verificar que el usuario sea admin
        String rol = (String) session.getAttribute("usuarioRol");
        if (!"ADMIN".equals(rol)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos de administrador");
            return "redirect:/login";
        }
        
        // Obtener la URL del QR Code
        String qrCodeUrl = twoFactorAuthServicio.getQRCodeUrlForReportes();
        String secretKey = twoFactorAuthServicio.getReportesSecret();
        
        model.addAttribute("qrCodeUrl", qrCodeUrl);
        model.addAttribute("secretKey", secretKey);
        
        return "admin/2fa-setup";
    }
}
