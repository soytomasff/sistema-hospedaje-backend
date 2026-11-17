package com.amauta.amauta_backend.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
public class DatabaseConfiguration {

    @Bean
    static BeanFactoryPostProcessor validateDatabaseCredentials(Environment environment) {
        return beanFactory -> {
            String url = environment.getProperty("spring.datasource.url", "");
            String password = environment.getProperty("spring.datasource.password", "");
            if (url.startsWith("jdbc:postgresql:") && password.isBlank()) {
                throw new IllegalStateException(
                        "Falta la contrasena de PostgreSQL. Define DB_PASSWORD (o "
                        + "SPRING_DATASOURCE_PASSWORD) en el entorno del proceso Java, "
                        + "o inicia con scripts/start-backend.ps1 para introducirla de forma segura. "
                        + "La contrasena de login de Amauta no es la de PostgreSQL.");
            }
        };
    }
}
