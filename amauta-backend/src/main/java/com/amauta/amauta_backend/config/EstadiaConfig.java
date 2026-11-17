package com.amauta.amauta_backend.config;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import java.time.*;

@Configuration
@EnableScheduling
@EnableMethodSecurity
public class EstadiaConfig {
    @Bean Clock estadiaClock() { return Clock.system(ZoneId.of("America/Lima")); }
}
