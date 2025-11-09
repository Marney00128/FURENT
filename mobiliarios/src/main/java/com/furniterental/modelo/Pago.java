package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "pagos")
public class Pago {
    
    @Id
    private String id;
    private String alquilerId;
    private String usuarioId;
    private Double monto;
    private String tipoPago; // PARCIAL o FINAL
    private String metodoPago; // TARJETA, TRANSFERENCIA, etc.
    private String estado; // PENDIENTE, COMPLETADO, FALLIDO
    private LocalDateTime fechaPago;
    private String numeroTransaccion;
    
    // Información de tarjeta (encriptada o tokenizada en producción)
    private String ultimos4Digitos;
    
    // Constructores
    public Pago() {
        this.fechaPago = LocalDateTime.now();
        this.estado = "COMPLETADO";
    }
    
    public Pago(String alquilerId, String usuarioId, Double monto, String tipoPago) {
        this();
        this.alquilerId = alquilerId;
        this.usuarioId = usuarioId;
        this.monto = monto;
        this.tipoPago = tipoPago;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAlquilerId() {
        return alquilerId;
    }
    
    public void setAlquilerId(String alquilerId) {
        this.alquilerId = alquilerId;
    }
    
    public String getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Double getMonto() {
        return monto;
    }
    
    public void setMonto(Double monto) {
        this.monto = monto;
    }
    
    public String getTipoPago() {
        return tipoPago;
    }
    
    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaPago() {
        return fechaPago;
    }
    
    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
    
    public String getNumeroTransaccion() {
        return numeroTransaccion;
    }
    
    public void setNumeroTransaccion(String numeroTransaccion) {
        this.numeroTransaccion = numeroTransaccion;
    }
    
    public String getUltimos4Digitos() {
        return ultimos4Digitos;
    }
    
    public void setUltimos4Digitos(String ultimos4Digitos) {
        this.ultimos4Digitos = ultimos4Digitos;
    }
}
