package com.maxiguias.maxigestion.maxigestion.configuracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxiguias.maxigestion.maxigestion.interceptor.PermisoInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermisoInterceptor permisoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permisoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/login",
                        "/registro",
                        "/recuperar-password",
                        "/cambiar-password",
                        "/auth/**",
                        "/api/**",
                        "/error/**",
                        "/",
                        "/productos/publico" // agregado
                );
    }
}
