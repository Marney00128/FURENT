package com.furniterental.modelo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
public class Producto {
    
    @Id
    private String id;
    
    private String nombreProducto;
    private String descripcionProducto;
    private Double precioProducto;
    private String categoriaProducto;
    private String imagenProducto;
    private Integer stock; // Cantidad disponible del producto
    private String estado; // ACTIVO, INACTIVO, AGOTADO
    
    // Constructores
    public Producto() {}
    
    public Producto(String nombreProducto, String descripcionProducto, Double precioProducto, 
                   String categoriaProducto, String imagenProducto, Integer stock) {
        this.nombreProducto = nombreProducto;
        this.descripcionProducto = descripcionProducto;
        this.precioProducto = precioProducto;
        this.categoriaProducto = categoriaProducto;
        this.imagenProducto = imagenProducto;
        this.stock = stock;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombreProducto() {
        return nombreProducto;
    }
    
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
    
    public String getDescripcionProducto() {
        return descripcionProducto;
    }
    
    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
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
    
    public String getImagenProducto() {
        return imagenProducto;
    }
    
    public void setImagenProducto(String imagenProducto) {
        this.imagenProducto = imagenProducto;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    // Método auxiliar para verificar si hay stock disponible
    public boolean tieneStock() {
        return stock != null && stock > 0;
    }
    
    // Método auxiliar para verificar si está activo
    public boolean estaActivo() {
        return "ACTIVO".equals(estado);
    }
}