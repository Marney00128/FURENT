package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador temporal para migrar contraseñas existentes a BCrypt.
 * IMPORTANTE: Eliminar este controlador después de ejecutar la migración.
 */
@RestController
@RequestMapping("/api/migration")
public class MigracionPasswordControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint para encriptar todas las contraseñas existentes en la base de datos.
     * Solo debe ejecutarse UNA VEZ después de implementar BCrypt.
     * 
     * Acceder mediante: POST http://localhost:8080/api/migration/encrypt-passwords
     */
    @PostMapping("/encrypt-passwords")
    public Map<String, Object> encriptarPasswordsExistentes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Usuario> usuarios = usuarioRepositorio.findAll();
            int encriptados = 0;
            int yaEncriptados = 0;
            
            for (Usuario usuario : usuarios) {
                String contrasenaActual = usuario.getContrasena();
                
                // Verificar si la contraseña ya está encriptada con BCrypt
                // Las contraseñas BCrypt comienzan con "$2a$" o "$2b$" o "$2y$"
                if (contrasenaActual != null && !contrasenaActual.startsWith("$2")) {
                    // Encriptar la contraseña
                    String contrasenaEncriptada = passwordEncoder.encode(contrasenaActual);
                    usuario.setContrasena(contrasenaEncriptada);
                    usuarioRepositorio.save(usuario);
                    encriptados++;
                    
                    System.out.println("Contraseña encriptada para usuario: " + usuario.getCorreo());
                } else {
                    yaEncriptados++;
                    System.out.println("Contraseña ya encriptada para usuario: " + usuario.getCorreo());
                }
            }
            
            response.put("success", true);
            response.put("totalUsuarios", usuarios.size());
            response.put("encriptados", encriptados);
            response.put("yaEncriptados", yaEncriptados);
            response.put("mensaje", "Migración completada exitosamente");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
