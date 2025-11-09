package com.furniterental.servicio;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio para generar y validar tokens CSRF (Cross-Site Request Forgery)
 * Protege contra ataques de falsificación de peticiones entre sitios
 */
@Service
public class CsrfTokenServicio {

    private static final String CSRF_TOKEN_SESSION_ATTRIBUTE = "CSRF_TOKEN";
    private static final int TOKEN_LENGTH = 32; // 32 bytes = 256 bits
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Genera un nuevo token CSRF y lo almacena en la sesión
     * @param session La sesión HTTP actual
     * @return El token CSRF generado
     */
    public String generarToken(HttpSession session) {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Guardar token en la sesión
        session.setAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE, token);
        
        return token;
    }

    /**
     * Obtiene el token CSRF actual de la sesión, o genera uno nuevo si no existe
     * @param session La sesión HTTP actual
     * @return El token CSRF
     */
    public String obtenerToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE);
        
        if (token == null || token.isEmpty()) {
            token = generarToken(session);
        }
        
        return token;
    }

    /**
     * Valida que el token proporcionado coincida con el token en la sesión
     * @param session La sesión HTTP actual
     * @param tokenRecibido El token recibido en la petición
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validarToken(HttpSession session, String tokenRecibido) {
        if (tokenRecibido == null || tokenRecibido.isEmpty()) {
            return false;
        }

        String tokenSesion = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE);
        
        if (tokenSesion == null || tokenSesion.isEmpty()) {
            return false;
        }

        // Comparación segura contra timing attacks
        return constantTimeEquals(tokenSesion, tokenRecibido);
    }

    /**
     * Invalida el token CSRF actual (útil después de operaciones críticas)
     * @param session La sesión HTTP actual
     */
    public void invalidarToken(HttpSession session) {
        session.removeAttribute(CSRF_TOKEN_SESSION_ATTRIBUTE);
    }

    /**
     * Regenera el token CSRF (útil después de login o cambios de privilegios)
     * @param session La sesión HTTP actual
     * @return El nuevo token generado
     */
    public String regenerarToken(HttpSession session) {
        invalidarToken(session);
        return generarToken(session);
    }

    /**
     * Comparación de strings en tiempo constante para prevenir timing attacks
     * @param a Primer string
     * @param b Segundo string
     * @return true si son iguales, false en caso contrario
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }
}
