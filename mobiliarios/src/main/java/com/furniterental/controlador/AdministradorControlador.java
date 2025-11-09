package com.furniterental.controlador;

import com.furniterental.modelo.Administrador;
import com.furniterental.servicio.AdministradorServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/administrador")
@CrossOrigin(origins = "*")
public class AdministradorControlador {

    @Autowired
    private AdministradorServicio administradorServicio;

    @PostMapping("/login")
    public Optional<Administrador> login(@RequestBody Administrador administrador) {
        String nombreUsuario = administrador.getNombreUsuario();
        
        // Verificar que el nombre de usuario sea válido (@gmail.com o admin@furent.com)
        if (nombreUsuario != null && !nombreUsuario.endsWith("@gmail.com") && !nombreUsuario.equals("admin@furent.com")) {
            return Optional.empty();
        }
        
        return administradorServicio.autenticar(nombreUsuario, administrador.getContrasena());
    }
}