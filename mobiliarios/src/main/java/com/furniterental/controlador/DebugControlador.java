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
 * CONTROLADOR TEMPORAL PARA DEBUG
 * Eliminar después de resolver el problema
 */
@RestController
@RequestMapping("/api/debug")
public class DebugControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Verificar qué contraseña tiene un usuario en la BD
     */
    @GetMapping("/verificar-usuario/{correo}")
    public Map<String, Object> verificarUsuario(@PathVariable String correo) {
        Map<String, Object> resultado = new HashMap<>();
        
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            resultado.put("encontrado", true);
            resultado.put("correo", usuario.getCorreo());
            resultado.put("nombre", usuario.getNombre());
            resultado.put("contrasenaHash", usuario.getContrasena());
            resultado.put("esHashBCrypt", usuario.getContrasena().startsWith("$2"));
            resultado.put("longitudHash", usuario.getContrasena().length());
        } else {
            resultado.put("encontrado", false);
            resultado.put("mensaje", "Usuario no encontrado");
        }
        
        return resultado;
    }
    
    /**
     * Probar si una contraseña coincide con el hash
     */
    @PostMapping("/probar-contrasena")
    public Map<String, Object> probarContrasena(@RequestBody Map<String, String> datos) {
        Map<String, Object> resultado = new HashMap<>();
        
        String correo = datos.get("correo");
        String contrasena = datos.get("contrasena");
        
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean coincide = passwordEncoder.matches(contrasena, usuario.getContrasena());
            
            resultado.put("encontrado", true);
            resultado.put("correo", usuario.getCorreo());
            resultado.put("contrasenaIngresada", contrasena);
            resultado.put("hashEnBD", usuario.getContrasena());
            resultado.put("coincide", coincide);
        } else {
            resultado.put("encontrado", false);
            resultado.put("mensaje", "Usuario no encontrado");
        }
        
        return resultado;
    }
    
    /**
     * Forzar actualización de contraseña (SOLO PARA DEBUG)
     */
    @PostMapping("/forzar-contrasena")
    public Map<String, Object> forzarContrasena(@RequestBody Map<String, String> datos) {
        Map<String, Object> resultado = new HashMap<>();
        
        String correo = datos.get("correo");
        String nuevaContrasena = datos.get("contrasena");
        
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Encriptar y guardar
            String hashAnterior = usuario.getContrasena();
            String hashNuevo = passwordEncoder.encode(nuevaContrasena);
            
            usuario.setContrasena(hashNuevo);
            usuarioRepositorio.save(usuario);
            
            resultado.put("exito", true);
            resultado.put("correo", usuario.getCorreo());
            resultado.put("hashAnterior", hashAnterior);
            resultado.put("hashNuevo", hashNuevo);
            resultado.put("mensaje", "Contraseña actualizada exitosamente");
        } else {
            resultado.put("exito", false);
            resultado.put("mensaje", "Usuario no encontrado");
        }
        
        return resultado;
    }
}
