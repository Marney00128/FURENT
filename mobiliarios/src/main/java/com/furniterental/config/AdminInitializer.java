package com.furniterental.config;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Inicializador que crea automáticamente el usuario administrador al iniciar la aplicación
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@furent.com";
        String adminPassword = "Furent2024!";
        String adminName = "Administrador FURENT";
        
        // Verificar si ya existe el administrador
        Optional<Usuario> adminExistente = usuarioRepositorio.findByCorreo(adminEmail);
        
        if (adminExistente.isEmpty()) {
            // Crear nuevo administrador
            Usuario admin = new Usuario();
            admin.setCorreo(adminEmail);
            admin.setNombre(adminName);
            admin.setContrasena(passwordEncoder.encode(adminPassword));
            admin.setRol("ADMIN");
            admin.setTelefono("N/A");
            
            usuarioRepositorio.save(admin);
            
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ USUARIO ADMINISTRADOR CREADO EXITOSAMENTE             ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  📧 Correo:     admin@furent.com                          ║");
            System.out.println("║  🔑 Contraseña: Furent2024!                               ║");
            System.out.println("║  👤 Nombre:     Administrador FURENT                      ║");
            System.out.println("║  🎯 Rol:        ADMIN                                     ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
        } else {
            // Actualizar contraseña del administrador existente si no está encriptada
            Usuario admin = adminExistente.get();
            String contrasenaActual = admin.getContrasena();
            
            if (contrasenaActual != null && !contrasenaActual.startsWith("$2")) {
                // La contraseña no está encriptada, actualizarla
                admin.setContrasena(passwordEncoder.encode(adminPassword));
                admin.setRol("ADMIN"); // Asegurar que tenga rol ADMIN
                usuarioRepositorio.save(admin);
                
                System.out.println("╔════════════════════════════════════════════════════════════╗");
                System.out.println("║  🔄 CONTRASEÑA DE ADMINISTRADOR ACTUALIZADA               ║");
                System.out.println("╠════════════════════════════════════════════════════════════╣");
                System.out.println("║  📧 Correo:     admin@furent.com                          ║");
                System.out.println("║  🔑 Contraseña: Furent2024!                               ║");
                System.out.println("║  ℹ️  La contraseña ha sido encriptada con BCrypt          ║");
                System.out.println("╚════════════════════════════════════════════════════════════╝");
            } else {
                System.out.println("✅ Usuario administrador ya existe con contraseña encriptada");
            }
        }
    }
}
