package com.furniterental.servicio;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TwoFactorAuthServicio {
    
    private final GoogleAuthenticator gAuth;
    private final Map<String, String> userSecrets; // Temporal - en producción está en BD
    
    public TwoFactorAuthServicio() {
        this.gAuth = new GoogleAuthenticator();
        this.userSecrets = new HashMap<>();
        initializeReportesSecret();
    }
    
    private void initializeReportesSecret() {
        String fixedSecret = "JBSWY3DPEHPK3PXP";
        userSecrets.put("reportes", fixedSecret);
        System.out.println("=== CLAVE SECRETA PARA REPORTES (FIJA) ===");
        System.out.println("Secret Key: " + fixedSecret);
        System.out.println("Escanea el QR en /admin/reportes/setup-2fa");
        System.out.println("===========================================");
    }
    
    /**
     * Generar nueva clave secreta para un usuario
     */
    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }
    
    /**
     * Generar URL del QR Code para Google Authenticator
     * Formato: otpauth://totp/ISSUER:ACCOUNT?secret=SECRET&issuer=ISSUER
     */
    public String generateQRCodeUrl(String secret, String userEmail) {
        // Generar la URL otpauth manualmente para asegurar compatibilidad
        String issuer = "FURENT";
        String account = userEmail;
        
        // Formato correcto para Google Authenticator
        // otpauth://totp/FURENT:email@example.com?secret=SECRET&issuer=FURENT&algorithm=SHA1&digits=6&period=30
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
            issuer,
            account,
            secret,
            issuer
        );
    }
    
    /**
     * Verificar código de autenticación de un usuario
     */
    public boolean verifyCode(String secret, String code) {
        try {
            if (secret == null || secret.isEmpty()) {
                return false;
            }
            int codeInt = Integer.parseInt(code);
            return gAuth.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // ========== Métodos para reportes (mantener compatibilidad) ==========
    
    public String getReportesSecret() {
        return userSecrets.get("reportes");
    }
    
    public String getQRCodeUrl(String issuer, String accountName) {
        String secret = getReportesSecret();
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret).build();
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, accountName, key);
    }
    
    public boolean verifyCode(String code) {
        try {
            String secret = getReportesSecret();
            int codeInt = Integer.parseInt(code);
            return gAuth.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public String regenerateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        userSecrets.put("reportes", key.getKey());
        return key.getKey();
    }
    
    public String getQRCodeUrlForReportes() {
        return getQRCodeUrl("FURENT Admin", "reportes@furent.com");
    }
}
