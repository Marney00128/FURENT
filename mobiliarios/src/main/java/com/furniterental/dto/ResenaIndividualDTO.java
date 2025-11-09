package com.furniterental.dto;

import java.util.List;

public class ResenaIndividualDTO {
    private String alquilerId;
    private List<ResenaData> resenas;
    
    public static class ResenaData {
        private String productoId;
        private Integer calificacion;
        private String comentario;
        
        public String getProductoId() {
            return productoId;
        }
        
        public void setProductoId(String productoId) {
            this.productoId = productoId;
        }
        
        public Integer getCalificacion() {
            return calificacion;
        }
        
        public void setCalificacion(Integer calificacion) {
            this.calificacion = calificacion;
        }
        
        public String getComentario() {
            return comentario;
        }
        
        public void setComentario(String comentario) {
            this.comentario = comentario;
        }
    }
    
    public String getAlquilerId() {
        return alquilerId;
    }
    
    public void setAlquilerId(String alquilerId) {
        this.alquilerId = alquilerId;
    }
    
    public List<ResenaData> getResenas() {
        return resenas;
    }
    
    public void setResenas(List<ResenaData> resenas) {
        this.resenas = resenas;
    }
}
