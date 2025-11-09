package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.servicio.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Permitir solicitudes desde cualquier origen
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @GetMapping
    public List<Usuario> obtenerTodos() {
        return usuarioServicio.obtenerTodosLosUsuarios();
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        // Validar que el correo no sea nulo
        String correo = usuario.getCorreo();
        if (correo == null || correo.isEmpty()) {
            return ResponseEntity.badRequest().body("El correo es obligatorio");
        }
        
        // Verificar si el correo ya está registrado
        if (usuarioServicio.buscarPorCorreo(correo).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }
        Usuario usuarioRegistrado = usuarioServicio.registrarUsuario(usuario);
        return ResponseEntity.ok(usuarioRegistrado);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario datosLogin) {
        // Validar que correo y contraseña no sean nulos
        String correo = datosLogin.getCorreo();
        String contrasena = datosLogin.getContrasena();
        
        if (correo == null || correo.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            return ResponseEntity.badRequest().body("Correo y contraseña son obligatorios");
        }
        
        Optional<Usuario> usuarioOpt = usuarioServicio.login(correo, contrasena);
        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(usuarioOpt.get());
        } else {
            return ResponseEntity.badRequest().body("Credenciales incorrectas");
        }
    }

    @GetMapping("/{id}/tema")
    public ResponseEntity<?> obtenerTema(@PathVariable String id) {
        Optional<String> tema = usuarioServicio.obtenerTemaPreferido(id);
        return tema.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/tema")
    public ResponseEntity<?> actualizarTema(@PathVariable String id, @RequestBody Map<String, String> body) {
        String tema = body != null ? body.get("tema") : null;
        if (tema == null || tema.isEmpty()) {
            return ResponseEntity.badRequest().body("El tema es obligatorio");
        }
        boolean ok = usuarioServicio.actualizarTemaPreferido(id, tema);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}