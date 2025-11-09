package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar tarjetas de crédito/débito guardadas por el usuario
 * Los datos sensibles (número de tarjeta, CVV) se almacenan encriptados
 */
@Document(collection = "tarjetas_guardadas")
public class TarjetaGuardada {
    
    @Id
    private String id;
    
    private String usuarioId;
    
    // Datos encriptados
    private String numeroTarjetaEncriptado;  // Número completo encriptado
    private String cvvEncriptado;            // CVV encriptado
    
    // Datos no sensibles (pueden estar sin encriptar)
    private String nombreTitular;
    private String ultimos4Digitos;          // Solo los últimos 4 dígitos para mostrar
    private String tipoTarjeta;              // VISA, MASTERCARD, AMEX, etc.
    private String mesExpiracion;            // MM
    private String anioExpiracion;           // YYYY
    
    // Metadatos
    private boolean esPredeterminada;
    private String alias;                    // Ej: "Tarjeta Personal", "Tarjeta Empresa"
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public TarjetaGuardada() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.esPredeterminada = false;
    }
    
    public TarjetaGuardada(String usuarioId, String numeroTarjetaEncriptado, String cvvEncriptado,
                          String nombreTitular, String ultimos4Digitos, String tipoTarjeta,
                          String mesExpiracion, String anioExpiracion) {
        this();
        this.usuarioId = usuarioId;
        this.numeroTarjetaEncriptado = numeroTarjetaEncriptado;
        this.cvvEncriptado = cvvEncriptado;
        this.nombreTitular = nombreTitular;
        this.ultimos4Digitos = ultimos4Digitos;
        this.tipoTarjeta = tipoTarjeta;
        this.mesExpiracion = mesExpiracion;
        this.anioExpiracion = anioExpiracion;
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
    
    public String getNumeroTarjetaEncriptado() {
        return numeroTarjetaEncriptado;
    }
    
    public void setNumeroTarjetaEncriptado(String numeroTarjetaEncriptado) {
        this.numeroTarjetaEncriptado = numeroTarjetaEncriptado;
    }
    
    public String getCvvEncriptado() {
        return cvvEncriptado;
    }
    
    public void setCvvEncriptado(String cvvEncriptado) {
        this.cvvEncriptado = cvvEncriptado;
    }
    
    public String getNombreTitular() {
        return nombreTitular;
    }
    
    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }
    
    public String getUltimos4Digitos() {
        return ultimos4Digitos;
    }
    
    public void setUltimos4Digitos(String ultimos4Digitos) {
        this.ultimos4Digitos = ultimos4Digitos;
    }
    
    public String getTipoTarjeta() {
        return tipoTarjeta;
    }
    
    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }
    
    public String getMesExpiracion() {
        return mesExpiracion;
    }
    
    public void setMesExpiracion(String mesExpiracion) {
        this.mesExpiracion = mesExpiracion;
    }
    
    public String getAnioExpiracion() {
        return anioExpiracion;
    }
    
    public void setAnioExpiracion(String anioExpiracion) {
        this.anioExpiracion = anioExpiracion;
    }
    
    public boolean isEsPredeterminada() {
        return esPredeterminada;
    }
    
    public void setEsPredeterminada(boolean esPredeterminada) {
        this.esPredeterminada = esPredeterminada;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
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
    
    // Métodos de utilidad
    public String getFechaExpiracionFormateada() {
        return mesExpiracion + "/" + anioExpiracion;
    }
    
    public boolean estaVencida() {
        try {
            int anio = Integer.parseInt(anioExpiracion);
            int mes = Integer.parseInt(mesExpiracion);
            LocalDateTime ahora = LocalDateTime.now();
            
            if (anio < ahora.getYear()) {
                return true;
            } else if (anio == ahora.getYear() && mes < ahora.getMonthValue()) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return true; // Si hay error en el formato, consideramos vencida
        }
    }
    
    public String getTarjetaEnmascarada() {
        return "**** **** **** " + ultimos4Digitos;
    }
}
