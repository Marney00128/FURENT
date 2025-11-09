package com.furniterental.repositorio;

import com.furniterental.modelo.Resena;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepositorio extends MongoRepository<Resena, String> {
    
    // Buscar reseñas por alquiler
    List<Resena> findByAlquilerId(String alquilerId);
    
    // Buscar reseñas por usuario
    List<Resena> findByUsuarioId(String usuarioId);
    
    // Buscar reseñas por producto
    List<Resena> findByProductoId(String productoId);
    
    // Buscar reseñas por estado
    List<Resena> findByEstado(String estado);
    
    // Verificar si existe reseña para un alquiler
    boolean existsByAlquilerId(String alquilerId);
    
    // Verificar si existe reseña para un alquiler y producto específico
    boolean existsByAlquilerIdAndProductoId(String alquilerId, String productoId);
    
    // Obtener reseñas por alquiler (para saber qué productos ya fueron reseñados)
    List<Resena> findByAlquilerIdAndProductoId(String alquilerId, String productoId);
    
    // Buscar reseñas aprobadas por producto
    List<Resena> findByProductoIdAndEstado(String productoId, String estado);
}
