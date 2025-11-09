package com.furniterental.servicio;

import com.furniterental.modelo.Administrador;
import com.furniterental.repositorio.AdministradorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdministradorServicio {

    @Autowired
    private AdministradorRepositorio administradorRepositorio;

    public Optional<Administrador> autenticar(String nombreUsuario, String contrasena) {
        return administradorRepositorio.findByNombreUsuarioAndContrasena(nombreUsuario, contrasena);
    }
}