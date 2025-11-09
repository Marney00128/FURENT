package com.furniterental.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio de encriptación AES-256-GCM para datos sensibles
 * Utiliza AES en modo GCM (Galois/Counter Mode) que proporciona:
 * - Confidencialidad (encriptación)
 * - Autenticidad (detección de manipulación)
 * - Integridad (detección de corrupción)
 */
@Service
public class EncriptacionServicio {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes (96 bits recomendado para GCM)
    private static final int AES_KEY_SIZE = 256; // bits
    
    // Clave secreta para encriptación (debería estar en variables de entorno en producción)
    @Value("${app.encryption.secret:FURENT-SECRET-KEY-2024-CHANGE-IN-PRODUCTION-32CHARS}")
    private String secretKeyString;
    
    /**
     * Encripta un texto usando AES-256-GCM
     * @param plainText Texto a encriptar
     * @return Texto encriptado en Base64 (incluye IV + datos encriptados)
     */
    public String encriptar(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("El texto a encriptar no puede estar vacío");
        }
        
        // Generar IV aleatorio (Initialization Vector)
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        
        // Preparar la clave secreta
        SecretKey secretKey = getSecretKey();
        
        // Configurar el cifrador
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        // Encriptar
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        
        // Combinar IV + datos encriptados
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        
        // Retornar en Base64
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
    
    /**
     * Desencripta un texto encriptado con AES-256-GCM
     * @param encryptedText Texto encriptado en Base64
     * @return Texto desencriptado
     */
    public String desencriptar(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("El texto a desencriptar no puede estar vacío");
        }
        
        // Decodificar de Base64
        byte[] decodedData = Base64.getDecoder().decode(encryptedText);
        
        // Extraer IV y datos encriptados
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] encryptedData = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedData);
        
        // Preparar la clave secreta
        SecretKey secretKey = getSecretKey();
        
        // Configurar el cifrador
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        
        // Desencriptar
        byte[] decryptedData = cipher.doFinal(encryptedData);
        
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
    
    /**
     * Obtiene la clave secreta desde la configuración
     * En producción, esta clave debería venir de variables de entorno o un gestor de secretos
     */
    private SecretKey getSecretKey() {
        // Asegurar que la clave tenga exactamente 32 bytes (256 bits)
        byte[] keyBytes = new byte[32];
        byte[] secretBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        
        // Copiar los bytes de la clave secreta (truncar o rellenar si es necesario)
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, 32));
        
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    /**
     * Genera una nueva clave AES-256 aleatoria (útil para generar claves seguras)
     * Este método es solo para referencia, no se usa en producción
     */
    public static String generarClaveAleatoria() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
    
    /**
     * Valida que un texto encriptado pueda ser desencriptado correctamente
     * Útil para verificar la integridad de los datos
     */
    public boolean validarIntegridad(String encryptedText) {
        try {
            desencriptar(encryptedText);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Enmascara un número de tarjeta mostrando solo los últimos 4 dígitos
     */
    public String enmascararNumeroTarjeta(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.length() < 4) {
            return "****";
        }
        return "**** **** **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }
    
    /**
     * Extrae los últimos 4 dígitos de un número de tarjeta
     */
    public String extraerUltimos4Digitos(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.length() < 4) {
            return "0000";
        }
        return numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }
    
    /**
     * Detecta el tipo de tarjeta basándose en el número
     */
    public String detectarTipoTarjeta(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) {
            return "DESCONOCIDA";
        }
        
        // Remover espacios y guiones
        String numero = numeroTarjeta.replaceAll("[\\s-]", "");
        
        // Detectar por el primer dígito o primeros dígitos
        if (numero.startsWith("4")) {
            return "VISA";
        } else if (numero.startsWith("5")) {
            return "MASTERCARD";
        } else if (numero.startsWith("3")) {
            if (numero.startsWith("34") || numero.startsWith("37")) {
                return "AMEX";
            }
            return "DINERS";
        } else if (numero.startsWith("6")) {
            return "DISCOVER";
        }
        
        return "DESCONOCIDA";
    }
}
