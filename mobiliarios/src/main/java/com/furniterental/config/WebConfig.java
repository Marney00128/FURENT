package com.furniterental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Autowired
    private NoCacheInterceptor noCacheInterceptor;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
                
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
    
    @Override
    @SuppressWarnings("null")
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Interceptor para evitar caché del contenido dinámico
        registry.addInterceptor(noCacheInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**", "/js/**", "/img/**", "/vendor/**", "/webjars/**"
                );
        
        // Interceptor para rutas de usuario autenticado
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/alquiler/**")
                .addPathPatterns("/carrito/**")
                .addPathPatterns("/favoritos/**")
                .excludePathPatterns("/login", "/register", "/logout", "/css/**", "/js/**", "/img/**", "/vendor/**");
        
        // Interceptor para rutas de administrador
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/vendor/**");
    }
}