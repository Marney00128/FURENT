package com.furniterental.repositorio;

import com.furniterental.modelo.Administrador;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdministradorRepositorio extends MongoRepository<Administrador, String> {
    Optional<Administrador> findByNombreUsuarioAndContrasena(String nombreUsuario, String contrasena);
}