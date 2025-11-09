package com.furniterental.repositorio;

import com.furniterental.modelo.TarjetaGuardada;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarjetaGuardadaRepositorio extends MongoRepository<TarjetaGuardada, String> {
    
    /**
     * Encuentra todas las tarjetas de un usuario ordenadas por fecha de creación
     */
    List<TarjetaGuardada> findByUsuarioIdOrderByFechaCreacionDesc(String usuarioId);
    
    /**
     * Encuentra la tarjeta predeterminada de un usuario
     */
    Optional<TarjetaGuardada> findByUsuarioIdAndEsPredeterminadaTrue(String usuarioId);
    
    /**
     * Cuenta cuántas tarjetas tiene un usuario
     */
    long countByUsuarioId(String usuarioId);
    
    /**
     * Verifica si un usuario tiene una tarjeta específica
     */
    boolean existsByIdAndUsuarioId(String id, String usuarioId);
    
    /**
     * Verifica si ya existe una tarjeta con los mismos últimos 4 dígitos para un usuario
     */
    boolean existsByUsuarioIdAndUltimos4Digitos(String usuarioId, String ultimos4Digitos);
}
