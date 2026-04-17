package com.sigma.sahur.studio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring MVC de l'application.
 * Définit notamment la politique CORS pour les appels depuis le frontend.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Autorise les requêtes CORS depuis toutes les origines pour tous les endpoints {@code /api/**}.
     * Méthodes HTTP autorisées : GET, POST, PUT, PATCH, DELETE, OPTIONS.
     *
     * @param registry registre de configuration CORS de Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
