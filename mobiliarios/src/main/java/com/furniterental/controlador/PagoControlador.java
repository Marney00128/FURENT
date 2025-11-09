package com.furniterental.controlador;

import com.furniterental.modelo.Alquiler;
import com.furniterental.modelo.Pago;
import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.AlquilerRepositorio;
import com.furniterental.repositorio.PagoRepositorio;
import com.furniterental.repositorio.UsuarioRepositorio;
import com.furniterental.servicio.EmailService;
import com.furniterental.servicio.ActivityLogServicio;
import com.furniterental.servicio.TarjetaGuardadaServicio;
import com.furniterental.modelo.TarjetaGuardada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pagos")
public class PagoControlador {

    @Autowired
    private AlquilerRepositorio alquilerRepositorio;
    
    @Autowired
    private PagoRepositorio pagoRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ActivityLogServicio activityLogServicio;
    
    @Autowired
    private TarjetaGuardadaServicio tarjetaServicio;

    /**
     * Obtiene el conteo de notificaciones de pagos pendientes para el usuario actual
     */
    @GetMapping("/notificaciones-count")
    @ResponseBody
    public Map<String, Object> obtenerConteoNotificaciones(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("count", 0);
                return response;
            }
            
            // Obtener alquileres del usuario
            List<Alquiler> alquileres = alquilerRepositorio.findByUsuarioIdOrderByFechaAlquilerDesc(usuarioId);
            
            // Contar pagos pendientes (excluyendo pago contra entrega)
            long pagosPendientes = alquileres.stream()
                .filter(a -> {
                    // Excluir pedidos con pago contra entrega
                    if (Boolean.TRUE.equals(a.getPagoContraEntrega())) {
                        return false;
                    }
                    
                    // Pago parcial pendiente: estado CONFIRMADO y pago parcial pendiente
                    boolean pagoParcialPendiente = "CONFIRMADO".equals(a.getEstado()) 
                        && "PENDIENTE".equals(a.getEstadoPagoParcial());
                    
                    // Pago final pendiente: estado COMPLETADO y pago final pendiente
                    boolean pagoFinalPendiente = "COMPLETADO".equals(a.getEstado()) 
                        && "PENDIENTE".equals(a.getEstadoPagoFinal());
                    
                    return pagoParcialPendiente || pagoFinalPendiente;
                })
                .count();
            
            response.put("success", true);
            response.put("count", pagosPendientes);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("count", 0);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * Obtiene los detalles de los pagos pendientes para mostrar en notificaciones
     */
    @GetMapping("/notificaciones")
    @ResponseBody
    public Map<String, Object> obtenerNotificaciones(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Obtener alquileres del usuario
            List<Alquiler> alquileres = alquilerRepositorio.findByUsuarioIdOrderByFechaAlquilerDesc(usuarioId);
            
            // Filtrar y mapear a notificaciones (excluyendo pago contra entrega)
            List<Map<String, Object>> notificaciones = alquileres.stream()
                .filter(a -> {
                    // Excluir pedidos con pago contra entrega
                    if (Boolean.TRUE.equals(a.getPagoContraEntrega())) {
                        return false;
                    }
                    
                    boolean pagoParcialPendiente = "CONFIRMADO".equals(a.getEstado()) 
                        && "PENDIENTE".equals(a.getEstadoPagoParcial());
                    boolean pagoFinalPendiente = "COMPLETADO".equals(a.getEstado()) 
                        && "PENDIENTE".equals(a.getEstadoPagoFinal());
                    return pagoParcialPendiente || pagoFinalPendiente;
                })
                .map(a -> {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("alquilerId", a.getId());
                    notif.put("estado", a.getEstado());
                    
                    // Determinar tipo de pago pendiente
                    if ("CONFIRMADO".equals(a.getEstado()) && "PENDIENTE".equals(a.getEstadoPagoParcial())) {
                        notif.put("tipoPago", "PARCIAL");
                        notif.put("monto", a.getMontoPagoParcial());
                        notif.put("porcentaje", 50);
                        notif.put("descripcion", "Pago inicial del 50%");
                    } else if ("COMPLETADO".equals(a.getEstado()) && "PENDIENTE".equals(a.getEstadoPagoFinal())) {
                        notif.put("tipoPago", "FINAL");
                        notif.put("monto", a.getMontoSaldoPendiente());
                        notif.put("porcentaje", 50);
                        notif.put("descripcion", "Pago final del 50%");
                    }
                    
                    notif.put("total", a.getTotal());
                    notif.put("fechaInicio", a.getFechaInicio() != null ? a.getFechaInicio().toString() : null);
                    notif.put("fechaFin", a.getFechaFin() != null ? a.getFechaFin().toString() : null);
                    
                    // Agregar items del alquiler
                    if (a.getItems() != null && !a.getItems().isEmpty()) {
                        List<String> itemsNombres = a.getItems().stream()
                            .map(item -> item.getNombreProducto() + " (x" + item.getCantidad() + ")")
                            .collect(Collectors.toList());
                        notif.put("items", itemsNombres);
                    }
                    
                    return notif;
                })
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("notificaciones", notificaciones);
            response.put("count", notificaciones.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener notificaciones: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Procesa el pago parcial (50% inicial) cuando el alquiler está CONFIRMADO
     */
    @PostMapping("/procesar-pago-parcial")
    @ResponseBody
    public Map<String, Object> procesarPagoParcial(
            @RequestParam String alquilerId,
            @RequestParam String numeroTarjeta,
            @RequestParam String nombreTitular,
            @RequestParam String fechaExpiracion,
            @RequestParam String cvv,
            HttpSession session,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Validar datos de tarjeta (simulado)
            if (numeroTarjeta == null || numeroTarjeta.length() < 16) {
                response.put("success", false);
                response.put("message", "Número de tarjeta inválido");
                return response;
            }
            
            if (cvv == null || cvv.length() < 3) {
                response.put("success", false);
                response.put("message", "CVV inválido");
                return response;
            }
            
            // Validar que alquilerId no sea null
            if (alquilerId == null || alquilerId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de alquiler inválido");
                return response;
            }
            
            // Buscar alquiler
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que pertenece al usuario
            if (!usuarioId.equals(alquiler.getUsuarioId())) {
                response.put("success", false);
                response.put("message", "No tienes permiso para realizar este pago");
                return response;
            }
            
            // Verificar que el alquiler está en estado CONFIRMADO
            if (!"CONFIRMADO".equals(alquiler.getEstado())) {
                response.put("success", false);
                response.put("message", "El alquiler debe estar en estado CONFIRMADO para realizar este pago");
                return response;
            }
            
            // Verificar que el pago parcial está pendiente
            if (!"PENDIENTE".equals(alquiler.getEstadoPagoParcial())) {
                response.put("success", false);
                response.put("message", "El pago parcial ya fue realizado");
                return response;
            }
            
            // Crear registro de pago
            Pago pago = new Pago(alquilerId, usuarioId, alquiler.getMontoPagoParcial(), "PARCIAL");
            pago.setMetodoPago("TARJETA");
            pago.setUltimos4Digitos(numeroTarjeta.substring(numeroTarjeta.length() - 4));
            pago.setNumeroTransaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            pagoRepositorio.save(pago);
            
            // Marcar pago parcial como pagado
            alquiler.marcarPagoParcialPagado();
            alquilerRepositorio.save(alquiler);
            
            // Obtener información del usuario para logs y correo
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
            
            // Enviar comprobante por correo
            try {
                if (usuarioOpt.isPresent()) {
                    emailService.enviarComprobantePago(usuarioOpt.get(), pago, alquiler);
                }
            } catch (Exception e) {
                System.err.println("Error al enviar correo: " + e.getMessage());
                // No fallar el pago si el correo falla
            }
            
            // Registrar en logs
            try {
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    activityLogServicio.logPayment(
                        usuarioId, 
                        usuario.getNombre(), 
                        usuario.getCorreo(),
                        "PARCIAL", 
                        alquiler.getMontoPagoParcial(), 
                        alquilerId,
                        pago.getNumeroTransaccion(),
                        request
                    );
                }
            } catch (Exception e) {
                System.err.println("Error al registrar log de pago: " + e.getMessage());
                // No fallar el pago si el log falla
            }
            
            response.put("success", true);
            response.put("message", "Pago recibido exitosamente");
            response.put("montoPagado", alquiler.getMontoPagoParcial());
            response.put("fechaPago", alquiler.getFechaPagoParcial().toString());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar pago: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Procesa el pago final (50% restante) cuando el alquiler está COMPLETADO
     */
    @PostMapping("/procesar-pago-final")
    @ResponseBody
    public Map<String, Object> procesarPagoFinal(
            @RequestParam String alquilerId,
            @RequestParam String numeroTarjeta,
            @RequestParam String nombreTitular,
            @RequestParam String fechaExpiracion,
            @RequestParam String cvv,
            HttpSession session,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Validar datos de tarjeta (simulado)
            if (numeroTarjeta == null || numeroTarjeta.length() < 16) {
                response.put("success", false);
                response.put("message", "Número de tarjeta inválido");
                return response;
            }
            
            if (cvv == null || cvv.length() < 3) {
                response.put("success", false);
                response.put("message", "CVV inválido");
                return response;
            }
            
            // Validar que alquilerId no sea null
            if (alquilerId == null || alquilerId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de alquiler inválido");
                return response;
            }
            
            // Buscar alquiler
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que pertenece al usuario
            if (!usuarioId.equals(alquiler.getUsuarioId())) {
                response.put("success", false);
                response.put("message", "No tienes permiso para realizar este pago");
                return response;
            }
            
            // Verificar que el alquiler está en estado COMPLETADO
            if (!"COMPLETADO".equals(alquiler.getEstado())) {
                response.put("success", false);
                response.put("message", "El alquiler debe estar en estado COMPLETADO para realizar este pago");
                return response;
            }
            
            // Verificar que el pago final está pendiente
            if (!"PENDIENTE".equals(alquiler.getEstadoPagoFinal())) {
                response.put("success", false);
                response.put("message", "El pago final ya fue realizado");
                return response;
            }
            
            // Crear registro de pago
            Pago pago = new Pago(alquilerId, usuarioId, alquiler.getMontoSaldoPendiente(), "FINAL");
            pago.setMetodoPago("TARJETA");
            pago.setUltimos4Digitos(numeroTarjeta.substring(numeroTarjeta.length() - 4));
            pago.setNumeroTransaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            pagoRepositorio.save(pago);
            
            // Marcar pago final como pagado
            alquiler.marcarPagoFinalPagado();
            alquilerRepositorio.save(alquiler);
            
            // Obtener información del usuario para logs y correo
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
            
            // Enviar comprobante por correo
            try {
                if (usuarioOpt.isPresent()) {
                    emailService.enviarComprobantePago(usuarioOpt.get(), pago, alquiler);
                }
            } catch (Exception e) {
                System.err.println("Error al enviar correo: " + e.getMessage());
                // No fallar el pago si el correo falla
            }
            
            // Registrar en logs
            try {
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    activityLogServicio.logPayment(
                        usuarioId, 
                        usuario.getNombre(), 
                        usuario.getCorreo(),
                        "FINAL", 
                        alquiler.getMontoSaldoPendiente(), 
                        alquilerId,
                        pago.getNumeroTransaccion(),
                        request
                    );
                }
            } catch (Exception e) {
                System.err.println("Error al registrar log de pago: " + e.getMessage());
                // No fallar el pago si el log falla
            }
            
            response.put("success", true);
            response.put("message", "Pago recibido exitosamente");
            response.put("montoPagado", alquiler.getMontoSaldoPendiente());
            response.put("fechaPago", alquiler.getFechaPagoFinal().toString());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar pago: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Procesa el pago parcial usando una tarjeta guardada
     */
    @PostMapping("/procesar-pago-parcial-tarjeta-guardada")
    @ResponseBody
    public Map<String, Object> procesarPagoParcialConTarjetaGuardada(
            @RequestParam String alquilerId,
            @RequestParam String tarjetaId,
            HttpSession session,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar parámetros requeridos
            if (alquilerId == null || alquilerId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de alquiler no proporcionado");
                return response;
            }
            
            if (tarjetaId == null || tarjetaId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de tarjeta no proporcionado");
                return response;
            }
            
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Obtener tarjeta guardada
            Optional<TarjetaGuardada> tarjetaOpt = tarjetaServicio.obtenerTarjeta(tarjetaId, usuarioId);
            if (tarjetaOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Tarjeta no encontrada o no pertenece al usuario");
                return response;
            }
            
            TarjetaGuardada tarjeta = tarjetaOpt.get();
            
            // Verificar que no esté vencida
            if (tarjeta.estaVencida()) {
                response.put("success", false);
                response.put("message", "La tarjeta seleccionada ha expirado");
                return response;
            }
            
            // Buscar alquiler
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que pertenece al usuario
            if (!usuarioId.equals(alquiler.getUsuarioId())) {
                response.put("success", false);
                response.put("message", "No tienes permiso para realizar este pago");
                return response;
            }
            
            // Verificar estado del alquiler
            if (!"CONFIRMADO".equals(alquiler.getEstado())) {
                response.put("success", false);
                response.put("message", "El alquiler debe estar en estado CONFIRMADO");
                return response;
            }
            
            if (!"PENDIENTE".equals(alquiler.getEstadoPagoParcial())) {
                response.put("success", false);
                response.put("message", "El pago parcial ya fue realizado");
                return response;
            }
            
            // Crear registro de pago
            Pago pago = new Pago(alquilerId, usuarioId, alquiler.getMontoPagoParcial(), "PARCIAL");
            pago.setMetodoPago("TARJETA_GUARDADA");
            pago.setUltimos4Digitos(tarjeta.getUltimos4Digitos());
            pago.setNumeroTransaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            pagoRepositorio.save(pago);
            
            // Marcar pago como pagado
            alquiler.marcarPagoParcialPagado();
            alquilerRepositorio.save(alquiler);
            
            // Obtener usuario para logs y correo
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
            
            // Enviar comprobante
            try {
                if (usuarioOpt.isPresent()) {
                    emailService.enviarComprobantePago(usuarioOpt.get(), pago, alquiler);
                }
            } catch (Exception e) {
                System.err.println("Error al enviar correo: " + e.getMessage());
            }
            
            // Registrar en logs
            try {
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    activityLogServicio.logPayment(
                        usuarioId, usuario.getNombre(), usuario.getCorreo(),
                        "PARCIAL", alquiler.getMontoPagoParcial(), alquilerId,
                        pago.getNumeroTransaccion(), request
                    );
                }
            } catch (Exception e) {
                System.err.println("Error al registrar log: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "Pago recibido exitosamente");
            response.put("montoPagado", alquiler.getMontoPagoParcial());
            response.put("fechaPago", alquiler.getFechaPagoParcial().toString());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar pago: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Procesa el pago final usando una tarjeta guardada
     */
    @PostMapping("/procesar-pago-final-tarjeta-guardada")
    @ResponseBody
    public Map<String, Object> procesarPagoFinalConTarjetaGuardada(
            @RequestParam String alquilerId,
            @RequestParam String tarjetaId,
            HttpSession session,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar parámetros requeridos
            if (alquilerId == null || alquilerId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de alquiler no proporcionado");
                return response;
            }
            
            if (tarjetaId == null || tarjetaId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de tarjeta no proporcionado");
                return response;
            }
            
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Obtener tarjeta guardada
            Optional<TarjetaGuardada> tarjetaOpt = tarjetaServicio.obtenerTarjeta(tarjetaId, usuarioId);
            if (tarjetaOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Tarjeta no encontrada o no pertenece al usuario");
                return response;
            }
            
            TarjetaGuardada tarjeta = tarjetaOpt.get();
            
            // Verificar que no esté vencida
            if (tarjeta.estaVencida()) {
                response.put("success", false);
                response.put("message", "La tarjeta seleccionada ha expirado");
                return response;
            }
            
            // Buscar alquiler
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que pertenece al usuario
            if (!usuarioId.equals(alquiler.getUsuarioId())) {
                response.put("success", false);
                response.put("message", "No tienes permiso para realizar este pago");
                return response;
            }
            
            // Verificar estado del alquiler
            if (!"COMPLETADO".equals(alquiler.getEstado())) {
                response.put("success", false);
                response.put("message", "El alquiler debe estar en estado COMPLETADO");
                return response;
            }
            
            if (!"PENDIENTE".equals(alquiler.getEstadoPagoFinal())) {
                response.put("success", false);
                response.put("message", "El pago final ya fue realizado");
                return response;
            }
            
            // Crear registro de pago
            Pago pago = new Pago(alquilerId, usuarioId, alquiler.getMontoSaldoPendiente(), "FINAL");
            pago.setMetodoPago("TARJETA_GUARDADA");
            pago.setUltimos4Digitos(tarjeta.getUltimos4Digitos());
            pago.setNumeroTransaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            pagoRepositorio.save(pago);
            
            // Marcar pago como pagado
            alquiler.marcarPagoFinalPagado();
            alquilerRepositorio.save(alquiler);
            
            // Obtener usuario para logs y correo
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
            
            // Enviar comprobante
            try {
                if (usuarioOpt.isPresent()) {
                    emailService.enviarComprobantePago(usuarioOpt.get(), pago, alquiler);
                }
            } catch (Exception e) {
                System.err.println("Error al enviar correo: " + e.getMessage());
            }
            
            // Registrar en logs
            try {
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    activityLogServicio.logPayment(
                        usuarioId, usuario.getNombre(), usuario.getCorreo(),
                        "FINAL", alquiler.getMontoSaldoPendiente(), alquilerId,
                        pago.getNumeroTransaccion(), request
                    );
                }
            } catch (Exception e) {
                System.err.println("Error al registrar log: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "Pago recibido exitosamente");
            response.put("montoPagado", alquiler.getMontoSaldoPendiente());
            response.put("fechaPago", alquiler.getFechaPagoFinal().toString());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar pago: " + e.getMessage());
        }
        
        return response;
    }
}
