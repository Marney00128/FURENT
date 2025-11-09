package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CONTROLADOR TEMPORAL PARA MIGRACIÓN DE CONTRASEÑAS
 * Este controlador debe ser eliminado después de ejecutar la migración
 */
@RestController
@RequestMapping("/api/migracion")
public class MigracionControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Endpoint temporal para encriptar todas las contraseñas existentes en la BD
     * EJECUTAR SOLO UNA VEZ y luego eliminar este controlador
     * 
     * Para ejecutar: POST http://localhost:8080/api/migracion/encriptar-contrasenas
     */
    @PostMapping("/encriptar-contrasenas")
    public String encriptarContrasenas() {
        try {
            List<Usuario> usuarios = usuarioRepositorio.findAll();
            int procesados = 0;
            int actualizados = 0;
            
            for (Usuario usuario : usuarios) {
                procesados++;
                String contrasenaActual = usuario.getContrasena();
                
                // Verificar si la contraseña ya está encriptada
                // Las contraseñas BCrypt empiezan con $2a$, $2b$ o $2y$
                if (contrasenaActual != null && !contrasenaActual.startsWith("$2")) {
                    System.out.println("Encriptando contraseña para: " + usuario.getCorreo());
                    System.out.println("Contraseña original: " + contrasenaActual);
                    
                    // Encriptar la contraseña
                    String contrasenaEncriptada = passwordEncoder.encode(contrasenaActual);
                    usuario.setContrasena(contrasenaEncriptada);
                    usuarioRepositorio.save(usuario);
                    
                    System.out.println("Contraseña encriptada: " + contrasenaEncriptada);
                    actualizados++;
                } else {
                    System.out.println("Contraseña ya encriptada para: " + usuario.getCorreo());
                }
            }
            
            String resultado = String.format(
                "Migración completada. Usuarios procesados: %d, Contraseñas actualizadas: %d",
                procesados, actualizados
            );
            
            System.out.println(resultado);
            return resultado;
            
        } catch (Exception e) {
            String error = "Error durante la migración: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return error;
        }
    }
}
