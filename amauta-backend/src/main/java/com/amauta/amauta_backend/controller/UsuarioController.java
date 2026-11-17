package com.amauta.amauta_backend.controller;
import com.amauta.amauta_backend.entity.Usuario;
import com.amauta.amauta_backend.repository.UsuarioRepository;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "RECEPCIONISTA");
    public record NuevoUsuario(String username, String password, String rol) { }
    public record CambioRol(String rol) { }
    private final UsuarioRepository usuarios;
    private final BCryptPasswordEncoder encoder;
    private final SecurityContextRepository contexts;
    public UsuarioController(UsuarioRepository usuarios, BCryptPasswordEncoder encoder, SecurityContextRepository contexts) {
        this.usuarios = usuarios; this.encoder = encoder; this.contexts = contexts;
    }
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Usuario datos, HttpServletRequest request, HttpServletResponse response) {
        Usuario user = usuarios.findByUsername(datos.getUsername()).orElseThrow(this::incorrecto);
        String stored = user.getPassword();
        String password = datos.getPassword();
        boolean hashed = stored != null && stored.startsWith("$2");
        if (password == null || stored == null ||
                !(hashed ? encoder.matches(password, stored) : stored.equals(password))) throw incorrecto();
        // Migracion gradual sin cambiar la clave del usuario existente.
        if (!hashed) { user.setPassword(encoder.encode(password)); usuarios.save(user); }
        if (request.getSession(false) != null) request.changeSessionId();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + (user.getRol() == null ? "USUARIO" : user.getRol())))));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return publico(user);
    }
    @GetMapping("/me")
    public Map<String, Object> me(java.security.Principal principal) {
        return publico(usuarios.findByUsername(principal.getName()).orElseThrow(this::incorrecto));
    }
    @GetMapping
    @PreAuthorize("@permisosEstadia.administrador()")
    public List<Map<String, Object>> listar() {
        return usuarios.findAll().stream().map(this::publico).toList();
    }
    @PostMapping
    @PreAuthorize("@permisosEstadia.administrador()")
    public Map<String, Object> crear(@RequestBody NuevoUsuario datos) {
        if (datos == null || datos.username() == null || datos.username().isBlank()
                || datos.password() == null || datos.password().length() < 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica usuario y contrasena de al menos 12 caracteres");
        String username = datos.username().trim();
        if (usuarios.findByUsername(username).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setPassword(encoder.encode(datos.password()));
        nuevo.setRol(rolValido(datos.rol()));
        return publico(usuarios.save(nuevo));
    }
    @PutMapping("/{id}/rol")
    @PreAuthorize("@permisosEstadia.administrador()")
    public Map<String, Object> cambiarRol(@PathVariable Long id, @RequestBody CambioRol datos,
            java.security.Principal principal) {
        Usuario usuario = usuarios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        String nuevoRol = rolValido(datos == null ? null : datos.rol());
        if (usuario.getUsername().equals(principal.getName())
                && !"ADMINISTRADOR".equals(nuevoRol))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes retirar tu propio acceso administrativo");
        usuario.setRol(nuevoRol);
        return publico(usuarios.save(usuario));
    }
    private String rolValido(String valor) {
        String rol = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        if (!ROLES.contains(rol)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no permitido");
        return rol;
    }
    private Map<String, Object> publico(Usuario user) {
        return Map.of("id", user.getId(), "username", user.getUsername(), "rol", user.getRol() == null ? "USUARIO" : user.getRol());
    }
    private ResponseStatusException incorrecto() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
    }
}
