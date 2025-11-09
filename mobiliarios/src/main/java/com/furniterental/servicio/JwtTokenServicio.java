package com.furniterental.servicio;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para generar y validar JSON Web Tokens (JWT)
 * Proporciona autenticación stateless y segura
 */
@Service
public class JwtTokenServicio {

    // Clave secreta para firmar los tokens (debe ser de al menos 256 bits)
    @Value("${jwt.secret:FURENT_SECRET_KEY_2024_SUPER_SECURE_KEY_FOR_JWT_AUTHENTICATION_DO_NOT_SHARE_THIS_KEY}")
    private String secretKey;

    // Tiempo de expiración del token en milisegundos (24 horas)
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * Genera un token JWT para un usuario
     * @param usuarioId ID del usuario
     * @param usuarioCorreo Correo del usuario
     * @param usuarioRol Rol del usuario (USER o ADMIN)
     * @return Token JWT generado
     */
    public String generarToken(String usuarioId, String usuarioCorreo, String usuarioRol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuarioId);
        claims.put("email", usuarioCorreo);
        claims.put("role", usuarioRol);
        
        return crearToken(claims, usuarioCorreo);
    }

    /**
     * Crea un token JWT con los claims especificados
     * @param claims Información adicional a incluir en el token
     * @param subject Sujeto del token (generalmente el correo del usuario)
     * @return Token JWT
     */
    private String crearToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtiene la clave de firma para los tokens
     * @return SecretKey para firmar tokens
     */
    private SecretKey getSigningKey() {
        // Codificar la clave en Base64 para asegurar 256 bits
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el correo del usuario del token
     * @param token Token JWT
     * @return Correo del usuario
     */
    public String extraerCorreo(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID del usuario del token
     * @param token Token JWT
     * @return ID del usuario
     */
    public String extraerUsuarioId(String token) {
        return extraerClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extrae el rol del usuario del token
     * @param token Token JWT
     * @return Rol del usuario
     */
    public String extraerRol(String token) {
        return extraerClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrae la fecha de expiración del token
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim
     * @return Valor del claim
     */
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extraerTodosClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si el token ha expirado
     * @param token Token JWT
     * @return true si el token ha expirado, false en caso contrario
     */
    public Boolean esTokenExpirado(String token) {
        try {
            return extraerExpiracion(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Valida si el token es válido para un usuario específico
     * @param token Token JWT
     * @param usuarioCorreo Correo del usuario
     * @return true si el token es válido, false en caso contrario
     */
    public Boolean validarToken(String token, String usuarioCorreo) {
        try {
            final String correoToken = extraerCorreo(token);
            return (correoToken.equals(usuarioCorreo) && !esTokenExpirado(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida si el token es válido (sin verificar usuario específico)
     * @param token Token JWT
     * @return true si el token es válido, false en caso contrario
     */
    public Boolean validarToken(String token) {
        try {
            extraerTodosClaims(token);
            return !esTokenExpirado(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene información completa del token
     * @param token Token JWT
     * @return Mapa con la información del token
     */
    public Map<String, Object> obtenerInformacionToken(String token) {
        Map<String, Object> info = new HashMap<>();
        try {
            info.put("userId", extraerUsuarioId(token));
            info.put("email", extraerCorreo(token));
            info.put("role", extraerRol(token));
            info.put("expiration", extraerExpiracion(token));
            info.put("isExpired", esTokenExpirado(token));
            info.put("isValid", validarToken(token));
        } catch (Exception e) {
            info.put("error", "Token inválido: " + e.getMessage());
            info.put("isValid", false);
        }
        return info;
    }
}
