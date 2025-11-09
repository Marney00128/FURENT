package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "resenas")
public class Resena {
    
    @Id
    private String id;
    
    private String alquilerId;
    private String usuarioId;
    private String usuarioNombre;
    private String productoId;
    private String productoNombre;
    
    private int calificacion; // 1-5 estrellas
    private String comentario;
    private LocalDateTime fechaCreacion;
    
    // Estado de la reseña
    private String estado; // PENDIENTE, APROBADA, RECHAZADA
    
    // Respuesta del administrador (opcional)
    private String respuestaAdmin;
    private LocalDateTime fechaRespuesta;
    
    public Resena() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
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
    
    public String getUsuarioNombre() {
        return usuarioNombre;
    }
    
    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
    
    public String getProductoId() {
        return productoId;
    }
    
    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }
    
    public String getProductoNombre() {
        return productoNombre;
    }
    
    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }
    
    public int getCalificacion() {
        return calificacion;
    }
    
    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }
    
    public String getComentario() {
        return comentario;
    }
    
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getRespuestaAdmin() {
        return respuestaAdmin;
    }
    
    public void setRespuestaAdmin(String respuestaAdmin) {
        this.respuestaAdmin = respuestaAdmin;
    }
    
    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }
    
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}
