package com.furniterental.config;

import com.furniterental.modelo.Categoria;
import com.furniterental.repositorio.CategoriaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Override
    public void run(String... args) throws Exception {
        inicializarCategorias();
    }

    private void inicializarCategorias() {
        // Solo inicializar si no hay categorías en la base de datos
        if (categoriaRepositorio.count() == 0) {
            List<CategoriaInicial> categoriasIniciales = Arrays.asList(
                new CategoriaInicial("Sillas", "Sillas y asientos para todo tipo de eventos", "bi-chair"),
                new CategoriaInicial("Mesas", "Mesas de diferentes tamaños y estilos", "bi-table"),
                new CategoriaInicial("Sillones", "Sillones y sofás cómodos para eventos", "bi-couch"),
                new CategoriaInicial("Decoración", "Elementos decorativos para ambientar eventos", "bi-palette"),
                new CategoriaInicial("Iluminación", "Iluminación profesional para eventos", "bi-lightbulb"),
                new CategoriaInicial("Manteles", "Manteles y textiles para mesas", "bi-file-text"),
                new CategoriaInicial("Otros", "Otros artículos y mobiliario para eventos", "bi-box")
            );

            for (CategoriaInicial catInicial : categoriasIniciales) {
                if (!categoriaRepositorio.existsByNombre(catInicial.nombre)) {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(catInicial.nombre);
                    categoria.setDescripcion(catInicial.descripcion);
                    categoria.setIcono(catInicial.icono);
                    categoria.setCantidadProductos(0);
                    categoria.setFechaCreacion(LocalDateTime.now());
                    categoria.setFechaActualizacion(LocalDateTime.now());
                    
                    categoriaRepositorio.save(categoria);
                    System.out.println("Categoría creada: " + catInicial.nombre);
                }
            }
            
            System.out.println("Inicialización de categorías completada.");
        } else {
            System.out.println("Las categorías ya existen en la base de datos.");
        }
    }

    // Clase interna para facilitar la inicialización
    private static class CategoriaInicial {
        String nombre;
        String descripcion;
        String icono;

        CategoriaInicial(String nombre, String descripcion, String icono) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.icono = icono;
        }
    }
}
