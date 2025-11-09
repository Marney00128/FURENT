package com.furniterental.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;

@Document(collection = "activity_logs")
public class ActivityLog {
    
    @Id
    private String id;
    
    private LocalDateTime timestamp;
    private String userId;
    private String userName;
    private String userEmail;
    private String action; // CREATE, UPDATE, DELETE, LOGIN, RENTAL, PAYMENT
    private String description;
    private String module; // PRODUCTOS, CATEGORIAS, USUARIOS, ALQUILERES, AUTENTICACION, PAGOS
    private String entityId; // ID del objeto afectado
    private String ipAddress;
    private String details; // Detalles adicionales en formato JSON
    
    // Constructores
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ActivityLog(String userId, String userName, String userEmail, String action, 
                       String description, String module) {
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.action = action;
        this.description = description;
        this.module = module;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
    }
    
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    // Métodos estáticos para crear logs fácilmente
    @NonNull
    public static ActivityLog create(String userId, String userName, String userEmail, 
                                     String entityName, String module) {
        return new ActivityLog(userId, userName, userEmail, "CREATE", 
                             "Creó el " + module.toLowerCase() + " \"" + entityName + "\"", module);
    }
    
    @NonNull
    public static ActivityLog update(String userId, String userName, String userEmail, 
                                     String entityName, String module, String detail) {
        return new ActivityLog(userId, userName, userEmail, "UPDATE", 
                             "Actualizó el " + module.toLowerCase() + " \"" + entityName + "\" - " + detail, module);
    }
    
    @NonNull
    public static ActivityLog delete(String userId, String userName, String userEmail, 
                                     String entityName, String module) {
        return new ActivityLog(userId, userName, userEmail, "DELETE", 
                             "Eliminó el " + module.toLowerCase() + " \"" + entityName + "\"", module);
    }
    
    @NonNull
    public static ActivityLog login(String userId, String userName, String userEmail) {
        return new ActivityLog(userId, userName, userEmail, "LOGIN", 
                             "Inició sesión en la plataforma", "AUTENTICACION");
    }
    
    @NonNull
    public static ActivityLog rental(String userId, String userName, String userEmail, 
                                     int productCount, double total) {
        return new ActivityLog(userId, userName, userEmail, "RENTAL", 
                             "Realizó un alquiler de " + productCount + " productos por $" + 
                             String.format("%.2f", total), "ALQUILERES");
    }
    
    @NonNull
    public static ActivityLog payment(String userId, String userName, String userEmail, 
                                      String tipoPago, double monto, String alquilerId) {
        return new ActivityLog(userId, userName, userEmail, "PAYMENT", 
                             "Realizó un pago " + tipoPago.toLowerCase() + " de $" + 
                             String.format("%.2f", monto) + " para el alquiler #" + alquilerId, "PAGOS");
    }
}
