package com.furniterental.controlador;

import com.furniterental.modelo.Alquiler;
import com.furniterental.modelo.ItemCarrito;
import com.furniterental.modelo.Producto;
import com.furniterental.repositorio.AlquilerRepositorio;
import com.furniterental.repositorio.ProductoRepositorio;
import com.furniterental.servicio.ActivityLogServicio;
import com.furniterental.servicio.EmailServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/alquiler")
public class AlquilerControlador {

    @Autowired
    private AlquilerRepositorio alquilerRepositorio;
    
    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    @Autowired
    private ActivityLogServicio activityLogServicio;
    
    @Autowired
    private EmailServicio emailServicio;

    // Procesar alquiler (checkout)
    @PostMapping("/procesar")
    public String procesarAlquiler(
            @RequestParam String direccionEntrega,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String notasAdicionales,
            @RequestParam(required = false) String requiereTransporte,
            @RequestParam(required = false) String pagoContraEntrega,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Convertir y validar requiereTransporte
            boolean solicitaTransporte = "true".equalsIgnoreCase(requiereTransporte);
            
            // Convertir y validar pagoContraEntrega
            boolean pagoContraEntregaBoolean = "true".equalsIgnoreCase(pagoContraEntrega);
            
            // Log para debug
            System.out.println(">>> Procesando alquiler - requiereTransporte STRING: " + requiereTransporte);
            System.out.println(">>> Procesando alquiler - requiereTransporte BOOLEAN: " + solicitaTransporte);
            System.out.println(">>> Procesando alquiler - pagoContraEntrega STRING: " + pagoContraEntrega);
            System.out.println(">>> Procesando alquiler - pagoContraEntrega BOOLEAN: " + pagoContraEntregaBoolean);
            
            String usuarioId = (String) session.getAttribute("usuarioId");
            String usuarioNombre = (String) session.getAttribute("usuarioNombre");
            String usuarioCorreo = (String) session.getAttribute("usuarioCorreo");
            
            if (usuarioId == null) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para realizar un alquiler");
                return "redirect:/login";
            }

            @SuppressWarnings("unchecked")
            List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
            
            if (carrito == null || carrito.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/carrito";
            }
            
            // Validar fechas
            if (fechaInicio.isAfter(fechaFin)) {
                redirectAttributes.addFlashAttribute("error", "La fecha de inicio debe ser anterior a la fecha de fin");
                return "redirect:/carrito";
            }
            
            // Calcular días de alquiler (mínimo 1 día)
            long diasAlquiler = ChronoUnit.DAYS.between(fechaInicio.toLocalDate(), fechaFin.toLocalDate());
            if (diasAlquiler < 1) {
                diasAlquiler = 1; // Mínimo 1 día
            }
            
            // Actualizar los días de alquiler en cada item del carrito para el cálculo correcto
            for (ItemCarrito item : carrito) {
                item.setDiasAlquiler((int) diasAlquiler);
            }
            
            // Validar y descontar stock de cada producto
            for (ItemCarrito item : carrito) {
                String productoId = item.getProductoId();
                if (productoId == null) {
                    redirectAttributes.addFlashAttribute("error", "ID de producto inválido en el carrito");
                    return "redirect:/carrito";
                }
                
                Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
                
                if (!productoOpt.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado: " + item.getNombreProducto());
                    return "redirect:/carrito";
                }
                
                Producto producto = productoOpt.get();
                Integer stockActual = producto.getStock();
                
                // Validar stock disponible
                if (stockActual == null || stockActual < item.getCantidad()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Stock insuficiente para " + item.getNombreProducto() + 
                        ". Disponible: " + (stockActual != null ? stockActual : 0) + 
                        ", Solicitado: " + item.getCantidad());
                    return "redirect:/carrito";
                }
                
                // Descontar stock
                producto.setStock(stockActual - item.getCantidad());
                productoRepositorio.save(producto);
                
                System.out.println(">>> Stock actualizado para " + producto.getNombreProducto() + 
                                 ": " + stockActual + " -> " + producto.getStock());
            }

            // Crear alquiler
            Alquiler alquiler = new Alquiler();
            alquiler.setUsuarioId(usuarioId);
            alquiler.setUsuarioNombre(usuarioNombre);
            alquiler.setUsuarioCorreo(usuarioCorreo);
            alquiler.setItems(carrito);
            alquiler.setDireccionEntrega(direccionEntrega);
            alquiler.setFechaInicio(fechaInicio);
            alquiler.setFechaFin(fechaFin);
            alquiler.setNotasAdicionales(notasAdicionales);
            alquiler.setEstado("PENDIENTE");
            
            // Configurar pago contra entrega
            alquiler.setPagoContraEntrega(pagoContraEntregaBoolean);
            
            // Configurar estado de transporte según la elección del usuario
            if (solicitaTransporte) {
                alquiler.setEstadoTransporte("PENDIENTE");
                System.out.println(">>> Usuario SÍ solicitó transporte - Estado: PENDIENTE");
            } else {
                alquiler.setEstadoTransporte(null);
                System.out.println(">>> Usuario NO solicitó transporte - Estado: null (no se mostrará opción de negociación)");
            }

            // Guardar en MongoDB
            alquilerRepositorio.save(alquiler);
            
            // Calcular total y cantidad de productos para el log
            int cantidadProductos = carrito.stream()
                .mapToInt(ItemCarrito::getCantidad)
                .sum();
            double totalAlquiler = carrito.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
            
            // Registrar log de alquiler
            activityLogServicio.logRental(
                usuarioId,
                usuarioNombre,
                usuarioCorreo,
                cantidadProductos,
                totalAlquiler,
                request
            );
            
            // Enviar correo de confirmación al usuario
            try {
                String detallesAlquiler = construirDetallesAlquiler(alquiler);
                emailServicio.enviarConfirmacionAlquiler(
                    usuarioCorreo,
                    usuarioNombre,
                    alquiler.getId(),
                    detallesAlquiler
                );
                System.out.println("Correo de confirmación enviado a: " + usuarioCorreo);
            } catch (Exception e) {
                System.err.println("Error al enviar correo de confirmación: " + e.getMessage());
                // No fallar la operación si el correo falla
            }

            // Limpiar carrito
            session.removeAttribute("carrito");

            redirectAttributes.addFlashAttribute("mensaje", "¡Alquiler realizado exitosamente! ID: " + alquiler.getId());
            return "redirect:/alquiler/mis-alquileres";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar alquiler: " + e.getMessage());
            return "redirect:/carrito";
        }
    }

    // Ver mis alquileres
    @GetMapping("/mis-alquileres")
    public String misAlquileres(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }

        List<Alquiler> alquileres = alquilerRepositorio.findByUsuarioIdOrderByFechaAlquilerDesc(usuarioId);
        model.addAttribute("alquileres", alquileres);

        return "pages/mis-alquileres";
    }

    // Ver detalle de un alquiler
    @GetMapping("/detalle/{id}")
    public String detalleAlquiler(@PathVariable @NonNull String id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }

        Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(id);
        
        if (!alquilerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Alquiler no encontrado");
            return "redirect:/alquiler/mis-alquileres";
        }

        Alquiler alquiler = alquilerOpt.get();
        
        // Verificar que el alquiler pertenece al usuario actual
        if (!alquiler.getUsuarioId().equals(usuarioId)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este alquiler");
            return "redirect:/alquiler/mis-alquileres";
        }

        model.addAttribute("alquiler", alquiler);
        return "pages/detalle-alquiler";
    }

    // Cancelar alquiler
    @PostMapping("/cancelar/{id}")
    public String cancelarAlquiler(@PathVariable @NonNull String id, HttpSession session, 
                                   HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }

        Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(id);
        
        if (!alquilerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Alquiler no encontrado");
            return "redirect:/alquiler/mis-alquileres";
        }

        Alquiler alquiler = alquilerOpt.get();
        
        // Verificar que el alquiler pertenece al usuario actual
        if (!alquiler.getUsuarioId().equals(usuarioId)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar este alquiler");
            return "redirect:/alquiler/mis-alquileres";
        }

        // Solo se puede cancelar si está pendiente
        if (!"PENDIENTE".equals(alquiler.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Solo se pueden cancelar alquileres pendientes");
            return "redirect:/alquiler/mis-alquileres";
        }

        // Devolver stock
        for (ItemCarrito item : alquiler.getItems()) {
            String productoId = item.getProductoId();
            if (productoId != null) {
                Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();
                    producto.setStock(producto.getStock() + item.getCantidad());
                    productoRepositorio.save(producto);
                }
            }
        }

        alquiler.setEstado("CANCELADO");
        alquilerRepositorio.save(alquiler);
        
        // Registrar log de cancelación
        String usuarioNombre = (String) session.getAttribute("usuarioNombre");
        String usuarioCorreo = (String) session.getAttribute("usuarioCorreo");
        
        if (usuarioNombre != null && usuarioCorreo != null) {
            activityLogServicio.logRentalCancellation(
                usuarioId,
                usuarioNombre,
                usuarioCorreo,
                alquiler.getId(),
                request
            );
        }
        
        // Enviar correo de notificación de cancelación al usuario
        try {
            String detallesAlquiler = construirDetallesAlquiler(alquiler);
            emailServicio.enviarNotificacionCancelacion(
                alquiler.getUsuarioCorreo(),
                alquiler.getUsuarioNombre(),
                alquiler.getId(),
                detallesAlquiler
            );
            System.out.println("Correo de cancelación enviado a: " + alquiler.getUsuarioCorreo());
        } catch (Exception e) {
            System.err.println("Error al enviar correo de cancelación: " + e.getMessage());
            // No fallar la operación si el correo falla
        }

        redirectAttributes.addFlashAttribute("mensaje", "Alquiler cancelado exitosamente");
        return "redirect:/alquiler/mis-alquileres";
    }
    
    /**
     * Construye los detalles del alquiler para el correo electrónico
     */
    private String construirDetallesAlquiler(Alquiler alquiler) {
        StringBuilder detalles = new StringBuilder();
        
        if (alquiler.getItems() != null && !alquiler.getItems().isEmpty()) {
            detalles.append("<strong>Productos alquilados:</strong><br>");
            for (ItemCarrito item : alquiler.getItems()) {
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
    
    // Proponer costo de transporte (Usuario)
    @PostMapping("/proponer-transporte")
    @ResponseBody
    public java.util.Map<String, Object> proponerCostoTransporte(
            @RequestParam String alquilerId,
            @RequestParam Double costoTransporte,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
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
            
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que el alquiler pertenece al usuario
            if (!alquiler.getUsuarioId().equals(usuarioId)) {
                response.put("success", false);
                response.put("message", "No tienes permiso para modificar este alquiler");
                return response;
            }
            
            // Actualizar propuesta de transporte
            alquiler.setCostoTransporteUsuario(costoTransporte);
            alquiler.setEstadoTransporte("PROPUESTA_USUARIO");
            alquiler.setQuienPropuso("USUARIO");
            alquiler.setFechaPropuestaTransporte(LocalDateTime.now());
            
            alquilerRepositorio.save(alquiler);
            
            response.put("success", true);
            response.put("message", "Propuesta de transporte enviada al administrador");
            response.put("costoTransporte", costoTransporte);
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al proponer costo de transporte: " + e.getMessage());
            return response;
        }
    }
    
    // Aceptar contrapropuesta del admin (Usuario)
    @PostMapping("/aceptar-transporte-admin")
    @ResponseBody
    public java.util.Map<String, Object> aceptarCostoTransporteAdmin(
            @RequestParam String alquilerId,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Validar alquilerId
            if (alquilerId == null || alquilerId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de alquiler inválido");
                return response;
            }
            
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que el alquiler pertenece al usuario
            if (!alquiler.getUsuarioId().equals(usuarioId)) {
                response.put("success", false);
                response.put("message", "No tienes permiso para modificar este alquiler");
                return response;
            }
            
            // Verificar que hay una propuesta del admin
            if (!"PROPUESTA_ADMIN".equals(alquiler.getEstadoTransporte())) {
                response.put("success", false);
                response.put("message", "No hay propuesta del administrador para aceptar");
                return response;
            }
            
            // Aceptar propuesta del admin
            alquiler.setCostoTransporteAceptado(alquiler.getCostoTransporteAdmin());
            alquiler.setEstadoTransporte("ACEPTADO");
            alquiler.recalcularTotalConTransporte();
            
            alquilerRepositorio.save(alquiler);
            
            response.put("success", true);
            response.put("message", "Costo de transporte aceptado");
            response.put("costoTransporteAceptado", alquiler.getCostoTransporteAceptado());
            response.put("totalNuevo", alquiler.getTotal());
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al aceptar costo de transporte: " + e.getMessage());
            return response;
        }
    }
    
    // Rechazar contrapropuesta del admin (Usuario)
    @PostMapping("/rechazar-transporte-admin")
    @ResponseBody
    public java.util.Map<String, Object> rechazarCostoTransporteAdmin(
            @RequestParam String alquilerId,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión");
                return response;
            }
            
            // Validar alquilerId
            if (alquilerId == null || alquilerId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ID de alquiler inválido");
                return response;
            }
            
            Optional<Alquiler> alquilerOpt = alquilerRepositorio.findById(alquilerId);
            if (alquilerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Alquiler no encontrado");
                return response;
            }
            
            Alquiler alquiler = alquilerOpt.get();
            
            // Verificar que el alquiler pertenece al usuario
            if (!alquiler.getUsuarioId().equals(usuarioId)) {
                response.put("success", false);
                response.put("message", "No tienes permiso para modificar este alquiler");
                return response;
            }
            
            // Rechazar propuesta del admin
            alquiler.setEstadoTransporte("RECHAZADO");
            alquiler.setCostoTransporteAdmin(null);
            
            alquilerRepositorio.save(alquiler);
            
            response.put("success", true);
            response.put("message", "Propuesta de transporte rechazada");
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al rechazar costo de transporte: " + e.getMessage());
            return response;
        }
    }
}
