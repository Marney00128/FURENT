package com.furniterental.controlador;

import com.furniterental.modelo.Direccion;
import com.furniterental.repositorio.DireccionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/direcciones")
public class DireccionControlador {
    
    @Autowired
    private DireccionRepositorio direccionRepositorio;
    
    // Ver página de direcciones
    @GetMapping
    public String verDirecciones(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para ver tus direcciones");
            return "redirect:/login";
        }
        
        List<Direccion> direcciones = direccionRepositorio.findByUsuarioIdOrderByEsPrincipalDescFechaCreacionDesc(usuarioId);
        model.addAttribute("direcciones", direcciones);
        
        return "pages/direcciones";
    }
    
    // Crear nueva dirección
    @PostMapping("/crear")
    public String crearDireccion(
            @RequestParam String nombreDireccion,
            @RequestParam String direccionCompleta,
            @RequestParam String ciudad,
            @RequestParam String departamento,
            @RequestParam(required = false) String codigoPostal,
            @RequestParam String telefono,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) boolean esPrincipal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        // Validaciones básicas
        if (nombreDireccion == null || nombreDireccion.trim().isEmpty() ||
            direccionCompleta == null || direccionCompleta.trim().isEmpty() ||
            ciudad == null || ciudad.trim().isEmpty() ||
            departamento == null || departamento.trim().isEmpty() ||
            telefono == null || telefono.trim().isEmpty()) {
            
            redirectAttributes.addFlashAttribute("error", "Todos los campos obligatorios deben estar completos");
            return "redirect:/direcciones";
        }
        
        try {
            // Si esta dirección es principal, quitar el flag de las demás
            if (esPrincipal) {
                Optional<Direccion> direccionPrincipalActual = direccionRepositorio.findByUsuarioIdAndEsPrincipalTrue(usuarioId);
                direccionPrincipalActual.ifPresent(dir -> {
                    dir.setEsPrincipal(false);
                    direccionRepositorio.save(dir);
                });
            }
            
            // Crear nueva dirección
            Direccion nuevaDireccion = new Direccion(usuarioId, nombreDireccion, direccionCompleta, ciudad, departamento, telefono);
            nuevaDireccion.setCodigoPostal(codigoPostal);
            nuevaDireccion.setReferencia(referencia);
            nuevaDireccion.setEsPrincipal(esPrincipal);
            
            direccionRepositorio.save(nuevaDireccion);
            
            redirectAttributes.addFlashAttribute("mensaje", "Dirección agregada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la dirección: " + e.getMessage());
        }
        
        return "redirect:/direcciones";
    }
    
    // Editar dirección
    @PostMapping("/editar/{id}")
    public String editarDireccion(
            @PathVariable String id,
            @RequestParam String nombreDireccion,
            @RequestParam String direccionCompleta,
            @RequestParam String ciudad,
            @RequestParam String departamento,
            @RequestParam(required = false) String codigoPostal,
            @RequestParam String telefono,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) boolean esPrincipal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        if (id == null || id.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ID de dirección inválido");
            return "redirect:/direcciones";
        }
        
        try {
            Optional<Direccion> direccionOpt = direccionRepositorio.findById(id);
            
            if (!direccionOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Dirección no encontrada");
                return "redirect:/direcciones";
            }
            
            Direccion direccion = Objects.requireNonNull(direccionOpt.orElseThrow());
            
            // Verificar que la dirección pertenece al usuario
            if (!direccion.getUsuarioId().equals(usuarioId)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar esta dirección");
                return "redirect:/direcciones";
            }
            
            // Si esta dirección es principal, quitar el flag de las demás
            if (esPrincipal && !direccion.isEsPrincipal()) {
                Optional<Direccion> direccionPrincipalActual = direccionRepositorio.findByUsuarioIdAndEsPrincipalTrue(usuarioId);
                direccionPrincipalActual.ifPresent(dir -> {
                    if (!dir.getId().equals(id)) {
                        dir.setEsPrincipal(false);
                        direccionRepositorio.save(dir);
                    }
                });
            }
            
            // Actualizar dirección
            direccion.setNombreDireccion(nombreDireccion);
            direccion.setDireccionCompleta(direccionCompleta);
            direccion.setCiudad(ciudad);
            direccion.setDepartamento(departamento);
            direccion.setCodigoPostal(codigoPostal);
            direccion.setTelefono(telefono);
            direccion.setReferencia(referencia);
            direccion.setEsPrincipal(esPrincipal);
            direccion.setFechaActualizacion(LocalDateTime.now());
            
            direccionRepositorio.save(direccion);
            
            redirectAttributes.addFlashAttribute("mensaje", "Dirección actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la dirección: " + e.getMessage());
        }
        
        return "redirect:/direcciones";
    }
    
    // Eliminar dirección
    @PostMapping("/eliminar/{id}")
    public String eliminarDireccion(
            @PathVariable @NonNull String id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }
        
        try {
            Optional<Direccion> direccionOpt = direccionRepositorio.findById(id);
            
            if (!direccionOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Dirección no encontrada");
                return "redirect:/direcciones";
            }
            
            Direccion direccion = Objects.requireNonNull(direccionOpt.orElseThrow());
            
            // Verificar que la dirección pertenece al usuario
            if (!direccion.getUsuarioId().equals(usuarioId)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar esta dirección");
                return "redirect:/direcciones";
            }
            
            direccionRepositorio.deleteById(id);
            
            redirectAttributes.addFlashAttribute("mensaje", "Dirección eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la dirección: " + e.getMessage());
        }
        
        return "redirect:/direcciones";
    }
    
    // Establecer como principal
    @PostMapping("/principal/{id}")
    @ResponseBody
    public Map<String, Object> establecerPrincipal(
            @PathVariable String id,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Debes iniciar sesión");
            return response;
        }
        
        if (id == null || id.isEmpty()) {
            response.put("success", false);
            response.put("message", "ID de dirección inválido");
            return response;
        }
        
        try {
            Optional<Direccion> direccionOpt = direccionRepositorio.findById(id);
            
            if (!direccionOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Dirección no encontrada");
                return response;
            }
            
            Direccion direccion = Objects.requireNonNull(direccionOpt.orElseThrow());
            
            // Verificar que la dirección pertenece al usuario
            if (!direccion.getUsuarioId().equals(usuarioId)) {
                response.put("success", false);
                response.put("message", "No tienes permisos");
                return response;
            }
            
            // Quitar flag principal de todas las direcciones del usuario
            Optional<Direccion> direccionPrincipalActual = direccionRepositorio.findByUsuarioIdAndEsPrincipalTrue(usuarioId);
            direccionPrincipalActual.ifPresent(dir -> {
                dir.setEsPrincipal(false);
                direccionRepositorio.save(dir);
            });
            
            // Establecer esta como principal
            direccion.setEsPrincipal(true);
            direccion.setFechaActualizacion(LocalDateTime.now());
            direccionRepositorio.save(direccion);
            
            response.put("success", true);
            response.put("message", "Dirección establecida como principal");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    // Obtener direcciones del usuario (API para checkout)
    @GetMapping("/api/listar")
    @ResponseBody
    public Map<String, Object> listarDirecciones(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("direcciones", List.of());
            return response;
        }
        
        List<Direccion> direcciones = direccionRepositorio.findByUsuarioIdOrderByEsPrincipalDescFechaCreacionDesc(usuarioId);
        response.put("success", true);
        response.put("direcciones", direcciones);
        
        return response;
    }
}
