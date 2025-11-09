package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador temporal para crear o actualizar el usuario administrador
 * Ejecutar UNA SOLA VEZ después de implementar BCrypt
 */
@RestController
@RequestMapping("/api/admin-setup")
public class CrearAdminControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint para crear o actualizar el usuario administrador con contraseña encriptada
     * 
     * Acceder mediante: POST http://localhost:8080/api/admin-setup/create
     * Body (JSON):
     * {
     *   "correo": "admin@furent.com",
     *   "contrasena": "Admin123!",
     *   "nombre": "Administrador"
     * }
     */
    @PostMapping("/create")
    public Map<String, Object> crearOActualizarAdmin(@RequestBody Map<String, String> datos) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String correo = datos.get("correo");
            String contrasena = datos.get("contrasena");
            String nombre = datos.get("nombre");
            
            if (correo == null || contrasena == null || nombre == null) {
                response.put("success", false);
                response.put("error", "Faltan datos: correo, contrasena o nombre");
                return response;
            }
            
            // Buscar si ya existe el usuario
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
            Usuario usuario;
            
            if (usuarioOpt.isPresent()) {
                // Actualizar usuario existente
                usuario = usuarioOpt.get();
                usuario.setContrasena(passwordEncoder.encode(contrasena));
                usuario.setNombre(nombre);
                usuario.setRol("ADMIN");
                response.put("accion", "actualizado");
            } else {
                // Crear nuevo usuario
                usuario = new Usuario();
                usuario.setCorreo(correo);
                usuario.setContrasena(passwordEncoder.encode(contrasena));
                usuario.setNombre(nombre);
                usuario.setRol("ADMIN");
                usuario.setTelefono("N/A");
                response.put("accion", "creado");
            }
            
            usuarioRepositorio.save(usuario);
            
            response.put("success", true);
            response.put("mensaje", "Usuario administrador " + response.get("accion") + " exitosamente");
            response.put("correo", correo);
            response.put("nombre", nombre);
            
            System.out.println("✅ Usuario administrador " + response.get("accion") + ": " + correo);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
    
    /**
     * Endpoint para verificar si un usuario tiene contraseña encriptada
     * 
     * GET http://localhost:8080/api/admin-setup/check?correo=admin@furent.com
     */
    @GetMapping("/check")
    public Map<String, Object> verificarUsuario(@RequestParam String correo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
            
            if (usuarioOpt.isEmpty()) {
                response.put("existe", false);
                response.put("mensaje", "Usuario no encontrado");
            } else {
                Usuario usuario = usuarioOpt.get();
                String contrasena = usuario.getContrasena();
                boolean esEncriptada = contrasena != null && contrasena.startsWith("$2");
                
                response.put("existe", true);
                response.put("correo", usuario.getCorreo());
                response.put("nombre", usuario.getNombre());
                response.put("rol", usuario.getRol());
                response.put("contrasenaEncriptada", esEncriptada);
                response.put("mensaje", esEncriptada ? 
                    "Contraseña ya está encriptada" : 
                    "Contraseña en texto plano - necesita migración");
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
