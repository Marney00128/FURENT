package com.furniterental.repositorio;

import com.furniterental.modelo.ActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepositorio extends MongoRepository<ActivityLog, String> {
    
    // Buscar logs por tipo de acción
    Page<ActivityLog> findByAction(String action, Pageable pageable);
    
    // Buscar logs por módulo
    Page<ActivityLog> findByModule(String module, Pageable pageable);
    
    // Buscar logs por usuario
    Page<ActivityLog> findByUserId(String userId, Pageable pageable);
    
    // Buscar logs por rango de fechas
    Page<ActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Buscar logs con filtros combinados usando Query Methods
    @Query("{ $and: [ " +
           "{ $or: [ { 'action': ?0 }, { 'action': { $exists: false } }, { ?0: null } ] }, " +
           "{ $or: [ { 'userName': { $regex: ?1, $options: 'i' } }, { 'userName': { $exists: false } }, { ?1: null } ] }, " +
           "{ $or: [ { 'timestamp': { $gte: ?2 } }, { 'timestamp': { $exists: false } }, { ?2: null } ] }, " +
           "{ $or: [ { 'timestamp': { $lte: ?3 } }, { 'timestamp': { $exists: false } }, { ?3: null } ] } " +
           "] }")
    Page<ActivityLog> findWithFilters(String action, String userName, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);
    
    // Contar logs por acción
    @Query(value = "{}", count = true)
    long countByAction(String action);
    
    // Obtener logs recientes
    List<ActivityLog> findTop10ByOrderByTimestampDesc();
    
    // Logs de un usuario específico
    List<ActivityLog> findByUserIdOrderByTimestampDesc(String userId);
    
    // Contar todos los logs por tipo de acción
    long countByAction();
    
    // Obtener logs ordenados por fecha
    Page<ActivityLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
