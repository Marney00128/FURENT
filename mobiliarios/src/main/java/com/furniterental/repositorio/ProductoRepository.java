package com.furniterental.repositorio;

import com.furniterental.modelo.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends MongoRepository<Producto, String> {
    // Aquí puedes agregar métodos personalizados de consulta si los necesitas

    
}

