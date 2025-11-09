package com.furniterental.repositorio;

import com.furniterental.modelo.Direccion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionRepositorio extends MongoRepository<Direccion, String> {
    
    // Encontrar todas las direcciones de un usuario
    List<Direccion> findByUsuarioIdOrderByEsPrincipalDescFechaCreacionDesc(String usuarioId);
    
    // Encontrar la dirección principal de un usuario
    Optional<Direccion> findByUsuarioIdAndEsPrincipalTrue(String usuarioId);
    
    // Contar direcciones de un usuario
    long countByUsuarioId(String usuarioId);
    
    // Eliminar todas las direcciones de un usuario
    void deleteByUsuarioId(String usuarioId);
}
