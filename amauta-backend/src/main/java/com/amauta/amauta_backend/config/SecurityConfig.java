package com.amauta.amauta_backend.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.context.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
@Configuration
public class SecurityConfig {
    @Bean org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        // El acceso se realiza por el controlador de sesion; no generar un usuario alternativo.
        return username -> { throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username); };
    }
    @Bean BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository repository,
            PermisosEstadia permisos) throws Exception {
        http.cors(cors -> {})
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .securityContext(context -> context.securityContextRepository(repository))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/usuarios/login", "/api/usuarios/csrf", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                .requestMatchers("/api/usuarios/**").access(roles(permisos, "ADMINISTRADOR"))
                .requestMatchers(HttpMethod.GET, "/api/habitaciones", "/api/habitaciones/**")
                    .access(roles(permisos, "ADMINISTRADOR", "RECEPCIONISTA"))
                .requestMatchers(HttpMethod.PUT, "/api/habitaciones/*")
                    .access(roles(permisos, "ADMINISTRADOR", "RECEPCIONISTA"))
                .requestMatchers("/api/habitaciones", "/api/habitaciones/**")
                    .access(roles(permisos, "ADMINISTRADOR"))
                .requestMatchers(HttpMethod.PUT, "/api/check-out/configuracion")
                    .access(roles(permisos, "ADMINISTRADOR"))
                .requestMatchers("/api/check-out/**", "/api/recepcion/**", "/api/registros/**",
                        "/reservas", "/reservas/**", "/api/huespedes", "/api/huespedes/**")
                    .access(roles(permisos, "ADMINISTRADOR", "RECEPCIONISTA"))
                .requestMatchers("/api/ventas-productos", "/api/ventas-productos/**",
                        "/api/boletas", "/api/boletas/**", "/api/caja-diaria", "/api/caja-diaria/**",
                        "/api/productos", "/api/productos/**", "/api/stock-ubicacion", "/api/stock-ubicacion/**",
                        "/api/movimientos", "/api/movimientos/**", "/api/equipamiento-habitaciones", "/api/equipamiento-habitaciones/**")
                    .access(roles(permisos, "ADMINISTRADOR", "RECEPCIONISTA"))
                .requestMatchers("/api/**").access(roles(permisos, "ADMINISTRADOR"))
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(401); res.setContentType("application/json");
                    res.getWriter().write("{\"message\":\"Inicia sesion para continuar\"}");
                })
                .accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(403); res.setContentType("application/json");
                    String message = ex instanceof org.springframework.security.web.csrf.CsrfException
                            ? "Sesion de seguridad vencida. Recarga la pagina"
                            : "No tienes permisos para esta operacion";
                    res.getWriter().write("{\"message\":\"" + message + "\"}");
                }))
            .logout(logout -> logout.logoutUrl("/api/usuarios/logout")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(204)));
        return http.build();
    }

    private static AuthorizationManager<RequestAuthorizationContext> roles(PermisosEstadia permisos, String... names) {
        Set<String> allowed = Arrays.stream(names).collect(Collectors.toUnmodifiableSet());
        return (authentication, context) -> {
            var current = authentication.get();
            boolean granted = permisos.permite(current, allowed);
            return new AuthorizationDecision(granted);
        };
    }
}
