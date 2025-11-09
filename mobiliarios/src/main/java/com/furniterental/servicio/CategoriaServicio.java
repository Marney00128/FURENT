package com.furniterental.servicio;

import com.furniterental.modelo.Categoria;
import com.furniterental.repositorio.CategoriaRepositorio;
import com.furniterental.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServicio {
    
    @Autowired
    private CategoriaRepositorio categoriaRepositorio;
    
    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    public List<Categoria> obtenerTodasCategorias() {
        List<Categoria> categorias = categoriaRepositorio.findAllByOrderByNombreAsc();
        // Actualizar la cantidad de productos por categoría
        for (Categoria categoria : categorias) {
            long cantidad = productoRepositorio.countByCategoriaProducto(categoria.getNombre());
            categoria.setCantidadProductos((int) cantidad);
        }
        return categorias;
    }
    
    public Optional<Categoria> obtenerCategoriaPorId(@NonNull String id) {
        return categoriaRepositorio.findById(id);
    }
    
    public Optional<Categoria> obtenerCategoriaPorNombre(@NonNull String nombre) {
        return categoriaRepositorio.findByNombre(nombre);
    }
    
    public Categoria crearCategoria(Categoria categoria) {
        categoria.setFechaCreacion(LocalDateTime.now());
        categoria.setFechaActualizacion(LocalDateTime.now());
        categoria.setCantidadProductos(0);
        return categoriaRepositorio.save(categoria);
    }
    
    public Categoria actualizarCategoria(@NonNull String id, Categoria categoriaActualizada) {
        Optional<Categoria> categoriaOpt = categoriaRepositorio.findById(id);
        if (categoriaOpt.isPresent()) {
            Categoria categoria = categoriaOpt.get();
            categoria.setNombre(categoriaActualizada.getNombre());
            categoria.setDescripcion(categoriaActualizada.getDescripcion());
            categoria.setIcono(categoriaActualizada.getIcono());
            categoria.setFechaActualizacion(LocalDateTime.now());
            return categoriaRepositorio.save(categoria);
        }
        return null;
    }
    
    public boolean eliminarCategoria(@NonNull String id) {
        Optional<Categoria> categoria = categoriaRepositorio.findById(id);
        if (categoria.isPresent()) {
            // Verificar si hay productos en esta categoría
            long cantidadProductos = productoRepositorio.countByCategoriaProducto(categoria.get().getNombre());
            if (cantidadProductos > 0) {
                return false; // No se puede eliminar si hay productos
            }
            categoriaRepositorio.deleteById(id);
            return true;
        }
        return false;
    }
    
    public boolean existeCategoria(@NonNull String nombre) {
        return categoriaRepositorio.existsByNombre(nombre);
    }
    
    public long contarCategorias() {
        return categoriaRepositorio.count();
    }
}
