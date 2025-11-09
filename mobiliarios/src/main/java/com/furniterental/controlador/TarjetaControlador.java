package com.furniterental.controlador;

import com.furniterental.modelo.TarjetaGuardada;
import com.furniterental.servicio.TarjetaGuardadaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar tarjetas guardadas de los usuarios
 */
@Controller
@RequestMapping("/tarjetas")
public class TarjetaControlador {
    
    @Autowired
    private TarjetaGuardadaServicio tarjetaServicio;
    
    /**
     * Obtiene todas las tarjetas del usuario (sin datos sensibles)
     */
    @GetMapping("/listar")
    @ResponseBody
    public Map<String, Object> listarTarjetas(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            List<TarjetaGuardada> tarjetas = tarjetaServicio.obtenerTarjetasUsuario(usuarioId);
            
            // Mapear a formato seguro (sin datos encriptados)
            List<Map<String, Object>> tarjetasSeguras = tarjetas.stream()
                .map(t -> {
                    Map<String, Object> tarjetaMap = new HashMap<>();
                    tarjetaMap.put("id", t.getId());
                    tarjetaMap.put("nombreTitular", t.getNombreTitular());
                    tarjetaMap.put("ultimos4Digitos", t.getUltimos4Digitos());
                    tarjetaMap.put("tarjetaEnmascarada", t.getTarjetaEnmascarada());
                    tarjetaMap.put("tipoTarjeta", t.getTipoTarjeta());
                    tarjetaMap.put("mesExpiracion", t.getMesExpiracion());
                    tarjetaMap.put("anioExpiracion", t.getAnioExpiracion());
                    tarjetaMap.put("fechaExpiracion", t.getFechaExpiracionFormateada());
                    tarjetaMap.put("esPredeterminada", t.isEsPredeterminada());
                    tarjetaMap.put("alias", t.getAlias());
                    tarjetaMap.put("estaVencida", t.estaVencida());
                    return tarjetaMap;
                })
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("tarjetas", tarjetasSeguras);
            response.put("count", tarjetasSeguras.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener tarjetas: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Guarda una nueva tarjeta
     */
    @PostMapping("/guardar")
    @ResponseBody
    public Map<String, Object> guardarTarjeta(
            @RequestParam String numeroTarjeta,
            @RequestParam String cvv,
            @RequestParam String nombreTitular,
            @RequestParam String mesExpiracion,
            @RequestParam String anioExpiracion,
            @RequestParam(required = false) String alias,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Validar datos
            if (numeroTarjeta == null || numeroTarjeta.replaceAll("[\\s-]", "").length() < 13) {
                response.put("success", false);
                response.put("message", "Número de tarjeta inválido");
                return response;
            }
            
            if (cvv == null || cvv.length() < 3) {
                response.put("success", false);
                response.put("message", "CVV inválido");
                return response;
            }
            
            if (nombreTitular == null || nombreTitular.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Nombre del titular es requerido");
                return response;
            }
            
            // Validar fecha de expiración
            try {
                int mes = Integer.parseInt(mesExpiracion);
                int anio = Integer.parseInt(anioExpiracion);
                
                if (mes < 1 || mes > 12) {
                    response.put("success", false);
                    response.put("message", "Mes de expiración inválido");
                    return response;
                }
                
                if (anio < 2024 || anio > 2050) {
                    response.put("success", false);
                    response.put("message", "Año de expiración inválido");
                    return response;
                }
            } catch (NumberFormatException e) {
                response.put("success", false);
                response.put("message", "Fecha de expiración inválida");
                return response;
            }
            
            // Limpiar número de tarjeta (remover espacios y guiones)
            String numeroLimpio = numeroTarjeta.replaceAll("[\\s-]", "");
            
            // Guardar tarjeta
            TarjetaGuardada tarjeta = tarjetaServicio.guardarTarjeta(
                usuarioId, numeroLimpio, cvv, nombreTitular,
                mesExpiracion, anioExpiracion, alias
            );
            
            // Retornar datos seguros
            Map<String, Object> tarjetaSegura = new HashMap<>();
            tarjetaSegura.put("id", tarjeta.getId());
            tarjetaSegura.put("nombreTitular", tarjeta.getNombreTitular());
            tarjetaSegura.put("ultimos4Digitos", tarjeta.getUltimos4Digitos());
            tarjetaSegura.put("tarjetaEnmascarada", tarjeta.getTarjetaEnmascarada());
            tarjetaSegura.put("tipoTarjeta", tarjeta.getTipoTarjeta());
            tarjetaSegura.put("fechaExpiracion", tarjeta.getFechaExpiracionFormateada());
            tarjetaSegura.put("esPredeterminada", tarjeta.isEsPredeterminada());
            tarjetaSegura.put("alias", tarjeta.getAlias());
            
            response.put("success", true);
            response.put("message", "Tarjeta guardada exitosamente");
            response.put("tarjeta", tarjetaSegura);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar tarjeta: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Establece una tarjeta como predeterminada
     */
    @PostMapping("/establecer-predeterminada")
    @ResponseBody
    public Map<String, Object> establecerPredeterminada(
            @RequestParam String tarjetaId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            tarjetaServicio.establecerComoPredeterminada(tarjetaId, usuarioId);
            
            response.put("success", true);
            response.put("message", "Tarjeta establecida como predeterminada");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al establecer tarjeta predeterminada: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Elimina una tarjeta
     */
    @PostMapping("/eliminar")
    @ResponseBody
    public Map<String, Object> eliminarTarjeta(
            @RequestParam String tarjetaId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            boolean eliminada = tarjetaServicio.eliminarTarjeta(tarjetaId, usuarioId);
            
            if (eliminada) {
                response.put("success", true);
                response.put("message", "Tarjeta eliminada exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo eliminar la tarjeta");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar tarjeta: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Actualiza el alias de una tarjeta
     */
    @PostMapping("/actualizar-alias")
    @ResponseBody
    public Map<String, Object> actualizarAlias(
            @RequestParam String tarjetaId,
            @RequestParam String alias,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            boolean actualizada = tarjetaServicio.actualizarAlias(tarjetaId, usuarioId, alias);
            
            if (actualizada) {
                response.put("success", true);
                response.put("message", "Alias actualizado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo actualizar el alias");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar alias: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Obtiene la tarjeta predeterminada (para usar en pagos)
     */
    @GetMapping("/predeterminada")
    @ResponseBody
    public Map<String, Object> obtenerTarjetaPredeterminada(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            Optional<TarjetaGuardada> tarjetaOpt = tarjetaServicio.obtenerTarjetaPredeterminada(usuarioId);
            
            if (tarjetaOpt.isPresent()) {
                TarjetaGuardada tarjeta = tarjetaOpt.get();
                
                Map<String, Object> tarjetaSegura = new HashMap<>();
                tarjetaSegura.put("id", tarjeta.getId());
                tarjetaSegura.put("nombreTitular", tarjeta.getNombreTitular());
                tarjetaSegura.put("ultimos4Digitos", tarjeta.getUltimos4Digitos());
                tarjetaSegura.put("tarjetaEnmascarada", tarjeta.getTarjetaEnmascarada());
                tarjetaSegura.put("tipoTarjeta", tarjeta.getTipoTarjeta());
                tarjetaSegura.put("fechaExpiracion", tarjeta.getFechaExpiracionFormateada());
                tarjetaSegura.put("alias", tarjeta.getAlias());
                
                response.put("success", true);
                response.put("tarjeta", tarjetaSegura);
            } else {
                response.put("success", false);
                response.put("message", "No tienes tarjetas guardadas");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener tarjeta: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Limpia tarjetas duplicadas del usuario (mantiene solo una por cada últimos 4 dígitos)
     */
    @PostMapping("/limpiar-duplicadas")
    @ResponseBody
    public Map<String, Object> limpiarDuplicadas(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            int eliminadas = tarjetaServicio.limpiarTarjetasDuplicadas(usuarioId);
            
            response.put("success", true);
            response.put("message", "Se eliminaron " + eliminadas + " tarjetas duplicadas");
            response.put("eliminadas", eliminadas);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al limpiar duplicadas: " + e.getMessage());
        }
        
        return response;
    }
}
