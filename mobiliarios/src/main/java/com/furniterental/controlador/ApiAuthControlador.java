package com.furniterental.controlador;

import com.furniterental.modelo.Usuario;
import com.furniterental.repositorio.UsuarioRepositorio;
import com.furniterental.servicio.JwtTokenServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para autenticación con JWT
 * Proporciona endpoints para login y validación de tokens
 */
@RestController
@RequestMapping("/api/auth")
public class ApiAuthControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenServicio jwtTokenServicio;

    /**
     * Endpoint para login y generación de token JWT
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();

        try {
            String correo = credentials.get("correo");
            String contrasena = credentials.get("contrasena");

            // Validar credenciales
            if (correo == null || contrasena == null) {
                response.put("success", false);
                response.put("message", "Correo y contraseña son requeridos");
                return ResponseEntity.badRequest().body(response);
            }

            // Normalizar correo a minúsculas y sin espacios
            correo = correo != null ? correo.trim().toLowerCase(Locale.ROOT) : null;

            // Buscar usuario
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findByCorreo(correo);
            if (usuarioOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Credenciales inválidas");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar contraseña
            if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
                response.put("success", false);
                response.put("message", "Credenciales inválidas");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Generar token JWT
            String token = jwtTokenServicio.generarToken(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getRol()
            );

            // Respuesta exitosa
            response.put("success", true);
            response.put("message", "Login exitoso");
            response.put("token", token);
            response.put("usuario", Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "correo", usuario.getCorreo(),
                "rol", usuario.getRol()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para validar un token JWT
     * POST /api/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String token = request.get("token");

            if (token == null || token.isEmpty()) {
                response.put("success", false);
                response.put("message", "Token es requerido");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar token
            boolean isValid = jwtTokenServicio.validarToken(token);

            if (isValid) {
                // Extraer información del token
                Map<String, Object> tokenInfo = jwtTokenServicio.obtenerInformacionToken(token);
                response.put("success", true);
                response.put("message", "Token válido");
                response.put("tokenInfo", tokenInfo);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Token inválido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al validar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint protegido de ejemplo que requiere JWT
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar header Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "Token de autorización requerido. Formato: Bearer <token>");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extraer token del header
            String token = authHeader.substring(7);

            // Validar token
            if (!jwtTokenServicio.validarToken(token)) {
                response.put("success", false);
                response.put("message", "Token inválido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extraer información del usuario del token
            String usuarioId = jwtTokenServicio.extraerUsuarioId(token);
            
            // Validar que el usuarioId no sea nulo
            if (usuarioId == null || usuarioId.isEmpty()) {
                response.put("success", false);
                response.put("message", "Token inválido: no contiene ID de usuario");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Buscar usuario en la base de datos
            Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Usuario usuario = usuarioOpt.get();

            // Respuesta con información del perfil
            response.put("success", true);
            response.put("message", "Perfil obtenido exitosamente");
            response.put("usuario", Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "correo", usuario.getCorreo(),
                "telefono", usuario.getTelefono() != null ? usuario.getTelefono() : "",
                "rol", usuario.getRol(),
                "twoFactorEnabled", usuario.isTwoFactorEnabled(),
                "ultimoInicioSesion", usuario.getUltimoInicioSesion() != null ? usuario.getUltimoInicioSesion().toString() : "Nunca"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener perfil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint de ejemplo para refrescar token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar header Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "Token de autorización requerido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extraer token del header
            String oldToken = authHeader.substring(7);

            // Validar token (puede estar expirado, pero debe ser válido en estructura)
            String usuarioId = jwtTokenServicio.extraerUsuarioId(oldToken);
            String correo = jwtTokenServicio.extraerCorreo(oldToken);
            String rol = jwtTokenServicio.extraerRol(oldToken);

            // Generar nuevo token
            String newToken = jwtTokenServicio.generarToken(usuarioId, correo, rol);

            response.put("success", true);
            response.put("message", "Token refrescado exitosamente");
            response.put("token", newToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al refrescar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
