package com.furniterental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para validar autenticación de administradores
 * Verifica que el usuario tenga rol de ADMIN
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        
        // Verificar si hay una sesión activa
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Verificar si el usuario está autenticado (independiente del tipo)
        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Verificar si el usuario es administrador
        String rol = (String) session.getAttribute("usuarioRol");
        if (!"ADMIN".equals(rol)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Se requieren permisos de administrador");
            return false;
        }

        // Usuario administrador autenticado, permitir acceso
        return true;
    }
}
