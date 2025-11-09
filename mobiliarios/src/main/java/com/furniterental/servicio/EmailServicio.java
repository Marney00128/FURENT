package com.furniterental.servicio;

import com.furniterental.config.MailProperties;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServicio {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProperties mailProperties;

    /**
     * Envía un correo de notificación de cambio de estado de alquiler
     */
    public void enviarNotificacionCambioEstado(
            String destinatario,
            String nombreUsuario,
            String numeroAlquiler,
            String estadoAnterior,
            String estadoNuevo,
            String detallesAlquiler) {
        
        // Validar parámetros requeridos
        if (destinatario == null || destinatario.isEmpty()) {
            System.err.println("Error: destinatario es nulo o vacío");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Asegurar valores no nulos para el remitente
            String fromEmail = mailProperties.getFrom();
            if (fromEmail == null || fromEmail.isEmpty()) {
                fromEmail = "noreply@furent.com";
            }
            
            String fromName = mailProperties.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                fromName = "FURENT";
            }
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject("Actualización de tu alquiler #" + numeroAlquiler + " - FURENT");

            String htmlContent = construirEmailCambioEstado(
                nombreUsuario, 
                numeroAlquiler, 
                estadoAnterior, 
                estadoNuevo, 
                detallesAlquiler
            );

            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(htmlContent, true);
            } else {
                throw new IllegalStateException("El contenido del correo no puede estar vacío");
            }

            mailSender.send(message);
            System.out.println("Correo enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Construye el contenido HTML del correo
     */
    private String construirEmailCambioEstado(
            String nombreUsuario,
            String numeroAlquiler,
            String estadoAnterior,
            String estadoNuevo,
            String detallesAlquiler) {

        // Valores por defecto para evitar nulls
        String nombre = nombreUsuario != null ? nombreUsuario : "Usuario";
        String numero = numeroAlquiler != null ? numeroAlquiler : "N/A";
        String anterior = estadoAnterior != null ? estadoAnterior : "N/A";
        String nuevo = estadoNuevo != null ? estadoNuevo : "PENDIENTE";
        String detalles = detallesAlquiler != null ? detallesAlquiler : "Sin detalles";
        
        String colorEstado = obtenerColorEstado(nuevo);
        String iconoEstado = obtenerIconoEstado(nuevo);
        String mensajeEstado = obtenerMensajeEstado(nuevo);

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Actualización de Alquiler</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #8cbc00 0%%, #6a9600 100%%); padding: 30px; text-align: center;">
                                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                                            🏠 FURENT
                                        </h1>
                                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 14px;">
                                            Alquiler de Mobiliario Premium
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px;">
                                            ¡Hola, %s!
                                        </h2>
                                        
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                            Te informamos que el estado de tu alquiler ha sido actualizado.
                                        </p>
                                        
                                        <!-- Estado Badge -->
                                        <div style="background-color: #f8f9fa; border-left: 4px solid %s; padding: 20px; margin: 20px 0; border-radius: 5px;">
                                            <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">
                                                <strong>Número de Alquiler:</strong> #%s
                                            </p>
                                            <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">
                                                <strong>Estado Anterior:</strong> <span style="color: #999999;">%s</span>
                                            </p>
                                            <p style="margin: 0; color: #666666; font-size: 14px;">
                                                <strong>Nuevo Estado:</strong> 
                                                <span style="color: %s; font-weight: bold; font-size: 16px;">
                                                    %s %s
                                                </span>
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                                            <p style="margin: 0; color: #2e7d32; font-size: 14px; line-height: 1.6;">
                                                %s
                                            </p>
                                        </div>
                                        
                                        <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 20px 0 0 0;">
                                            <strong>Detalles del alquiler:</strong><br>
                                            %s
                                        </p>
                                        
                                        <!-- CTA Button -->
                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="http://localhost:8080/mis-alquileres" 
                                               style="display: inline-block; background-color: #8cbc00; color: #ffffff; 
                                                      padding: 15px 40px; text-decoration: none; border-radius: 5px; 
                                                      font-weight: bold; font-size: 16px;">
                                                Ver mis alquileres
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                        <p style="color: #999999; font-size: 12px; margin: 0 0 10px 0;">
                                            Este es un correo automático, por favor no responder.
                                        </p>
                                        <p style="color: #999999; font-size: 12px; margin: 0;">
                                            © 2024 FURENT - Alquiler de Mobiliario Premium<br>
                                            📞 +57 300 123 4567 | 📧 info@furent.com
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                nombre,
                colorEstado,
                numero,
                anterior,
                colorEstado,
                iconoEstado,
                nuevo,
                mensajeEstado,
                detalles
            );
    }

    /**
     * Obtiene el color según el estado
     */
    private String obtenerColorEstado(String estado) {
        return switch (estado.toUpperCase()) {
            case "PENDIENTE" -> "#ffc107";
            case "CONFIRMADO" -> "#2196f3";
            case "EN PREPARACIÓN" -> "#ff9800";
            case "EN CAMINO" -> "#9c27b0";
            case "ENTREGADO" -> "#4caf50";
            case "COMPLETADO" -> "#4caf50";
            case "CANCELADO" -> "#f44336";
            default -> "#757575";
        };
    }

    /**
     * Obtiene el icono según el estado
     */
    private String obtenerIconoEstado(String estado) {
        return switch (estado.toUpperCase()) {
            case "PENDIENTE" -> "⏳";
            case "CONFIRMADO" -> "✅";
            case "EN PREPARACIÓN" -> "📦";
            case "EN CAMINO" -> "🚚";
            case "ENTREGADO" -> "🎉";
            case "COMPLETADO" -> "✔️";
            case "CANCELADO" -> "❌";
            default -> "📋";
        };
    }

    /**
     * Obtiene el mensaje según el estado
     */
    private String obtenerMensajeEstado(String estado) {
        return switch (estado.toUpperCase()) {
            case "PENDIENTE" -> "Tu alquiler está pendiente de confirmación. Te notificaremos cuando sea procesado.";
            case "CONFIRMADO" -> "¡Excelente! Tu alquiler ha sido confirmado y está siendo preparado.";
            case "EN PREPARACIÓN" -> "Estamos preparando tu pedido con mucho cuidado para garantizar la mejor calidad.";
            case "EN CAMINO" -> "Tu pedido está en camino. Pronto llegará a tu dirección.";
            case "ENTREGADO" -> "¡Tu pedido ha sido entregado! Esperamos que disfrutes de nuestro mobiliario.";
            case "COMPLETADO" -> "Tu alquiler ha sido completado exitosamente. ¡Gracias por confiar en FURENT!";
            case "CANCELADO" -> "Tu alquiler ha sido cancelado. Si tienes dudas, contáctanos.";
            default -> "El estado de tu alquiler ha sido actualizado.";
        };
    }
    
    /**
     * Envía un correo de confirmación cuando se crea un nuevo alquiler
     */
    public void enviarConfirmacionAlquiler(
            String destinatario,
            String nombreUsuario,
            String numeroAlquiler,
            String detallesAlquiler) {
        
        // Validar parámetros requeridos
        if (destinatario == null || destinatario.isEmpty()) {
            System.err.println("Error: destinatario es nulo o vacío");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Asegurar valores no nulos para el remitente
            String fromEmail = mailProperties.getFrom();
            if (fromEmail == null || fromEmail.isEmpty()) {
                fromEmail = "noreply@furent.com";
            }
            
            String fromName = mailProperties.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                fromName = "FURENT";
            }
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject("¡Alquiler Recibido! #" + numeroAlquiler + " - FURENT");

            String htmlContent = construirEmailConfirmacionAlquiler(
                nombreUsuario, 
                numeroAlquiler, 
                detallesAlquiler
            );

            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(htmlContent, true);
            } else {
                throw new IllegalStateException("El contenido del correo no puede estar vacío");
            }

            mailSender.send(message);
            System.out.println("Correo de confirmación enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error al enviar correo de confirmación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Construye el contenido HTML del correo de confirmación de alquiler
     */
    private String construirEmailConfirmacionAlquiler(
            String nombreUsuario,
            String numeroAlquiler,
            String detallesAlquiler) {

        // Valores por defecto para evitar nulls
        String nombre = nombreUsuario != null ? nombreUsuario : "Usuario";
        String numero = numeroAlquiler != null ? numeroAlquiler : "N/A";
        String detalles = detallesAlquiler != null ? detallesAlquiler : "Sin detalles";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Confirmación de Alquiler</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #8cbc00 0%%, #6a9600 100%%); padding: 30px; text-align: center;">
                                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                                            🏠 FURENT
                                        </h1>
                                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 14px;">
                                            Alquiler de Mobiliario Premium
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px;">
                                            ¡Hola, %s! 👋
                                        </h2>
                                        
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                            ¡Gracias por tu confianza! Hemos recibido tu solicitud de alquiler exitosamente.
                                        </p>
                                        
                                        <!-- Success Badge -->
                                        <div style="background-color: #e8f5e9; border-left: 4px solid #4caf50; padding: 20px; margin: 20px 0; border-radius: 5px;">
                                            <p style="margin: 0 0 10px 0; color: #2e7d32; font-size: 18px; font-weight: bold;">
                                                ✅ ¡Alquiler Recibido!
                                            </p>
                                            <p style="margin: 0; color: #666666; font-size: 14px;">
                                                <strong>Número de Alquiler:</strong> #%s
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                ⏳ <strong>Estado:</strong> PENDIENTE<br>
                                                Tu solicitud está siendo revisada por nuestro equipo. Te notificaremos cuando sea confirmada.
                                            </p>
                                        </div>
                                        
                                        <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 20px 0 0 0;">
                                            <strong>Detalles de tu alquiler:</strong><br>
                                            %s
                                        </p>
                                        
                                        <!-- Info Box -->
                                        <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                                            <p style="margin: 0; color: #1565c0; font-size: 14px; line-height: 1.6;">
                                                💡 <strong>¿Qué sigue?</strong><br>
                                                • Revisaremos tu solicitud<br>
                                                • Te confirmaremos la disponibilidad<br>
                                                • Prepararemos tu pedido<br>
                                                • Te notificaremos en cada paso
                                            </p>
                                        </div>
                                        
                                        <!-- CTA Button -->
                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="http://localhost:8080/alquiler/mis-alquileres" 
                                               style="display: inline-block; background-color: #8cbc00; color: #ffffff; 
                                                      padding: 15px 40px; text-decoration: none; border-radius: 5px; 
                                                      font-weight: bold; font-size: 16px;">
                                                Ver Mi Alquiler
                                            </a>
                                        </div>
                                        
                                        <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 20px 0 0 0; text-align: center;">
                                            Si tienes alguna pregunta, no dudes en contactarnos.<br>
                                            Estamos aquí para ayudarte. 😊
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                        <p style="color: #999999; font-size: 12px; margin: 0 0 10px 0;">
                                            Este es un correo automático, por favor no responder.
                                        </p>
                                        <p style="color: #999999; font-size: 12px; margin: 0;">
                                            © 2024 FURENT - Alquiler de Mobiliario Premium<br>
                                            📞 +57 300 123 4567 | 📧 info@furent.com
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                nombre,
                numero,
                detalles
            );
    }
    
    /**
     * Envía un correo de notificación cuando el usuario cancela su alquiler
     */
    public void enviarNotificacionCancelacion(
            String destinatario,
            String nombreUsuario,
            String numeroAlquiler,
            String detallesAlquiler) {
        
        // Validar parámetros requeridos
        if (destinatario == null || destinatario.isEmpty()) {
            System.err.println("Error: destinatario es nulo o vacío");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Asegurar valores no nulos para el remitente
            String fromEmail = mailProperties.getFrom();
            if (fromEmail == null || fromEmail.isEmpty()) {
                fromEmail = "noreply@furent.com";
            }
            
            String fromName = mailProperties.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                fromName = "FURENT";
            }
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject("Alquiler Cancelado #" + numeroAlquiler + " - FURENT");

            String htmlContent = construirEmailCancelacion(
                nombreUsuario, 
                numeroAlquiler, 
                detallesAlquiler
            );

            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(htmlContent, true);
            } else {
                throw new IllegalStateException("El contenido del correo no puede estar vacío");
            }

            mailSender.send(message);
            System.out.println("Correo de cancelación enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error al enviar correo de cancelación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Construye el contenido HTML del correo de cancelación
     */
    private String construirEmailCancelacion(
            String nombreUsuario,
            String numeroAlquiler,
            String detallesAlquiler) {

        // Valores por defecto para evitar nulls
        String nombre = nombreUsuario != null ? nombreUsuario : "Usuario";
        String numero = numeroAlquiler != null ? numeroAlquiler : "N/A";
        String detalles = detallesAlquiler != null ? detallesAlquiler : "Sin detalles";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Alquiler Cancelado</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #8cbc00 0%%, #6a9600 100%%); padding: 30px; text-align: center;">
                                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                                            🏠 FURENT
                                        </h1>
                                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 14px;">
                                            Alquiler de Mobiliario Premium
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px;">
                                            Hola, %s
                                        </h2>
                                        
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                            Te confirmamos que tu alquiler ha sido cancelado exitosamente.
                                        </p>
                                        
                                        <!-- Cancelled Badge -->
                                        <div style="background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 20px; margin: 20px 0; border-radius: 5px;">
                                            <p style="margin: 0 0 10px 0; color: #721c24; font-size: 18px; font-weight: bold;">
                                                ❌ Alquiler Cancelado
                                            </p>
                                            <p style="margin: 0; color: #666666; font-size: 14px;">
                                                <strong>Número de Alquiler:</strong> #%s
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                ℹ️ <strong>Información importante:</strong><br>
                                                • El stock de los productos ha sido restaurado<br>
                                                • No se realizará ningún cargo<br>
                                                • Puedes realizar un nuevo alquiler cuando lo desees
                                            </p>
                                        </div>
                                        
                                        <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 20px 0 0 0;">
                                            <strong>Detalles del alquiler cancelado:</strong><br>
                                            %s
                                        </p>
                                        
                                        <!-- CTA Button -->
                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="http://localhost:8080/" 
                                               style="display: inline-block; background-color: #8cbc00; color: #ffffff; 
                                                      padding: 15px 40px; text-decoration: none; border-radius: 5px; 
                                                      font-weight: bold; font-size: 16px;">
                                                Explorar Productos
                                            </a>
                                        </div>
                                        
                                        <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 20px 0 0 0; text-align: center;">
                                            Si tienes alguna pregunta, no dudes en contactarnos.<br>
                                            Esperamos verte pronto. 😊
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                        <p style="color: #999999; font-size: 12px; margin: 0 0 10px 0;">
                                            Este es un correo automático, por favor no responder.
                                        </p>
                                        <p style="color: #999999; font-size: 12px; margin: 0;">
                                            © 2024 FURENT - Alquiler de Mobiliario Premium<br>
                                            📞 +57 300 123 4567 | 📧 info@furent.com
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                nombre,
                numero,
                detalles
            );
    }
    
    /**
     * Envía un correo con el código de verificación para cambio de contraseña
     */
    public void enviarCodigoVerificacion(
            String destinatario,
            String nombreUsuario,
            String codigoVerificacion) {
        
        // Validar parámetros requeridos
        if (destinatario == null || destinatario.isEmpty()) {
            System.err.println("Error: destinatario es nulo o vacío");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Asegurar valores no nulos para el remitente
            String fromEmail = mailProperties.getFrom();
            if (fromEmail == null || fromEmail.isEmpty()) {
                fromEmail = "noreply@furent.com";
            }
            
            String fromName = mailProperties.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                fromName = "FURENT";
            }
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject("Código de Verificación - Cambio de Contraseña - FURENT");

            String htmlContent = construirEmailCodigoVerificacion(
                nombreUsuario, 
                codigoVerificacion
            );

            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(htmlContent, true);
            } else {
                throw new IllegalStateException("El contenido del correo no puede estar vacío");
            }

            mailSender.send(message);
            System.out.println("Código de verificación enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error al enviar código de verificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Construye el contenido HTML del correo con el código de verificación
     */
    private String construirEmailCodigoVerificacion(
            String nombreUsuario,
            String codigoVerificacion) {

        // Valores por defecto para evitar nulls
        String nombre = nombreUsuario != null ? nombreUsuario : "Usuario";
        String codigo = codigoVerificacion != null ? codigoVerificacion : "000000";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Código de Verificación</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #8cbc00 0%%, #6a9600 100%%); padding: 30px; text-align: center;">
                                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                                            🔒 FURENT
                                        </h1>
                                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 14px;">
                                            Seguridad de tu Cuenta
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px;">
                                            ¡Hola, %s! 👋
                                        </h2>
                                        
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                            Has solicitado cambiar tu contraseña. Para continuar, utiliza el siguiente código de verificación:
                                        </p>
                                        
                                        <!-- Código de Verificación -->
                                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; margin: 30px 0; border-radius: 10px; text-align: center;">
                                            <p style="margin: 0 0 10px 0; color: #ffffff; font-size: 14px; font-weight: 600; letter-spacing: 1px;">
                                                TU CÓDIGO DE VERIFICACIÓN
                                            </p>
                                            <p style="margin: 0; color: #ffffff; font-size: 42px; font-weight: bold; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                                                %s
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                ⏰ <strong>Importante:</strong> Este código expirará en <strong>15 minutos</strong>.
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #f8d7da; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #dc3545;">
                                            <p style="margin: 0; color: #721c24; font-size: 14px; line-height: 1.6;">
                                                🛡️ <strong>Seguridad:</strong><br>
                                                • Si no solicitaste este cambio, ignora este correo<br>
                                                • Nunca compartas este código con nadie<br>
                                                • FURENT nunca te pedirá este código por teléfono o email
                                            </p>
                                        </div>
                                        
                                        <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 20px 0 0 0; text-align: center;">
                                            Si tienes alguna pregunta sobre la seguridad de tu cuenta,<br>
                                            contáctanos inmediatamente. 🔐
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                        <p style="color: #999999; font-size: 12px; margin: 0 0 10px 0;">
                                            Este es un correo automático, por favor no responder.
                                        </p>
                                        <p style="color: #999999; font-size: 12px; margin: 0;">
                                            © 2024 FURENT - Alquiler de Mobiliario Premium<br>
                                            📞 +57 300 123 4567 | 📧 info@furent.com
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                nombre,
                codigo
            );
    }
    
    /**
     * Envía un correo de confirmación cuando la contraseña ha sido actualizada
     */
    public void enviarConfirmacionCambioContrasena(
            String destinatario,
            String nombreUsuario) {
        
        // Validar parámetros requeridos
        if (destinatario == null || destinatario.isEmpty()) {
            System.err.println("Error: destinatario es nulo o vacío");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Asegurar valores no nulos para el remitente
            String fromEmail = mailProperties.getFrom();
            if (fromEmail == null || fromEmail.isEmpty()) {
                fromEmail = "noreply@furent.com";
            }
            
            String fromName = mailProperties.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                fromName = "FURENT";
            }
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject("Contraseña Actualizada - FURENT");

            String htmlContent = construirEmailConfirmacionCambioContrasena(nombreUsuario);

            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(htmlContent, true);
            } else {
                throw new IllegalStateException("El contenido del correo no puede estar vacío");
            }

            mailSender.send(message);
            System.out.println("Confirmación de cambio de contraseña enviada a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error al enviar confirmación de cambio de contraseña: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Construye el contenido HTML del correo de confirmación de cambio de contraseña
     */
    private String construirEmailConfirmacionCambioContrasena(String nombreUsuario) {

        // Valores por defecto para evitar nulls
        String nombre = nombreUsuario != null ? nombreUsuario : "Usuario";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Contraseña Actualizada</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #8cbc00 0%%, #6a9600 100%%); padding: 30px; text-align: center;">
                                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                                            ✅ FURENT
                                        </h1>
                                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 14px;">
                                            Seguridad de tu Cuenta
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px;">
                                            ¡Hola, %s! 👋
                                        </h2>
                                        
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                            Te confirmamos que tu contraseña ha sido actualizada exitosamente.
                                        </p>
                                        
                                        <!-- Success Badge -->
                                        <div style="background: linear-gradient(135deg, #4caf50 0%%, #2e7d32 100%%); padding: 30px; margin: 30px 0; border-radius: 10px; text-align: center;">
                                            <p style="margin: 0 0 10px 0; color: #ffffff; font-size: 18px; font-weight: 600;">
                                                🔒 CONTRASEÑA ACTUALIZADA
                                            </p>
                                            <p style="margin: 0; color: #ffffff; font-size: 14px; opacity: 0.9;">
                                                Tu cuenta ahora está protegida con tu nueva contraseña
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #2196f3;">
                                            <p style="margin: 0; color: #1565c0; font-size: 14px; line-height: 1.6;">
                                                ℹ️ <strong>Información:</strong><br>
                                                • Fecha y hora: Ahora mismo<br>
                                                • Puedes usar tu nueva contraseña de inmediato<br>
                                                • Asegúrate de recordarla o guardarla en un lugar seguro
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                ⚠️ <strong>¿No fuiste tú?</strong><br>
                                                Si no realizaste este cambio, tu cuenta podría estar comprometida.<br>
                                                Contáctanos inmediatamente al +57 300 123 4567 o info@furent.com
                                            </p>
                                        </div>
                                        
                                        <!-- CTA Button -->
                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="http://localhost:8080/login" 
                                               style="display: inline-block; background-color: #8cbc00; color: #ffffff; 
                                                      padding: 15px 40px; text-decoration: none; border-radius: 5px; 
                                                      font-weight: bold; font-size: 16px;">
                                                Iniciar Sesión
                                            </a>
                                        </div>
                                        
                                        <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 20px 0 0 0; text-align: center;">
                                            Gracias por mantener tu cuenta segura. 🔐
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                        <p style="color: #999999; font-size: 12px; margin: 0 0 10px 0;">
                                            Este es un correo automático, por favor no responder.
                                        </p>
                                        <p style="color: #999999; font-size: 12px; margin: 0;">
                                            © 2024 FURENT - Alquiler de Mobiliario Premium<br>
                                            📞 +57 300 123 4567 | 📧 info@furent.com
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(nombre);
    }
    
    /**
     * Método genérico para enviar correos HTML
     */
    public void enviarCorreoHtml(String destinatario, String asunto, String contenidoHtml) {
        try {
            // Validar que el destinatario no sea nulo o vacío
            if (destinatario == null || destinatario.trim().isEmpty()) {
                System.err.println("Error: El destinatario del correo no puede ser nulo o vacío");
                return;
            }
            
            // Validar que el asunto no sea nulo
            if (asunto == null || asunto.trim().isEmpty()) {
                System.err.println("Error: El asunto del correo no puede ser nulo o vacío");
                return;
            }
            
            // Validar que el contenido no sea nulo
            if (contenidoHtml == null || contenidoHtml.trim().isEmpty()) {
                System.err.println("Error: El contenido del correo no puede ser nulo o vacío");
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String fromEmail = mailProperties.getFrom();
            if (fromEmail == null || fromEmail.isEmpty()) {
                fromEmail = "noreply@furent.com";
            }
            
            String fromName = mailProperties.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                fromName = "FURENT";
            }
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(message);
            System.out.println("Correo enviado exitosamente a: " + destinatario);
            
        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Envía notificación de solicitud de pago al usuario
     */
    public void enviarNotificacionSolicitudPago(
            String destinatario,
            String nombreUsuario,
            String numeroAlquiler,
            double montoPagar) {
        
        // Validar parámetros requeridos
        if (destinatario == null || destinatario.isEmpty()) {
            System.err.println("Error: destinatario es nulo o vacío");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Asegurar valores no nulos para el remitente
            String fromEmail = mailProperties.getFrom();
            if (fromEmail == null || fromEmail.isEmpty()) {
                fromEmail = "noreply@furent.com";
            }
            
            String fromName = mailProperties.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                fromName = "FURENT";
            }
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject("Solicitud de Pago - Alquiler #" + numeroAlquiler + " - FURENT");

            String htmlContent = construirEmailSolicitudPago(
                nombreUsuario, 
                numeroAlquiler, 
                montoPagar
            );

            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(htmlContent, true);
            } else {
                throw new IllegalStateException("El contenido del correo no puede estar vacío");
            }

            mailSender.send(message);
            System.out.println("Notificación de pago enviada exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error al enviar notificación de pago: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Construye el contenido HTML del correo de solicitud de pago
     */
    private String construirEmailSolicitudPago(
            String nombreUsuario,
            String numeroAlquiler,
            double montoPagar) {

        // Valores por defecto para evitar nulls
        String nombre = nombreUsuario != null ? nombreUsuario : "Usuario";
        String numero = numeroAlquiler != null ? numeroAlquiler : "N/A";
        String monto = String.format("$%.2f", montoPagar);

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Solicitud de Pago</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #8cbc00 0%%, #6a9600 100%%); padding: 30px; text-align: center;">
                                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                                            💳 FURENT
                                        </h1>
                                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 14px;">
                                            Solicitud de Pago
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px;">
                                            ¡Hola, %s! 👋
                                        </h2>
                                        
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                            Tu alquiler ha sido confirmado. Para continuar con el proceso, necesitamos que realices el pago inicial del 50%%.
                                        </p>
                                        
                                        <!-- Payment Info -->
                                        <div style="background-color: #e3f2fd; border-left: 4px solid #2196f3; padding: 20px; margin: 20px 0; border-radius: 5px;">
                                            <p style="margin: 0 0 10px 0; color: #1565c0; font-size: 18px; font-weight: bold;">
                                                💰 Información de Pago
                                            </p>
                                            <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">
                                                <strong>Número de Alquiler:</strong> #%s
                                            </p>
                                            <p style="margin: 0; color: #666666; font-size: 14px;">
                                                <strong>Monto a Pagar (50%%):</strong> 
                                                <span style="color: #2196f3; font-weight: bold; font-size: 24px;">%s</span>
                                            </p>
                                        </div>
                                        
                                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                ℹ️ <strong>Importante:</strong><br>
                                                • El pago inicial es del 50%% del total<br>
                                                • El 50%% restante se paga al finalizar el alquiler<br>
                                                • Una vez realizado el pago, prepararemos tu pedido
                                            </p>
                                        </div>
                                        
                                        <!-- CTA Button -->
                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="http://localhost:8080/mis-alquileres" 
                                               style="display: inline-block; background-color: #8cbc00; color: #ffffff; 
                                                      padding: 15px 40px; text-decoration: none; border-radius: 5px; 
                                                      font-weight: bold; font-size: 16px;">
                                                Realizar Pago
                                            </a>
                                        </div>
                                        
                                        <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 20px 0 0 0; text-align: center;">
                                            Si tienes alguna pregunta, no dudes en contactarnos.<br>
                                            Estamos aquí para ayudarte. 😊
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                        <p style="color: #999999; font-size: 12px; margin: 0 0 10px 0;">
                                            Este es un correo automático, por favor no responder.
                                        </p>
                                        <p style="color: #999999; font-size: 12px; margin: 0;">
                                            © 2024 FURENT - Alquiler de Mobiliario Premium<br>
                                            📞 +57 300 123 4567 | 📧 info@furent.com
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                nombre,
                numero,
                monto
            );
    }
}
