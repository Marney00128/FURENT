package com.furniterental.servicio;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private EmailServicio emailServicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TwoFactorAuthServicio twoFactorAuthServicio;

    /**
     * Genera un código de 6 dígitos aleatorio
     */
    private String generarCodigoVerificacion() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }

    /**
     * Inicia el proceso de recuperación de contraseña
     * Genera y envía un código de verificación al correo del usuario
     */
    public boolean iniciarRecuperacion(String correo) {
        try {
            // Buscar usuario por correo
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                // Por seguridad, no revelamos si el correo existe o no
                return true;
            }

            Usuario usuario = usuarioOpt.get();

            // Generar código de verificación
            String codigo = generarCodigoVerificacion();
            
            // Guardar código y fecha de expiración (15 minutos)
            usuario.setCodigoVerificacion(codigo);
            usuario.setCodigoExpiracion(LocalDateTime.now().plusMinutes(15));
            usuarioRepositorio.save(usuario);

            // Enviar código por correo
            emailServicio.enviarCodigoVerificacion(
                usuario.getCorreo(),
                usuario.getNombre(),
                codigo
            );

            System.out.println("Código de recuperación generado para: " + correo);
            return true;

        } catch (Exception e) {
            System.err.println("Error al iniciar recuperación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica el código de recuperación ingresado por el usuario
     * Si el usuario tiene 2FA habilitado, también valida el código 2FA
     */
    public boolean verificarCodigo(String correo, String codigo, String codigo2FA) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                return false;
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que el código no haya expirado
            if (usuario.getCodigoExpiracion() == null || 
                LocalDateTime.now().isAfter(usuario.getCodigoExpiracion())) {
                System.out.println("Código expirado para: " + correo);
                return false;
            }

            // Verificar el código de recuperación
            if (!codigo.equals(usuario.getCodigoVerificacion())) {
                System.out.println("Código incorrecto para: " + correo);
                return false;
            }

            // Si el usuario tiene 2FA habilitado, verificar también el código 2FA
            if (usuario.isTwoFactorEnabled()) {
                if (codigo2FA == null || codigo2FA.trim().isEmpty()) {
                    System.out.println("Se requiere código 2FA para: " + correo);
                    return false;
                }

                boolean codigo2FAValido = twoFactorAuthServicio.verifyCode(
                    usuario.getTwoFactorSecret(), 
                    codigo2FA
                );

                if (!codigo2FAValido) {
                    System.out.println("Código 2FA incorrecto para: " + correo);
                    return false;
                }
            }

            System.out.println("Códigos verificados exitosamente para: " + correo);
            return true;

        } catch (Exception e) {
            System.err.println("Error al verificar código: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restablece la contraseña del usuario
     */
    public boolean restablecerContrasena(String correo, String nuevaContrasena) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                return false;
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que el código no haya expirado
            if (usuario.getCodigoExpiracion() == null || 
                LocalDateTime.now().isAfter(usuario.getCodigoExpiracion())) {
                System.out.println("Código expirado, no se puede restablecer contraseña");
                return false;
            }

            // Encriptar la nueva contraseña
            String contrasenaEncriptada = passwordEncoder.encode(nuevaContrasena);
            usuario.setContrasena(contrasenaEncriptada);

            // Limpiar código de verificación
            usuario.setCodigoVerificacion(null);
            usuario.setCodigoExpiracion(null);

            usuarioRepositorio.save(usuario);

            System.out.println("Contraseña restablecida exitosamente para: " + correo);
            return true;

        } catch (Exception e) {
            System.err.println("Error al restablecer contraseña: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si un usuario tiene 2FA habilitado
     */
    public boolean usuarioTiene2FA(String correo) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
            return usuarioOpt.isPresent() && usuarioOpt.get().isTwoFactorEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida la fortaleza de la contraseña
     */
    public String validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }
        
        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;
        boolean tieneCaracterEspecial = false;
        
        for (char c : contrasena.toCharArray()) {
            if (Character.isUpperCase(c)) {
                tieneMayuscula = true;
            } else if (Character.isLowerCase(c)) {
                tieneMinuscula = true;
            } else if (Character.isDigit(c)) {
                tieneNumero = true;
            } else if (!Character.isLetterOrDigit(c)) {
                tieneCaracterEspecial = true;
            }
        }
        
        if (!tieneMayuscula) {
            return "La contraseña debe contener al menos una letra mayúscula";
        }
        if (!tieneMinuscula) {
            return "La contraseña debe contener al menos una letra minúscula";
        }
        if (!tieneNumero) {
            return "La contraseña debe contener al menos un número";
        }
        if (!tieneCaracterEspecial) {
            return "La contraseña debe contener al menos un carácter especial";
        }
        
        return null; // Contraseña válida
    }
}
