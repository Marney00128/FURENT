package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id; // Cambiado de Long a String para mejor compatibilidad con MongoDB
    
    private String nombre;
    private String correo;
    private String telefono;
    private String contrasena;
    private String rol; // "USER" o "ADMIN"
    
    private LocalDateTime ultimoInicioSesion;
    
    // Campos para verificación de cambio de contraseña
    private String codigoVerificacion;
    private LocalDateTime codigoExpiracion;
    
    // Campos para autenticación de dos factores (2FA)
    private boolean twoFactorEnabled;
    private String twoFactorSecret;
    private String temaPreferido;

    // Constructor vacío necesario para MongoDB
    public Usuario() {
    }
    
    // Constructor con parámetros para facilitar la creación
    public Usuario(String nombre, String correo, String telefono, String contrasena) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.contrasena = contrasena;
    }

    // Getters y Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public LocalDateTime getUltimoInicioSesion() { return ultimoInicioSesion; }
    public void setUltimoInicioSesion(LocalDateTime ultimoInicioSesion) { this.ultimoInicioSesion = ultimoInicioSesion; }

    public String getCodigoVerificacion() { return codigoVerificacion; }
    public void setCodigoVerificacion(String codigoVerificacion) { this.codigoVerificacion = codigoVerificacion; }

    public LocalDateTime getCodigoExpiracion() { return codigoExpiracion; }
    public void setCodigoExpiracion(LocalDateTime codigoExpiracion) { this.codigoExpiracion = codigoExpiracion; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }
    public String getTemaPreferido() { return temaPreferido; }
    public void setTemaPreferido(String temaPreferido) { this.temaPreferido = temaPreferido; }
}