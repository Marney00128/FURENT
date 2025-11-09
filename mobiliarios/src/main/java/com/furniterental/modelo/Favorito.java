package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "favoritos")
public class Favorito {
    
    @Id
    private String id;
    
    private String usuarioId;
    private String productoId;
    private LocalDateTime fechaAgregado;
    
    // Datos desnormalizados del producto para consultas rápidas
    private String nombreProducto;
    private String imagenProducto;
    private Double precioProducto;
    private String categoriaProducto;
    
    // Constructores
    public Favorito() {
        this.fechaAgregado = LocalDateTime.now();
    }
    
    public Favorito(String usuarioId, String productoId) {
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.fechaAgregado = LocalDateTime.now();
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getProductoId() {
        return productoId;
    }
    
    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }
    
    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }
    
    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
    
    public String getNombreProducto() {
        return nombreProducto;
    }
    
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
    
    public String getImagenProducto() {
        return imagenProducto;
    }
    
    public void setImagenProducto(String imagenProducto) {
        this.imagenProducto = imagenProducto;
    }
    
    public Double getPrecioProducto() {
        return precioProducto;
    }
    
    public void setPrecioProducto(Double precioProducto) {
        this.precioProducto = precioProducto;
    }
    
    public String getCategoriaProducto() {
        return categoriaProducto;
    }
    
    public void setCategoriaProducto(String categoriaProducto) {
        this.categoriaProducto = categoriaProducto;
    }
}
