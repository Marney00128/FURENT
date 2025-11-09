package com.furniterental.modelo;

public class ItemCarrito {
    private String productoId;
    private String nombreProducto;
    private String imagenProducto;
    private Double precioProducto;
    private Integer cantidad;
    private Integer diasAlquiler;
    private Double subtotal;

    public ItemCarrito() {
    }

    public ItemCarrito(String productoId, String nombreProducto, String imagenProducto, 
                      Double precioProducto, Integer cantidad, Integer diasAlquiler) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.imagenProducto = imagenProducto;
        this.precioProducto = precioProducto;
        this.cantidad = cantidad;
        this.diasAlquiler = diasAlquiler;
        calcularSubtotal();
    }

    // Getters y Setters
    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
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
        calcularSubtotal();
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public Integer getDiasAlquiler() {
        return diasAlquiler;
    }

    public void setDiasAlquiler(Integer diasAlquiler) {
        this.diasAlquiler = diasAlquiler;
        calcularSubtotal();
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    private void calcularSubtotal() {
        if (precioProducto != null && cantidad != null && diasAlquiler != null) {
            this.subtotal = precioProducto * cantidad * diasAlquiler;
        }
    }
}
