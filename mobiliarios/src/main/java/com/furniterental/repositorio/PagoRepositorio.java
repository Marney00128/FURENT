package com.furniterental.repositorio;

import com.furniterental.modelo.Pago;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepositorio extends MongoRepository<Pago, String> {
    
    // Buscar pagos por alquiler
    List<Pago> findByAlquilerId(String alquilerId);
    
    // Buscar pagos por usuario
    List<Pago> findByUsuarioId(String usuarioId);
    
    // Buscar pagos por tipo
    List<Pago> findByTipoPago(String tipoPago);
    
    // Buscar pagos por estado
    List<Pago> findByEstado(String estado);
}
