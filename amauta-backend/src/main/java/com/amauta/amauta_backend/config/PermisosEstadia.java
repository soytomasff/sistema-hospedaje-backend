package com.amauta.amauta_backend.config;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Locale;
import com.amauta.amauta_backend.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;

@Component("permisosEstadia")
public class PermisosEstadia {
    private final UsuarioRepository usuarios;
    public PermisosEstadia(UsuarioRepository usuarios) { this.usuarios = usuarios; }
    public boolean administrador() { return tiene(Set.of("ADMINISTRADOR")); }
    public boolean recepcion() { return tiene(Set.of("ADMINISTRADOR", "RECEPCIONISTA")); }
    public boolean limpieza() { return recepcion(); }
    public boolean permite(Authentication auth, Set<String> roles) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return usuarios.findByUsername(auth.getName()).map(usuario -> usuario.getRol() != null
                && roles.contains(usuario.getRol().trim().toUpperCase(Locale.ROOT))).orElse(false);
    }
    private boolean tiene(Set<String> roles) {
        return permite(SecurityContextHolder.getContext().getAuthentication(), roles);
    }
}
