package com.furniterental.servicio;

import com.furniterental.modelo.Producto;
import com.furniterental.repositorio.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }
    
    public Optional<Producto> getProductoById(@NonNull String id) {
        return productoRepository.findById(id);
    }
    
    public Producto saveProducto(@NonNull Producto producto) {
        return productoRepository.save(producto);
    }
    
    public void deleteProducto(@NonNull String id) {
        productoRepository.deleteById(id);
    }
}