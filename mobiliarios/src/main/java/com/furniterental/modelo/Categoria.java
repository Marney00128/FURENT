package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "categorias")
public class Categoria {
    
    @Id
    private String id;
    
    private String nombre;
    private String descripcion;
    private String icono; // Bootstrap icon class (e.g., "bi-chair")
    private Integer cantidadProductos; // Cantidad de productos en esta categoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public Categoria() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.cantidadProductos = 0;
    }
    
    public Categoria(String nombre, String descripcion, String icono) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getIcono() {
        return icono;
    }
    
    public void setIcono(String icono) {
        this.icono = icono;
    }
    
    public Integer getCantidadProductos() {
        return cantidadProductos;
    }
    
    public void setCantidadProductos(Integer cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
