package com.furniterental.servicio;

import com.furniterental.modelo.Resena;
import com.furniterental.modelo.Producto;
import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.ResenaRepositorio;
import com.furniterental.repositorio.ProductoRepositorio;
import com.furniterental.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class ResenaServicio {
    
    @Autowired
    private ResenaRepositorio resenaRepositorio;
    
    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private EmailServicio emailServicio;
    
    /**
     * Crear una nueva reseña
     */
    public Resena crearResena(String alquilerId, String usuarioId, String productoId, 
                              int calificacion, String comentario) {
        
        // Validar parámetros no nulos
        if (alquilerId == null || alquilerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del alquiler no puede ser nulo o vacío");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío");
        }
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo o vacío");
        }
        
        // Validar calificación
        if (calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");
        }
        
        // Verificar si ya existe una reseña para este alquiler y producto específico
        if (resenaRepositorio.existsByAlquilerIdAndProductoId(alquilerId, productoId)) {
            throw new IllegalStateException("Ya has dejado una reseña para este producto en este alquiler");
        }
        
        // Obtener información del usuario
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        
        // Obtener información del producto
        Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
        if (!productoOpt.isPresent()) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
        Producto producto = productoOpt.get();
        
        // Crear la reseña
        Resena resena = new Resena();
        resena.setAlquilerId(alquilerId);
        resena.setUsuarioId(usuarioId);
        resena.setUsuarioNombre(usuario.getNombre());
        resena.setProductoId(productoId);
        resena.setProductoNombre(producto.getNombreProducto());
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setEstado("PENDIENTE");
        resena.setFechaCreacion(LocalDateTime.now());
        
        // Guardar la reseña
        Resena resenaSaved = resenaRepositorio.save(resena);
        
        // Enviar notificación por correo a la empresa
        enviarNotificacionResenaAEmpresa(resenaSaved);
        
        return resenaSaved;
    }
    
    /**
     * Obtener reseñas aprobadas de un producto
     */
    public List<Resena> obtenerResenasAprobadasPorProducto(String productoId) {
        return resenaRepositorio.findByProductoIdAndEstado(productoId, "APROBADA");
    }
    
    /**
     * Obtener reseñas por usuario
     */
    public List<Resena> obtenerResenasPorUsuario(String usuarioId) {
        return resenaRepositorio.findByUsuarioId(usuarioId);
    }
    
    /**
     * Obtener todas las reseñas de un alquiler
     */
    public List<Resena> obtenerResenasPorAlquiler(String alquilerId) {
        return resenaRepositorio.findByAlquilerId(alquilerId);
    }
    
    /**
     * Obtener todas las reseñas pendientes (para admin)
     */
    public List<Resena> obtenerResenasPendientes() {
        return resenaRepositorio.findByEstado("PENDIENTE");
    }
    
    /**
     * Aprobar una reseña
     */
    public Resena aprobarResena(String resenaId) {
        if (resenaId == null || resenaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la reseña no puede ser nulo o vacío");
        }
        Optional<Resena> resenaOpt = resenaRepositorio.findById(resenaId);
        if (!resenaOpt.isPresent()) {
            throw new IllegalArgumentException("Reseña no encontrada");
        }
        
        Resena resena = resenaOpt.get();
        resena.setEstado("APROBADA");
        return resenaRepositorio.save(resena);
    }
    
    /**
     * Rechazar una reseña
     */
    public Resena rechazarResena(String resenaId) {
        if (resenaId == null || resenaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la reseña no puede ser nulo o vacío");
        }
        Optional<Resena> resenaOpt = resenaRepositorio.findById(resenaId);
        if (!resenaOpt.isPresent()) {
            throw new IllegalArgumentException("Reseña no encontrada");
        }
        
        Resena resena = resenaOpt.get();
        resena.setEstado("RECHAZADA");
        return resenaRepositorio.save(resena);
    }
    
    /**
     * Responder a una reseña (admin)
     */
    public Resena responderResena(String resenaId, String respuesta) {
        if (resenaId == null || resenaId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la reseña no puede ser nulo o vacío");
        }
        Optional<Resena> resenaOpt = resenaRepositorio.findById(resenaId);
        if (!resenaOpt.isPresent()) {
            throw new IllegalArgumentException("Reseña no encontrada");
        }
        
        Resena resena = resenaOpt.get();
        resena.setRespuestaAdmin(respuesta);
        resena.setFechaRespuesta(LocalDateTime.now());
        return resenaRepositorio.save(resena);
    }
    
    /**
     * Calcular promedio de calificaciones de un producto
     */
    public Map<String, Object> obtenerEstadisticasProducto(String productoId) {
        List<Resena> resenas = obtenerResenasAprobadasPorProducto(productoId);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        if (resenas.isEmpty()) {
            estadisticas.put("promedioCalificacion", 0.0);
            estadisticas.put("totalResenas", 0);
            estadisticas.put("distribucionEstrellas", new int[]{0, 0, 0, 0, 0});
            return estadisticas;
        }
        
        // Calcular promedio
        double suma = 0;
        int[] distribucion = new int[5]; // Índices 0-4 para estrellas 1-5
        
        for (Resena resena : resenas) {
            suma += resena.getCalificacion();
            distribucion[resena.getCalificacion() - 1]++;
        }
        
        double promedio = suma / resenas.size();
        
        estadisticas.put("promedioCalificacion", Math.round(promedio * 10.0) / 10.0);
        estadisticas.put("totalResenas", resenas.size());
        estadisticas.put("distribucionEstrellas", distribucion);
        
        return estadisticas;
    }
    
    /**
     * Verificar si un usuario puede dejar reseña para un alquiler
     * Ahora siempre retorna true porque permitimos múltiples reseñas (una por producto)
     * La validación de duplicados se hace a nivel de producto individual
     */
    public boolean puedeDejarResena(String alquilerId) {
        // Siempre puede intentar dejar reseña
        // La validación de duplicados se maneja a nivel de producto individual
        return true;
    }
    
    /**
     * Enviar notificación de nueva reseña a la empresa
     */
    private void enviarNotificacionResenaAEmpresa(Resena resena) {
        try {
            String destinatario = "furent.empresa@gmail.com"; // Correo de la empresa
            String asunto = "Nueva reseña recibida - " + resena.getProductoNombre();
            
            String htmlContent = construirEmailNotificacionResena(resena);
            
            emailServicio.enviarCorreoHtml(destinatario, asunto, htmlContent);
            
            System.out.println("Notificación de reseña enviada a la empresa");
        } catch (Exception e) {
            System.err.println("Error al enviar notificación de reseña: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Construir HTML del correo de notificación de reseña
     */
    private String construirEmailNotificacionResena(Resena resena) {
        String estrellas = "⭐".repeat(resena.getCalificacion());
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .review-box {
                        background-color: #f8f9fa;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .stars {
                        font-size: 24px;
                        color: #ffc107;
                        margin: 10px 0;
                    }
                    .info-row {
                        margin: 10px 0;
                        padding: 10px 0;
                        border-bottom: 1px solid #e0e0e0;
                    }
                    .info-label {
                        font-weight: bold;
                        color: #667eea;
                        display: inline-block;
                        width: 150px;
                    }
                    .comment {
                        background-color: #fff;
                        padding: 15px;
                        border-radius: 5px;
                        border: 1px solid #e0e0e0;
                        margin-top: 15px;
                        font-style: italic;
                        color: #555;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #666;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📝 Nueva Reseña Recibida</h1>
                    </div>
                    
                    <div class="content">
                        <p>Se ha recibido una nueva reseña de un cliente:</p>
                        
                        <div class="review-box">
                            <div class="info-row">
                                <span class="info-label">Producto:</span>
                                <span>""" + resena.getProductoNombre() + """
                                </span>
                            </div>
                            
                            <div class="info-row">
                                <span class="info-label">Cliente:</span>
                                <span>""" + resena.getUsuarioNombre() + """
                                </span>
                            </div>
                            
                            <div class="info-row">
                                <span class="info-label">Calificación:</span>
                                <div class="stars">""" + estrellas + " (" + resena.getCalificacion() + "/5)" + """
                                </div>
                            </div>
                            
                            <div class="info-row">
                                <span class="info-label">Fecha:</span>
                                <span>""" + resena.getFechaCreacion().toString() + """
                                </span>
                            </div>
                            
                            <div class="info-row">
                                <span class="info-label">Estado:</span>
                                <span style="color: #ff9800; font-weight: bold;">PENDIENTE DE APROBACIÓN</span>
                            </div>
                            
                            <div class="comment">
                                <strong>Comentario:</strong><br><br>
                                """ + resena.getComentario() + """
                            </div>
                        </div>
                        
                        <p style="margin-top: 20px;">
                            Esta reseña está pendiente de aprobación. Puedes aprobarla o rechazarla desde el panel de administración.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>Este es un correo automático de FURENT</p>
                        <p>Sistema de Gestión de Reseñas</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
    
    /**
     * Crear reseña general para múltiples productos
     * (aplica la misma calificación y comentario a todos los productos)
     */
    public List<Resena> crearResenaMultiple(String alquilerId, String usuarioId, 
                                            List<String> productosIds, int calificacion, String comentario) {
        
        // Validar parámetros
        if (alquilerId == null || alquilerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del alquiler no puede ser nulo o vacío");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío");
        }
        if (productosIds == null || productosIds.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos un producto");
        }
        
        // Validar calificación
        if (calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");
        }
        
        // Obtener información del usuario
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        
        List<Resena> resenasCreadas = new java.util.ArrayList<>();
        
        // Crear una reseña para cada producto con la misma calificación y comentario
        for (String productoId : productosIds) {
            // Skip null or empty product IDs
            if (productoId == null || productoId.trim().isEmpty()) {
                continue;
            }
            
            // Verificar si ya existe reseña para este producto en este alquiler
            if (resenaRepositorio.existsByAlquilerIdAndProductoId(alquilerId, productoId)) {
                System.out.println("Ya existe reseña para producto: " + productoId + " en alquiler: " + alquilerId);
                continue; // Saltar productos ya reseñados
            }
            
            // Obtener información del producto
            Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
            if (!productoOpt.isPresent()) {
                System.out.println("Producto no encontrado: " + productoId);
                continue; // Saltar productos no encontrados
            }
            Producto producto = productoOpt.get();
            
            // Crear la reseña
            Resena resena = new Resena();
            resena.setAlquilerId(alquilerId);
            resena.setUsuarioId(usuarioId);
            resena.setUsuarioNombre(usuario.getNombre());
            resena.setProductoId(productoId);
            resena.setProductoNombre(producto.getNombreProducto());
            resena.setCalificacion(calificacion);
            resena.setComentario(comentario);
            resena.setEstado("PENDIENTE");
            resena.setFechaCreacion(LocalDateTime.now());
            
            // Guardar la reseña
            Resena resenaSaved = resenaRepositorio.save(resena);
            resenasCreadas.add(resenaSaved);
        }
        
        // Enviar notificación por correo a la empresa (solo una vez por todas las reseñas)
        if (!resenasCreadas.isEmpty()) {
            enviarNotificacionResenaMultipleAEmpresa(resenasCreadas);
        }
        
        return resenasCreadas;
    }
    
    /**
     * Enviar notificación de reseñas múltiples a la empresa
     */
    private void enviarNotificacionResenaMultipleAEmpresa(List<Resena> resenas) {
        try {
            String adminEmail = "furent.company@gmail.com"; // Reemplaza con el correo real
            String subject = "🌟 Nuevas Reseñas Recibidas - FURENT";
            
            // Primera reseña para obtener info general
            Resena primeraResena = resenas.get(0);
            
            // Crear HTML con todas las reseñas
            StringBuilder resenasHtml = new StringBuilder();
            for (Resena resena : resenas) {
                String estrellas = "★".repeat(resena.getCalificacion()) + "☆".repeat(5 - resena.getCalificacion());
                resenasHtml.append(String.format("""
                    <div style="padding: 15px; background-color: #f8f9fa; border-radius: 8px; margin-bottom: 10px;">
                        <p><strong>Producto:</strong> %s</p>
                        <p><strong>Calificación:</strong> %s (%d/5)</p>
                    </div>
                """, resena.getProductoNombre(), estrellas, resena.getCalificacion()));
            }
            
            String htmlMessage = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #8cbc00, #037bc0); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background-color: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
                        .footer { background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 10px 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1 style="margin: 0;">📝 Nuevas Reseñas Múltiples</h1>
                        </div>
                        
                        <div class="content">
                            <p>Se han recibido <strong>%d reseñas</strong> de <strong>%s</strong>:</p>
                            
                            %s
                            
                            <div style="margin-top: 20px; padding: 15px; background-color: #fff3cd; border-left: 4px solid #ffc107; border-radius: 5px;">
                                <p><strong>💬 Comentario General:</strong></p>
                                <p style="font-style: italic;">%s</p>
                            </div>
                            
                            <p style="margin-top: 20px;">
                                Estas reseñas están pendientes de aprobación. Puedes aprobarlas o rechazarlas desde el panel de administración.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p>Este es un correo automático de FURENT</p>
                            <p>Sistema de Gestión de Reseñas</p>
                        </div>
                    </div>
                </body>
                </html>
            """, resenas.size(), primeraResena.getUsuarioNombre(), resenasHtml.toString(), primeraResena.getComentario());
            
            emailServicio.enviarCorreoHtml(adminEmail, subject, htmlMessage);
            System.out.println("Notificación de reseñas múltiples enviada a: " + adminEmail);
            
        } catch (Exception e) {
            System.err.println("Error al enviar notificación de reseñas múltiples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
