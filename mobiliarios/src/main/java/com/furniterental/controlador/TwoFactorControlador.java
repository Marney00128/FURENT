package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/user-account/2fa")
public class TwoFactorControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    /**
     * Página para configurar 2FA
     */
    @GetMapping("/setup")
    public String setupPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        model.addAttribute("twoFactorEnabled", usuario != null && usuario.isTwoFactorEnabled());
        
        return "pages/2fa-setup";
    }

    /**
     * Habilitar 2FA - Generar QR
     */
    @PostMapping("/enable")
    public String enableTwoFactor(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        Map<String, String> resultado = usuarioServicio.habilitarTwoFactor(usuarioId);
        
        if (!resultado.isEmpty()) {
            model.addAttribute("qrCodeUrl", resultado.get("qrCodeUrl"));
            model.addAttribute("secret", resultado.get("secret"));
            model.addAttribute("mensaje", resultado.get("mensaje"));
            return "pages/2fa-qr";
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo generar el código QR");
            return "redirect:/user-account/2fa/setup";
        }
    }

    /**
     * Verificar código y activar 2FA
     */
    @PostMapping("/verify")
    public String verifyAndActivate(
            @RequestParam String code,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        if (code == null || code.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El código es requerido");
            return "redirect:/user-account/2fa/setup";
        }
        
        boolean activado = usuarioServicio.verificarYActivarTwoFactor(usuarioId, code);
        
        if (activado) {
            // Actualizar el usuario en sesión
            usuarioServicio.buscarPorId(usuarioId).ifPresent(usuario -> {
                session.setAttribute("usuario", usuario);
            });
            
            redirectAttributes.addFlashAttribute("mensaje", "Autenticación de dos factores activada exitosamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Código inválido. Intenta de nuevo.");
        }
        
        return "redirect:/user-account/2fa/setup";
    }

    /**
     * Deshabilitar 2FA
     */
    @PostMapping("/disable")
    public String disableTwoFactor(
            @RequestParam String code,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        if (code == null || code.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El código es requerido");
            return "redirect:/user-account/2fa/setup";
        }
        
        boolean deshabilitado = usuarioServicio.deshabilitarTwoFactor(usuarioId, code);
        
        if (deshabilitado) {
            // Actualizar el usuario en sesión
            usuarioServicio.buscarPorId(usuarioId).ifPresent(usuario -> {
                session.setAttribute("usuario", usuario);
            });
            
            redirectAttributes.addFlashAttribute("mensaje", "Autenticación de dos factores deshabilitada");
        } else {
            redirectAttributes.addFlashAttribute("error", "Código inválido o 2FA no está habilitado");
        }
        
        return "redirect:/user-account/2fa/setup";
    }
}
