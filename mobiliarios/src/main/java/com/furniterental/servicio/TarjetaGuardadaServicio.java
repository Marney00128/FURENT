package com.furniterental.servicio;

import com.furniterental.modelo.TarjetaGuardada;
import com.furniterental.repositorio.TarjetaGuardadaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para gestionar tarjetas guardadas de los usuarios
 */
@Service
public class TarjetaGuardadaServicio {
    
    @Autowired
    private TarjetaGuardadaRepositorio tarjetaRepositorio;
    
    @Autowired
    private EncriptacionServicio encriptacionServicio;
    
    // Límite máximo de tarjetas por usuario
    private static final int MAX_TARJETAS_POR_USUARIO = 5;
    
    /**
     * Guarda una nueva tarjeta para un usuario
     * Encripta automáticamente los datos sensibles
     */
    public TarjetaGuardada guardarTarjeta(String usuarioId, String numeroTarjeta, String cvv,
                                         String nombreTitular, String mesExpiracion, 
                                         String anioExpiracion, String alias) throws Exception {
        
        // Validar datos
        if (numeroTarjeta == null || numeroTarjeta.length() < 13) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        
        if (cvv == null || cvv.length() < 3) {
            throw new IllegalArgumentException("CVV inválido");
        }
        
        // Validar límite de tarjetas
        long cantidadTarjetas = tarjetaRepositorio.countByUsuarioId(usuarioId);
        if (cantidadTarjetas >= MAX_TARJETAS_POR_USUARIO) {
            throw new IllegalArgumentException("Has alcanzado el límite máximo de " + MAX_TARJETAS_POR_USUARIO + " tarjetas guardadas");
        }
        
        // Extraer últimos 4 dígitos para validar duplicados
        String ultimos4 = encriptacionServicio.extraerUltimos4Digitos(numeroTarjeta);
        
        // Validar si ya existe una tarjeta con los mismos últimos 4 dígitos
        if (tarjetaRepositorio.existsByUsuarioIdAndUltimos4Digitos(usuarioId, ultimos4)) {
            throw new IllegalArgumentException("Ya tienes una tarjeta guardada con estos últimos 4 dígitos");
        }
        
        // Encriptar datos sensibles
        String numeroEncriptado = encriptacionServicio.encriptar(numeroTarjeta);
        String cvvEncriptado = encriptacionServicio.encriptar(cvv);
        
        // Detectar tipo de tarjeta
        String tipoTarjeta = encriptacionServicio.detectarTipoTarjeta(numeroTarjeta);
        
        // Crear entidad
        TarjetaGuardada tarjeta = new TarjetaGuardada(
            usuarioId, numeroEncriptado, cvvEncriptado,
            nombreTitular, ultimos4, tipoTarjeta,
            mesExpiracion, anioExpiracion
        );
        
        tarjeta.setAlias(alias);
        
        // Si es la primera tarjeta, marcarla como predeterminada
        if (cantidadTarjetas == 0) {
            tarjeta.setEsPredeterminada(true);
        }
        
        return tarjetaRepositorio.save(tarjeta);
    }
    
    /**
     * Obtiene todas las tarjetas de un usuario
     * NO retorna los datos encriptados por seguridad
     */
    public List<TarjetaGuardada> obtenerTarjetasUsuario(String usuarioId) {
        return tarjetaRepositorio.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }
    
    /**
     * Obtiene una tarjeta específica si pertenece al usuario
     */
    public Optional<TarjetaGuardada> obtenerTarjeta(String tarjetaId, String usuarioId) {
        if (tarjetaId == null || usuarioId == null) {
            return Optional.empty();
        }
        
        Optional<TarjetaGuardada> tarjetaOpt = tarjetaRepositorio.findById(tarjetaId);
        
        // Verificar que pertenece al usuario
        if (tarjetaOpt.isPresent() && tarjetaOpt.get().getUsuarioId().equals(usuarioId)) {
            return tarjetaOpt;
        }
        
        return Optional.empty();
    }
    
    /**
     * Desencripta el número de tarjeta (solo para uso en pagos)
     */
    public String desencriptarNumeroTarjeta(TarjetaGuardada tarjeta) throws Exception {
        return encriptacionServicio.desencriptar(tarjeta.getNumeroTarjetaEncriptado());
    }
    
    /**
     * Desencripta el CVV (solo para uso en pagos)
     */
    public String desencriptarCVV(TarjetaGuardada tarjeta) throws Exception {
        return encriptacionServicio.desencriptar(tarjeta.getCvvEncriptado());
    }
    
    /**
     * Establece una tarjeta como predeterminada
     */
    public void establecerComoPredeterminada(String tarjetaId, String usuarioId) {
        // Primero, quitar la marca de predeterminada de todas las tarjetas del usuario
        List<TarjetaGuardada> tarjetas = tarjetaRepositorio.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        for (TarjetaGuardada t : tarjetas) {
            if (t.isEsPredeterminada()) {
                t.setEsPredeterminada(false);
                tarjetaRepositorio.save(t);
            }
        }
        
        // Luego, marcar la tarjeta seleccionada como predeterminada
        Optional<TarjetaGuardada> tarjetaOpt = obtenerTarjeta(tarjetaId, usuarioId);
        if (tarjetaOpt.isPresent()) {
            TarjetaGuardada tarjeta = tarjetaOpt.get();
            tarjeta.setEsPredeterminada(true);
            tarjeta.setFechaActualizacion(LocalDateTime.now());
            tarjetaRepositorio.save(tarjeta);
        }
    }
    
    /**
     * Obtiene la tarjeta predeterminada del usuario
     */
    public Optional<TarjetaGuardada> obtenerTarjetaPredeterminada(String usuarioId) {
        return tarjetaRepositorio.findByUsuarioIdAndEsPredeterminadaTrue(usuarioId);
    }
    
    /**
     * Elimina una tarjeta si pertenece al usuario
     */
    public boolean eliminarTarjeta(String tarjetaId, String usuarioId) {
        Optional<TarjetaGuardada> tarjetaOpt = obtenerTarjeta(tarjetaId, usuarioId);
        
        if (tarjetaOpt.isPresent()) {
            TarjetaGuardada tarjeta = tarjetaOpt.get();
            
            // Si era la predeterminada, establecer otra como predeterminada
            if (tarjeta.isEsPredeterminada()) {
                List<TarjetaGuardada> otrasTarjetas = tarjetaRepositorio.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
                for (TarjetaGuardada t : otrasTarjetas) {
                    if (!t.getId().equals(tarjetaId)) {
                        t.setEsPredeterminada(true);
                        tarjetaRepositorio.save(t);
                        break;
                    }
                }
            }
            
            String id = tarjeta.getId();
            if (id != null) {
                tarjetaRepositorio.deleteById(id);
                return true;
            }
            return false;
        }
        
        return false;
    }
    
    /**
     * Actualiza el alias de una tarjeta
     */
    public boolean actualizarAlias(String tarjetaId, String usuarioId, String nuevoAlias) {
        Optional<TarjetaGuardada> tarjetaOpt = obtenerTarjeta(tarjetaId, usuarioId);
        
        if (tarjetaOpt.isPresent()) {
            TarjetaGuardada tarjeta = tarjetaOpt.get();
            tarjeta.setAlias(nuevoAlias);
            tarjeta.setFechaActualizacion(LocalDateTime.now());
            tarjetaRepositorio.save(tarjeta);
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica si una tarjeta está vencida
     */
    public boolean estaVencida(TarjetaGuardada tarjeta) {
        return tarjeta.estaVencida();
    }
    
    /**
     * Obtiene el conteo de tarjetas de un usuario
     */
    public long contarTarjetasUsuario(String usuarioId) {
        return tarjetaRepositorio.countByUsuarioId(usuarioId);
    }
    
    /**
     * Limpia tarjetas duplicadas de un usuario (mantiene solo una por cada últimos 4 dígitos)
     */
    public int limpiarTarjetasDuplicadas(String usuarioId) {
        List<TarjetaGuardada> tarjetas = tarjetaRepositorio.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        
        // Agrupar por últimos 4 dígitos
        Map<String, List<TarjetaGuardada>> tarjetasPorUltimos4 = new HashMap<>();
        for (TarjetaGuardada tarjeta : tarjetas) {
            String ultimos4 = tarjeta.getUltimos4Digitos();
            tarjetasPorUltimos4.computeIfAbsent(ultimos4, k -> new ArrayList<>()).add(tarjeta);
        }
        
        int eliminadas = 0;
        
        // Para cada grupo de tarjetas con los mismos últimos 4 dígitos
        for (Map.Entry<String, List<TarjetaGuardada>> entry : tarjetasPorUltimos4.entrySet()) {
            List<TarjetaGuardada> grupo = entry.getValue();
            
            // Si hay duplicados (más de 1 tarjeta con los mismos últimos 4 dígitos)
            if (grupo.size() > 1) {
                // Ordenar por fecha de creación (más reciente primero)
                grupo.sort((t1, t2) -> t2.getFechaCreacion().compareTo(t1.getFechaCreacion()));
                
                // Mantener la primera (más reciente) y eliminar el resto
                for (int i = 1; i < grupo.size(); i++) {
                    String id = grupo.get(i).getId();
                    if (id != null) {
                        tarjetaRepositorio.deleteById(id);
                        eliminadas++;
                    }
                }
            }
        }
        
        return eliminadas;
    }
}
