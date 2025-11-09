package com.furniterental.repositorio;

import com.furniterental.modelo.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByCorreoAndContrasena(String correo, String contrasena);
    long countByRol(String rol);
}