package com.furniterental.repositorio;

import com.furniterental.modelo.Categoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CategoriaRepositorio extends MongoRepository<Categoria, String> {
    
    Optional<Categoria> findByNombre(String nombre);
    
    boolean existsByNombre(String nombre);
    
    List<Categoria> findAllByOrderByNombreAsc();
}
