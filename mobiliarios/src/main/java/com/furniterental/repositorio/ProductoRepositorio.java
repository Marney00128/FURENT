package com.furniterental.repositorio;

import com.furniterental.modelo.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepositorio extends MongoRepository<Producto, String> {
    List<Producto> findByCategoriaProducto(String categoriaProducto);
    
    long countByCategoriaProducto(String categoriaProducto);
}