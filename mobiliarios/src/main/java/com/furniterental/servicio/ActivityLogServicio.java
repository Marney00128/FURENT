package com.furniterental.servicio;

import com.furniterental.modelo.ActivityLog;
import com.furniterental.repositorio.ActivityLogRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityLogServicio {
    
    @Autowired
    private ActivityLogRepositorio activityLogRepositorio;
    
    // Guardar un log
    @NonNull
    public ActivityLog save(@NonNull ActivityLog log) {
        return activityLogRepositorio.save(log);
    }
    
    // Obtener todos los logs con paginación
    public Page<ActivityLog> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return activityLogRepositorio.findAllByOrderByTimestampDesc(pageable);
    }
    
    // Buscar logs con filtros
    public Page<ActivityLog> findWithFilters(String action, String userName, 
                                              LocalDateTime dateFrom, LocalDateTime dateTo, 
                                              int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return activityLogRepositorio.findWithFilters(action, userName, dateFrom, dateTo, pageable);
    }
    
    // Obtener logs recientes
    public List<ActivityLog> getRecentLogs() {
        return activityLogRepositorio.findTop10ByOrderByTimestampDesc();
    }
    
    // Obtener logs de un usuario
    public List<ActivityLog> getUserLogs(String userId) {
        return activityLogRepositorio.findByUserIdOrderByTimestampDesc(userId);
    }
    
    // Obtener estadísticas
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        List<ActivityLog> allLogs = activityLogRepositorio.findAll();
        
        long createCount = allLogs.stream().filter(log -> "CREATE".equals(log.getAction())).count();
        long updateCount = allLogs.stream().filter(log -> "UPDATE".equals(log.getAction())).count();
        long deleteCount = allLogs.stream().filter(log -> "DELETE".equals(log.getAction())).count();
        long rentalCount = allLogs.stream().filter(log -> "RENTAL".equals(log.getAction())).count();
        long loginCount = allLogs.stream().filter(log -> "LOGIN".equals(log.getAction())).count();
        long statusChangeCount = allLogs.stream().filter(log -> "STATUS_CHANGE".equals(log.getAction())).count();
        long rentalCancelCount = allLogs.stream().filter(log -> "RENTAL_CANCEL".equals(log.getAction())).count();
        long paymentCount = allLogs.stream().filter(log -> "PAYMENT".equals(log.getAction())).count();
        
        stats.put("CREATE", createCount);
        stats.put("UPDATE", updateCount);
        stats.put("DELETE", deleteCount);
        stats.put("RENTAL", rentalCount);
        stats.put("LOGIN", loginCount);
        stats.put("STATUS_CHANGE", statusChangeCount);
        stats.put("RENTAL_CANCEL", rentalCancelCount);
        stats.put("PAYMENT", paymentCount);
        stats.put("TOTAL", (long) allLogs.size());
        
        return stats;
    }
    
    // Métodos helper para crear logs
    public void logCreate(String userId, String userName, String userEmail, String entityName, String module) {
        ActivityLog log = ActivityLog.create(userId, userName, userEmail, entityName, module);
        save(log);
    }
    
    public void logUpdate(String userId, String userName, String userEmail, String entityName, String module, String detail) {
        ActivityLog log = ActivityLog.update(userId, userName, userEmail, entityName, module, detail);
        save(log);
    }
    
    public void logDelete(String userId, String userName, String userEmail, String entityName, String module) {
        ActivityLog log = ActivityLog.delete(userId, userName, userEmail, entityName, module);
        save(log);
    }
    
    public void logLogin(String userId, String userName, String userEmail, HttpServletRequest request) {
        ActivityLog log = ActivityLog.login(userId, userName, userEmail);
        log.setIpAddress(getClientIP(request));
        save(log);
    }
    
    public void logRental(String userId, String userName, String userEmail, int productCount, double total, HttpServletRequest request) {
        ActivityLog log = ActivityLog.rental(userId, userName, userEmail, productCount, total);
        log.setIpAddress(getClientIP(request));
        save(log);
    }
    
    /**
     * Registra cuando un administrador cambia el estado de un alquiler
     */
    public void logStatusChange(String adminId, String adminName, String adminEmail, 
                                 String alquilerId, String estadoAnterior, String estadoNuevo,
                                 String usuarioAfectado, HttpServletRequest request) {
        ActivityLog log = new ActivityLog();
        log.setUserId(adminId);
        log.setUserName(adminName);
        log.setUserEmail(adminEmail);
        log.setAction("STATUS_CHANGE");
        log.setDescription(String.format("Cambió estado del alquiler #%s de '%s' a '%s'", 
                                         alquilerId, estadoAnterior, estadoNuevo));
        log.setModule("ALQUILERES");
        log.setEntityId(alquilerId);
        log.setDetails(String.format("Usuario afectado: %s | Estado anterior: %s | Estado nuevo: %s", 
                                     usuarioAfectado, estadoAnterior, estadoNuevo));
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(getClientIP(request));
        save(log);
    }
    
    /**
     * Registra cuando un usuario cancela su propio alquiler
     */
    public void logRentalCancellation(String userId, String userName, String userEmail,
                                      String alquilerId, HttpServletRequest request) {
        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setUserEmail(userEmail);
        log.setAction("RENTAL_CANCEL");
        log.setDescription(String.format("Canceló su alquiler #%s", alquilerId));
        log.setModule("ALQUILERES");
        log.setEntityId(alquilerId);
        log.setDetails("Estado: PENDIENTE → CANCELADO | Acción realizada por el usuario");
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(getClientIP(request));
        save(log);
    }
    
    /**
     * Registra cuando un usuario realiza un pago (parcial o final)
     */
    public void logPayment(String userId, String userName, String userEmail,
                          String tipoPago, double monto, String alquilerId, 
                          String numeroTransaccion, HttpServletRequest request) {
        ActivityLog log = ActivityLog.payment(userId, userName, userEmail, tipoPago, monto, alquilerId);
        log.setEntityId(alquilerId);
        log.setDetails(String.format("Tipo: %s | Monto: $%.2f | Transacción: %s", 
                                     tipoPago, monto, numeroTransaccion));
        log.setIpAddress(getClientIP(request));
        save(log);
    }
    
    // Obtener IP del cliente
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
