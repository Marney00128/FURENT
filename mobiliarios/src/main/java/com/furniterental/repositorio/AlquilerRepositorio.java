package com.furniterental.repositorio;

import com.furniterental.modelo.Alquiler;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlquilerRepositorio extends MongoRepository<Alquiler, String> {
    List<Alquiler> findByUsuarioIdOrderByFechaAlquilerDesc(String usuarioId);
    List<Alquiler> findByEstado(String estado);
    List<Alquiler> findAllByOrderByFechaAlquilerDesc();
    long countByUsuarioId(String usuarioId);
}
