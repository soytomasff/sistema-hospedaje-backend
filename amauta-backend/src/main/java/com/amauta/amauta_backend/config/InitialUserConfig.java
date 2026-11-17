package com.amauta.amauta_backend.config;

import com.amauta.amauta_backend.entity.Usuario;
import com.amauta.amauta_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class InitialUserConfig {
    @Bean
    ApplicationRunner initialUser(UsuarioRepository usuarios, BCryptPasswordEncoder encoder,
            @Value("${BOOTSTRAP_ADMIN_USERNAME:admin}") String username,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        return args -> {
            if (!password.isBlank() && usuarios.count() == 0) {
                if (password.length() < 12) throw new IllegalArgumentException("La clave inicial necesita al menos 12 caracteres");
                Usuario usuario = new Usuario();
                usuario.setUsername(username); usuario.setPassword(encoder.encode(password)); usuario.setRol("ADMIN");
                usuarios.save(usuario);
            }
        };
    }
}
