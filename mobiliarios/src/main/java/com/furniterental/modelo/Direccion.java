package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "direcciones")
public class Direccion {
    
    @Id
    private String id;
    
    private String usuarioId;
    private String nombreDireccion; // Ej: "Casa", "Oficina", "Casa de playa"
    private String direccionCompleta;
    private String ciudad;
    private String departamento;
    private String codigoPostal;
    private String telefono;
    private String referencia; // Punto de referencia adicional
    private boolean esPrincipal; // Si es la dirección principal
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public Direccion() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.esPrincipal = false;
    }
    
    public Direccion(String usuarioId, String nombreDireccion, String direccionCompleta, 
                     String ciudad, String departamento, String telefono) {
        this();
        this.usuarioId = usuarioId;
        this.nombreDireccion = nombreDireccion;
        this.direccionCompleta = direccionCompleta;
        this.ciudad = ciudad;
        this.departamento = departamento;
        this.telefono = telefono;
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
    
    public String getNombreDireccion() {
        return nombreDireccion;
    }
    
    public void setNombreDireccion(String nombreDireccion) {
        this.nombreDireccion = nombreDireccion;
    }
    
    public String getDireccionCompleta() {
        return direccionCompleta;
    }
    
    public void setDireccionCompleta(String direccionCompleta) {
        this.direccionCompleta = direccionCompleta;
    }
    
    public String getCiudad() {
        return ciudad;
    }
    
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    
    public String getDepartamento() {
        return departamento;
    }
    
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
    
    public String getCodigoPostal() {
        return codigoPostal;
    }
    
    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getReferencia() {
        return referencia;
    }
    
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
    public boolean isEsPrincipal() {
        return esPrincipal;
    }
    
    public void setEsPrincipal(boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
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
