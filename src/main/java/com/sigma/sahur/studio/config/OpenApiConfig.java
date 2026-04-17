package com.sigma.sahur.studio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration SpringDoc pour la documentation OpenAPI (Swagger UI).
 * Accessible à {@code /swagger-ui/index.html} et {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Définit les métadonnées de l'API exposées dans Swagger UI.
     *
     * @return instance {@link OpenAPI} configurée avec le titre, la description et la version de l'API
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sigma Sahur Studio 3 Pro — API")
                        .description("API REST de gestion du studio de photographie professionnel")
                        .version("1.0.0"));
    }
}
