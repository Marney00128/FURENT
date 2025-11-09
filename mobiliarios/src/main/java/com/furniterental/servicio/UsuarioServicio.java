package com.furniterental.servicio;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private EmailServicio emailServicio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TwoFactorAuthServicio twoFactorAuthServicio;

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepositorio.findAll();
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // Encriptar la contraseña antes de guardar
        if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        // Establecer la fecha de registro como el primer inicio de sesión
        usuario.setUltimoInicioSesion(LocalDateTime.now());
        return usuarioRepositorio.save(usuario);
    }

    public Optional<Usuario> login(@NonNull String correo, @NonNull String contrasena) {
        System.out.println("DEBUG LOGIN: Intentando login con correo: " + correo);
        
        // Buscar usuario por correo
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println("DEBUG LOGIN: Usuario encontrado en BD");
            
            // Verificar la contraseña usando BCrypt
            if (passwordEncoder.matches(contrasena, usuario.getContrasena())) {
                System.out.println("DEBUG LOGIN: Login exitoso - contraseña correcta");
                // Actualizar último inicio de sesión
                usuario.setUltimoInicioSesion(LocalDateTime.now());
                usuarioRepositorio.save(usuario);
                return Optional.of(usuario);
            } else {
                System.out.println("DEBUG LOGIN: Login fallido - contraseña incorrecta");
            }
        } else {
            System.out.println("DEBUG LOGIN: Usuario NO encontrado en BD");
        }
        
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorCorreo(@NonNull String correo) {
        return usuarioRepositorio.findByCorreo(correo);
    }
    
    public Optional<Usuario> buscarPorId(@NonNull String id) {
        return usuarioRepositorio.findById(id);
    }
    
    /**
     * Actualiza la información del perfil del usuario
     */
    public Usuario actualizarPerfil(@NonNull String usuarioId, @NonNull String nombre, @NonNull String telefono) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setNombre(nombre);
            usuario.setTelefono(telefono);
            return usuarioRepositorio.save(usuario);
        }
        return null;
    }
    
    /**
     * Genera y envía un código de verificación al correo del usuario
     */
    public boolean generarYEnviarCodigoVerificacion(@NonNull String usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Generar código de 6 dígitos
            String codigo = generarCodigoAleatorio();
            
            // Establecer expiración en 15 minutos
            LocalDateTime expiracion = LocalDateTime.now().plusMinutes(15);
            
            // Guardar código y expiración en el usuario
            usuario.setCodigoVerificacion(codigo);
            usuario.setCodigoExpiracion(expiracion);
            usuarioRepositorio.save(usuario);
            
            // Enviar correo con el código
            emailServicio.enviarCodigoVerificacion(
                usuario.getCorreo(),
                usuario.getNombre(),
                codigo
            );
            
            return true;
        }
        return false;
    }
    
    /**
     * Genera un código aleatorio de 6 dígitos
     */
    private String generarCodigoAleatorio() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }
    
    /**
     * Valida el código de verificación
     */
    public boolean validarCodigoVerificacion(@NonNull String usuarioId, @NonNull String codigo) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Verificar que el código exista
            if (usuario.getCodigoVerificacion() == null) {
                return false;
            }
            
            // Verificar que el código coincida
            if (!usuario.getCodigoVerificacion().equals(codigo)) {
                return false;
            }
            
            // Verificar que no haya expirado
            if (usuario.getCodigoExpiracion() == null || 
                LocalDateTime.now().isAfter(usuario.getCodigoExpiracion())) {
                return false;
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Cambia la contraseña del usuario usando código de verificación
     */
    public boolean cambiarContrasenaConCodigo(@NonNull String usuarioId, @NonNull String codigo, @NonNull String nuevaContrasena) {
        // Primero validar el código
        if (!validarCodigoVerificacion(usuarioId, codigo)) {
            System.out.println("DEBUG: Código de verificación inválido");
            return false;
        }
        
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            System.out.println("DEBUG: Cambiando contraseña para usuario: " + usuario.getCorreo());
            System.out.println("DEBUG: Nueva contraseña (texto plano): " + nuevaContrasena);
            
            // Encriptar la nueva contraseña antes de guardar
            String contrasenaEncriptada = passwordEncoder.encode(nuevaContrasena);
            System.out.println("DEBUG: Nueva contraseña (encriptada): " + contrasenaEncriptada);
            
            usuario.setContrasena(contrasenaEncriptada);
            
            // Limpiar el código de verificación
            usuario.setCodigoVerificacion(null);
            usuario.setCodigoExpiracion(null);
            
            usuarioRepositorio.save(usuario);
            System.out.println("DEBUG: Contraseña actualizada exitosamente en BD");
            
            return true;
        }
        return false;
    }
    
    /**
     * Método antiguo - Mantener por compatibilidad pero deprecado
     * @deprecated Usar cambiarContrasenaConCodigo en su lugar
     */
    @Deprecated
    public boolean cambiarContrasena(@NonNull String usuarioId, @NonNull String contrasenaActual, @NonNull String nuevaContrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Verificar que la contraseña actual sea correcta usando BCrypt
            if (passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
                // Encriptar la nueva contraseña
                usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
                usuarioRepositorio.save(usuario);
                return true;
            }
        }
        return false;
    }
    
    // ========== Métodos para Autenticación de Dos Factores (2FA) ==========
    
    /**
     * Habilitar 2FA para un usuario y generar clave secreta
     */
    public Map<String, String> habilitarTwoFactor(@NonNull String usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        Map<String, String> resultado = new HashMap<>();
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Generar nueva clave secreta
            String secret = twoFactorAuthServicio.generateSecretKey();
            
            // Guardar en el usuario
            usuario.setTwoFactorSecret(secret);
            usuario.setTwoFactorEnabled(false); // Se activará después de verificar el primer código
            usuarioRepositorio.save(usuario);
            
            // Generar URL del QR
            String qrCodeUrl = twoFactorAuthServicio.generateQRCodeUrl(secret, usuario.getCorreo());
            
            resultado.put("secret", secret);
            resultado.put("qrCodeUrl", qrCodeUrl);
            resultado.put("mensaje", "Escanea el código QR con Google Authenticator");
            
            System.out.println("2FA iniciado para: " + usuario.getCorreo());
            System.out.println("Secret: " + secret);
            System.out.println("QR URL: " + qrCodeUrl);
        }
        
        return resultado;
    }
    
    /**
     * Verificar código 2FA y activar si es correcto
     */
    public boolean verificarYActivarTwoFactor(@NonNull String usuarioId, @NonNull String code) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            if (usuario.getTwoFactorSecret() == null) {
                return false;
            }
            
            // Verificar el código
            boolean valido = twoFactorAuthServicio.verifyCode(usuario.getTwoFactorSecret(), code);
            
            if (valido) {
                // Activar 2FA
                usuario.setTwoFactorEnabled(true);
                usuarioRepositorio.save(usuario);
                System.out.println("2FA activado exitosamente para: " + usuario.getCorreo());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Deshabilitar 2FA para un usuario
     */
    public boolean deshabilitarTwoFactor(@NonNull String usuarioId, @NonNull String code) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            if (!usuario.isTwoFactorEnabled()) {
                return false;
            }
            
            // Verificar el código antes de deshabilitar
            boolean valido = twoFactorAuthServicio.verifyCode(usuario.getTwoFactorSecret(), code);
            
            if (valido) {
                usuario.setTwoFactorEnabled(false);
                usuario.setTwoFactorSecret(null);
                usuarioRepositorio.save(usuario);
                System.out.println("2FA deshabilitado para: " + usuario.getCorreo());
                return true;
            }
        }
        return false;
    }

/**
 * Verificar código 2FA durante el login
 */
public boolean verificarCodigoTwoFactor(@NonNull String usuarioId, @NonNull String code) {
    Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
    if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        if (!usuario.isTwoFactorEnabled() || usuario.getTwoFactorSecret() == null) {
            return false;
        }
        return twoFactorAuthServicio.verifyCode(usuario.getTwoFactorSecret(), code);
    }
    return false;
}

public Optional<String> obtenerTemaPreferido(@NonNull String usuarioId) {
    Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
    if (usuarioOpt.isPresent()) {
        return Optional.ofNullable(usuarioOpt.get().getTemaPreferido());
    }
    return Optional.empty();
}

public boolean actualizarTemaPreferido(@NonNull String usuarioId, @NonNull String temaPreferido) {
    Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
    if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        usuario.setTemaPreferido(temaPreferido);
        usuarioRepositorio.save(usuario);
        return true;
    }
    return false;
}
}