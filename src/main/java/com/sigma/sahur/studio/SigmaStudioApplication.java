package com.sigma.sahur.studio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Point d'entrée de l'application Sigma Sahur Studio 3 Pro.
 * Étend {@link SpringBootServletInitializer} pour permettre le déploiement en WAR sur un Tomcat externe.
 */
@SpringBootApplication
public class SigmaStudioApplication extends SpringBootServletInitializer {

    /**
     * Configure l'application pour le déploiement WAR sur un Tomcat externe.
     *
     * @param application builder Spring Boot fourni par le conteneur de servlet
     * @return builder configuré avec la classe principale comme source
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SigmaStudioApplication.class);
    }

    /**
     * Lance l'application Spring Boot en mode autonome (JAR/WAR exécutable).
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(SigmaStudioApplication.class, args);
    }
}
