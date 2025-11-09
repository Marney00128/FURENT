package com.furniterental.servicio;

import com.furniterental.modelo.Alquiler;
import com.furniterental.modelo.Pago;
import com.furniterental.modelo.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía un comprobante de pago por correo electrónico
     */
    @SuppressWarnings("null")
    public void enviarComprobantePago(Usuario usuario, Pago pago, Alquiler alquiler) {
        if (usuario == null || pago == null || alquiler == null) {
            System.err.println("✗ Error: Parámetros nulos en enviarComprobantePago");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null) {
                helper.setFrom(fromEmail);
            }
            if (usuario.getCorreo() != null) {
                helper.setTo(usuario.getCorreo());
            }
            helper.setSubject("✓ Comprobante de Pago - FURENT");

            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("nombreUsuario", usuario.getNombre());
            context.setVariable("pagoId", pago.getId());
            context.setVariable("alquilerId", alquiler.getId());
            context.setVariable("tipoPago", pago.getTipoPago());
            context.setVariable("monto", pago.getMonto());
            context.setVariable("fechaPago", formatearFecha(pago.getFechaPago()));
            context.setVariable("metodoPago", pago.getMetodoPago());
            context.setVariable("estadoPago", pago.getEstado());

            // Procesar plantilla HTML
            String htmlContent = templateEngine.process("emails/comprobante-pago", context);
            helper.setText(htmlContent, true);

            // Enviar correo
            mailSender.send(message);
            
            System.out.println("✓ Comprobante de pago enviado a: " + usuario.getCorreo());

        } catch (MessagingException e) {
            System.err.println("✗ Error al enviar comprobante de pago: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Formatea una fecha para mostrarla en el correo
     */
    private String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fecha.format(formatter);
    }

    /**
     * Envía un correo de bienvenida al registrarse
     */
    @SuppressWarnings("null")
    public void enviarCorreoBienvenida(Usuario usuario) {
        if (usuario == null) {
            System.err.println("✗ Error: Usuario nulo en enviarCorreoBienvenida");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null) {
                helper.setFrom(fromEmail);
            }
            if (usuario.getCorreo() != null) {
                helper.setTo(usuario.getCorreo());
            }
            helper.setSubject("¡Bienvenido a FURENT! 🎉");

            Context context = new Context();
            context.setVariable("nombreUsuario", usuario.getNombre());

            String htmlContent = templateEngine.process("emails/bienvenida", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
            System.out.println("✓ Correo de bienvenida enviado a: " + usuario.getCorreo());

        } catch (MessagingException e) {
            System.err.println("✗ Error al enviar correo de bienvenida: " + e.getMessage());
        }
    }

    /**
     * Envía notificación de cambio de estado de alquiler
     */
    @SuppressWarnings("null")
    public void enviarNotificacionEstadoAlquiler(Usuario usuario, Alquiler alquiler, String nuevoEstado) {
        if (usuario == null || alquiler == null) {
            System.err.println("✗ Error: Parámetros nulos en enviarNotificacionEstadoAlquiler");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null) {
                helper.setFrom(fromEmail);
            }
            if (usuario.getCorreo() != null) {
                helper.setTo(usuario.getCorreo());
            }
            helper.setSubject("Actualización de tu alquiler - FURENT");

            Context context = new Context();
            context.setVariable("nombreUsuario", usuario.getNombre());
            context.setVariable("alquilerId", alquiler.getId());
            context.setVariable("nuevoEstado", nuevoEstado);

            String htmlContent = templateEngine.process("emails/estado-alquiler", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
            System.out.println("✓ Notificación de estado enviada a: " + usuario.getCorreo());

        } catch (MessagingException e) {
            System.err.println("✗ Error al enviar notificación de estado: " + e.getMessage());
        }
    }
}
