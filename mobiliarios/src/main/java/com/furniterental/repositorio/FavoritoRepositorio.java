package com.furniterental.repositorio;

import com.furniterental.modelo.Favorito;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepositorio extends MongoRepository<Favorito, String> {
    
    // Buscar todos los favoritos de un usuario
    List<Favorito> findByUsuarioIdOrderByFechaAgregadoDesc(String usuarioId);
    
    // Buscar un favorito específico por usuario y producto
    Optional<Favorito> findByUsuarioIdAndProductoId(String usuarioId, String productoId);
    
    // Verificar si existe un favorito
    boolean existsByUsuarioIdAndProductoId(String usuarioId, String productoId);
    
    // Eliminar un favorito específico
    void deleteByUsuarioIdAndProductoId(String usuarioId, String productoId);
    
    // Contar favoritos de un usuario
    long countByUsuarioId(String usuarioId);
}
