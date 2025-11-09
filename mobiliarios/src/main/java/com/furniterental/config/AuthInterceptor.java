package com.furniterental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para validar autenticación en rutas protegidas
 * Verifica que el usuario tenga una sesión activa válida
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

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

        // Usuario autenticado, permitir acceso
        return true;
    }
}
