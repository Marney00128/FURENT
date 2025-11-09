package com.furniterental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor que aplica cabeceras de no-caché a respuestas HTML dinámicas
 * para evitar que el navegador muestre páginas obsoletas al usar el botón Atrás
 * tras iniciar o cerrar sesión.
 */
@Component
public class NoCacheInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        // Evitar cacheo del contenido dinámico
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
