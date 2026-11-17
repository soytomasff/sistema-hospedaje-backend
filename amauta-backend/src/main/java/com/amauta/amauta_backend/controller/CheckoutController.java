package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.*;
import com.amauta.amauta_backend.service.*;
import com.amauta.amauta_backend.repository.MovimientoEstadiaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/check-out")
@PreAuthorize("@permisosEstadia.recepcion() || @permisosEstadia.limpieza()")
public class CheckoutController {
    public record Horario(LocalTime horaSalida) {}
    public record Programacion(LocalDateTime salidaPrevista) {}
    public record Salida(LocalDateTime salidaReal) {}
    private final CheckoutService checkout;
    private final ConfiguracionEstadiaService configuracion;
    private final HabitacionService habitaciones;
    private final MovimientoEstadiaRepository movimientos;
    public CheckoutController(CheckoutService checkout, ConfiguracionEstadiaService configuracion,
            HabitacionService habitaciones, MovimientoEstadiaRepository movimientos) {
        this.checkout = checkout; this.configuracion = configuracion; this.habitaciones = habitaciones; this.movimientos = movimientos;
    }
    @GetMapping("/configuracion")
    @PreAuthorize("@permisosEstadia.recepcion()")
    public Map<String, Object> configuracion() {
        return Map.of("horaSalida", configuracion.horaSalida(), "zonaHoraria", "America/Lima", "ahora", checkout.ahora());
    }
    @PutMapping("/configuracion")
    @PreAuthorize("@permisosEstadia.administrador()")
    public Map<String, Object> configurar(@RequestBody Horario datos) {
        return Map.of("horaSalida", configuracion.cambiar(datos.horaSalida()), "zonaHoraria", "America/Lima");
    }
    @PutMapping("/estadias/{id}/salida-prevista")
    @PreAuthorize("@permisosEstadia.recepcion()")
    public Registro programar(@PathVariable Long id, @RequestBody Programacion datos, Principal usuario) {
        return checkout.programar(id, datos.salidaPrevista(), usuario.getName());
    }
    @PostMapping("/estadias/{id}/confirmar")
    @PreAuthorize("@permisosEstadia.recepcion()")
    public Registro confirmar(@PathVariable Long id, @RequestBody Salida datos, Principal usuario) {
        return checkout.confirmar(id, datos.salidaReal(), usuario.getName());
    }
    @PostMapping("/habitaciones/{id}/limpieza")
    @PreAuthorize("@permisosEstadia.limpieza()")
    public Habitacion limpiar(@PathVariable Long id, Principal usuario) { return checkout.limpiar(id, usuario.getName()); }
    @GetMapping("/notificaciones")
    @PreAuthorize("@permisosEstadia.recepcion()")
    public List<Map<String, Object>> notificaciones() { return checkout.notificaciones(); }
    @GetMapping("/estadias/{id}/movimientos")
    @PreAuthorize("@permisosEstadia.recepcion()")
    public List<MovimientoEstadia> historial(@PathVariable Long id) { return movimientos.findByRegistroIdOrderByIdAsc(id); }
    @GetMapping("/habitaciones/{id}/movimientos")
    public List<MovimientoEstadia> historialHabitacion(@PathVariable Long id) { return movimientos.findByHabitacionIdOrderByIdAsc(id); }
    @GetMapping("/resumen")
    @PreAuthorize("@permisosEstadia.recepcion()")
    public Map<String, Long> resumen() {
        checkout.revisarPendientes();
        Map<String, Long> total = new LinkedHashMap<>();
        for (String estado : List.of("LIBRE", "OCUPADA", "SALIDA PENDIENTE", "PENDIENTE DE LIMPIEZA", "MANTENIMIENTO")) total.put(estado, 0L);
        var lista = habitaciones.listar();
        for (Habitacion h : lista) {
            String estado = h.getEstadoOperativo() == null ? "SIN ESTADO" : h.getEstadoOperativo().trim().toUpperCase(Locale.ROOT);
            if ("DISPONIBLE".equals(estado)) estado = "LIBRE";
            if (Set.of("OCUPADO", "PARCIALMENTE OCUPADA").contains(estado)) estado = "OCUPADA";
            total.merge(estado, 1L, Long::sum);
        }
        total.put("TOTAL", (long) lista.size());
        return total;
    }
}
