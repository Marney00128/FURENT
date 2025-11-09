package com.furniterental.servicio;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Servicio para validar las transiciones de estados de alquileres
 * Asegura que los estados sigan un flujo lógico sin saltar pasos
 */
@Service
public class AlquilerEstadoValidacionServicio {

    // Mapa de transiciones válidas: estado actual -> lista de estados permitidos
    private static final Map<String, List<String>> TRANSICIONES_VALIDAS = new HashMap<>();
    
    static {
        // PENDIENTE puede pasar a CONFIRMADO solamente
        TRANSICIONES_VALIDAS.put("PENDIENTE", Arrays.asList("CONFIRMADO"));
        
        // CONFIRMADO puede pasar a EN_CURSO solamente
        TRANSICIONES_VALIDAS.put("CONFIRMADO", Arrays.asList("EN_CURSO"));
        
        // EN_CURSO puede pasar a COMPLETADO solamente
        TRANSICIONES_VALIDAS.put("EN_CURSO", Arrays.asList("COMPLETADO"));
        
        // COMPLETADO es un estado final, no puede cambiar
        TRANSICIONES_VALIDAS.put("COMPLETADO", Arrays.asList());
    }
    
    /**
     * Valida si una transición de estado es válida
     * @param estadoActual El estado actual del alquiler
     * @param nuevoEstado El nuevo estado al que se quiere cambiar
     * @return true si la transición es válida, false en caso contrario
     */
    public boolean esTransicionValida(String estadoActual, String nuevoEstado) {
        // Si el estado no cambia, es válido (no hacer nada)
        if (estadoActual.equals(nuevoEstado)) {
            return true;
        }
        
        // Obtener las transiciones válidas para el estado actual
        List<String> estadosPermitidos = TRANSICIONES_VALIDAS.get(estadoActual);
        
        // Si no hay transiciones definidas para este estado, no es válido
        if (estadosPermitidos == null) {
            return false;
        }
        
        // Verificar si el nuevo estado está en la lista de estados permitidos
        return estadosPermitidos.contains(nuevoEstado);
    }
    
    /**
     * Obtiene el siguiente estado válido para un estado dado
     * @param estadoActual El estado actual del alquiler
     * @return El siguiente estado válido, o null si no hay siguiente estado
     */
    public String obtenerSiguienteEstado(String estadoActual) {
        List<String> estadosPermitidos = TRANSICIONES_VALIDAS.get(estadoActual);
        
        if (estadosPermitidos == null || estadosPermitidos.isEmpty()) {
            return null;
        }
        
        // Retornar el primer (y generalmente único) estado permitido
        return estadosPermitidos.get(0);
    }
    
    /**
     * Obtiene todos los estados permitidos desde un estado actual
     * @param estadoActual El estado actual del alquiler
     * @return Lista de estados permitidos
     */
    public List<String> obtenerEstadosPermitidos(String estadoActual) {
        List<String> estadosPermitidos = TRANSICIONES_VALIDAS.get(estadoActual);
        
        if (estadosPermitidos == null) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(estadosPermitidos);
    }
    
    /**
     * Obtiene un mensaje de error descriptivo para una transición inválida
     * @param estadoActual El estado actual del alquiler
     * @param nuevoEstado El nuevo estado que se intentó establecer
     * @return Mensaje de error descriptivo
     */
    public String obtenerMensajeError(String estadoActual, String nuevoEstado) {
        if (estadoActual.equals(nuevoEstado)) {
            return "El alquiler ya está en el estado " + estadoActual;
        }
        
        List<String> estadosPermitidos = TRANSICIONES_VALIDAS.get(estadoActual);
        
        if (estadosPermitidos == null || estadosPermitidos.isEmpty()) {
            return "El estado " + estadoActual + " es un estado final y no puede ser modificado";
        }
        
        String siguienteEstado = estadosPermitidos.get(0);
        
        return String.format(
            "No se puede cambiar de %s a %s. El siguiente estado válido es: %s",
            obtenerNombreAmigable(estadoActual),
            obtenerNombreAmigable(nuevoEstado),
            obtenerNombreAmigable(siguienteEstado)
        );
    }
    
    /**
     * Convierte el código de estado a un nombre amigable
     * @param estado El código del estado
     * @return Nombre amigable del estado
     */
    private String obtenerNombreAmigable(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "Pendiente";
            case "CONFIRMADO" -> "Confirmado";
            case "EN_CURSO" -> "En Curso";
            case "COMPLETADO" -> "Completado";
            case "CANCELADO" -> "Cancelado";
            default -> estado;
        };
    }
    
    /**
     * Obtiene el flujo completo de estados
     * @return Lista ordenada de estados en el flujo normal
     */
    public List<String> obtenerFlujoCompleto() {
        return Arrays.asList("PENDIENTE", "CONFIRMADO", "EN_CURSO", "COMPLETADO");
    }
    
    /**
     * Verifica si un estado es un estado final
     * @param estado El estado a verificar
     * @return true si es un estado final, false en caso contrario
     */
    public boolean esEstadoFinal(String estado) {
        List<String> estadosPermitidos = TRANSICIONES_VALIDAS.get(estado);
        return estadosPermitidos != null && estadosPermitidos.isEmpty();
    }
    
    /**
     * Obtiene información detallada sobre el flujo de estados
     * @return Mapa con información del flujo
     */
    public Map<String, Object> obtenerInformacionFlujo() {
        Map<String, Object> info = new HashMap<>();
        info.put("flujo", obtenerFlujoCompleto());
        info.put("transiciones", TRANSICIONES_VALIDAS);
        info.put("descripcion", "Flujo de estados: PENDIENTE → CONFIRMADO → EN_CURSO → COMPLETADO");
        return info;
    }
}
