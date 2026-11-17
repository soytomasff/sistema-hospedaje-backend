package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.*;
import com.amauta.amauta_backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@Service
public class CheckoutService {
    private final RegistroRepository registros;
    private final HabitacionRepository habitaciones;
    private final MovimientoEstadiaRepository movimientos;
    private final ConfiguracionEstadiaService configuracion;
    private final Clock clock;
    private final com.amauta.amauta_backend.config.PermisosEstadia permisos;
    public CheckoutService(RegistroRepository registros, HabitacionRepository habitaciones,
            MovimientoEstadiaRepository movimientos, ConfiguracionEstadiaService configuracion, Clock clock,
            com.amauta.amauta_backend.config.PermisosEstadia permisos) {
        this.registros = registros; this.habitaciones = habitaciones; this.movimientos = movimientos;
        this.configuracion = configuracion; this.clock = clock;
        this.permisos = permisos;
    }
    public LocalDateTime ahora() { return LocalDateTime.ofInstant(clock.instant(), ZoneId.of("America/Lima")); }
    public static boolean activo(Registro r) { return r.getFechaSalida() == null || r.getHoraSalida() == null; }

    public void prepararIngreso(Registro r) {
        if (r.getFechaSalida() != null || r.getHoraSalida() != null)
            throw error(HttpStatus.BAD_REQUEST, "La salida real se confirma desde check-out, no al crear la estadia");
        if (r.getSalidaPrevista() == null)
            r.setSalidaPrevista(r.getFechaIngreso().plusDays(1).atTime(configuracion.horaSalida()));
        validarPrevista(r, r.getSalidaPrevista());
        r.setEstado("EN_CURSO"); r.setAvisoSalidaEmitido(false);
    }
    private void validarPrevista(Registro r, LocalDateTime prevista) {
        if (r.getFechaIngreso() == null || r.getHoraIngreso() == null)
            throw error(HttpStatus.BAD_REQUEST, "Completa la fecha y hora de ingreso antes de programar la salida");
        if (prevista == null || prevista.isBefore(r.getFechaIngreso().atTime(r.getHoraIngreso())))
            throw error(HttpStatus.BAD_REQUEST, "La salida prevista no puede ser anterior al ingreso");
    }
    public void movimiento(Registro r, String tipo, String usuario, String detalle) {
        MovimientoEstadia m = new MovimientoEstadia();
        m.setRegistroId(r.getId()); m.setHabitacionId(r.getHabitacionId());
        m.setTipo(tipo); m.setUsuario(usuario); m.setDetalle(detalle); m.setMomento(ahora());
        movimientos.save(m);
    }
    @Transactional
    public Registro programar(Long id, LocalDateTime prevista, String usuario) {
        Registro r = bloquearRegistro(id);
        if (!activo(r)) throw error(HttpStatus.CONFLICT, "La estadia ya tiene una salida confirmada");
        validarPrevista(r, prevista);
        if (Objects.equals(r.getSalidaPrevista(), prevista)) return r;
        var anterior = r.getSalidaPrevista();
        r.setSalidaPrevista(prevista); r.setEstado("EN_CURSO");
        movimiento(r, "SALIDA_REPROGRAMADA", usuario, anterior + " -> " + prevista);
        marcarPendiente(r);
        return r;
    }
    @Transactional
    public Registro confirmar(Long id, LocalDateTime real, String usuario) {
        Registro r = bloquearRegistro(id);
        // A retried confirmation neither changes history nor adds another movement.
        if (!activo(r)) {
            if (real == null || real.equals(r.getFechaSalida().atTime(r.getHoraSalida()))) return r;
            throw error(HttpStatus.CONFLICT, "La salida ya fue confirmada con otra hora");
        }
        LocalDateTime salida = real == null ? ahora() : real;
        if (r.getFechaIngreso() == null || r.getHoraIngreso() == null)
            throw error(HttpStatus.BAD_REQUEST, "Completa la fecha y hora de ingreso antes de confirmar la salida");
        if (salida.isBefore(r.getFechaIngreso().atTime(r.getHoraIngreso())) || salida.isAfter(ahora()))
            throw error(HttpStatus.BAD_REQUEST, "La salida real debe estar entre el ingreso y la hora actual de Peru");
        Habitacion h = habitaciones.buscarParaActualizar(r.getHabitacionId()).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Habitacion no encontrada"));
        r.setFechaSalida(salida.toLocalDate()); r.setHoraSalida(salida.toLocalTime()); r.setEstado("FINALIZADA");
        registros.saveAndFlush(r);
        h.setLimpiezaPendienteDesde(ahora());
        HabitacionService.actualizarEstado(h, registros.contarActivos(h.getId(), null));
        habitaciones.save(h);
        movimiento(r, "SALIDA_CONFIRMADA", usuario, "Salida real: " + salida);
        return r;
    }
    @Transactional
    public Habitacion limpiar(Long habitacionId, String usuario) {
        Habitacion h = habitaciones.buscarParaActualizar(habitacionId).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Habitacion no encontrada"));
        if (registros.contarActivos(habitacionId, null) > 0)
            throw error(HttpStatus.CONFLICT, "Aun hay huespedes activos; no se puede liberar la habitacion");
        if (h.getLimpiezaPendienteDesde() == null) {
            if ("LIBRE".equalsIgnoreCase(h.getEstado())) return h;
            throw error(HttpStatus.CONFLICT, "La habitacion no tiene limpieza pendiente");
        }
        if ("MANTENIMIENTO".equalsIgnoreCase(h.getEstado())) throw error(HttpStatus.CONFLICT, "La habitacion esta en mantenimiento");
        h.setLimpiezaPendienteDesde(null); h.setEstado("LIBRE");
        MovimientoEstadia m = new MovimientoEstadia(); m.setHabitacionId(habitacionId);
        m.setTipo("LIMPIEZA_CONFIRMADA"); m.setMomento(ahora()); m.setUsuario(usuario);
        m.setDetalle("Habitacion limpia y disponible"); movimientos.save(m);
        return h;
    }
    @Transactional
    public void revisarPendientes() {
        // Legacy date-only departures were unconfirmed plans. Preserve those fields
        // and recover only the explicitly recorded date; never guess a missing date.
        for (Long id : registros.idsSinSalidaPrevista()) {
            Registro r = bloquearRegistro(id);
            if (!activo(r) || r.getSalidaPrevista() != null) continue;
            if (r.getFechaSalida() == null || r.getFechaIngreso() == null || r.getHoraIngreso() == null) continue;
            var ingreso = r.getFechaIngreso().atTime(r.getHoraIngreso());
            var prevista = r.getFechaSalida().atTime(configuracion.horaSalida());
            if (prevista.isBefore(ingreso)) continue;
            String anterior = "Fecha parcial: " + r.getFechaSalida() + "; hora parcial: " + r.getHoraSalida();
            r.setSalidaPrevista(prevista);
            r.setEstado("EN_CURSO");
            movimiento(r, "PREVISION_INICIALIZADA", "SISTEMA", anterior + "; prevista: " + prevista);
        }
        registros.flush();
        for (Long id : registros.idsSalidasVencidas(ahora())) {
            Registro r = bloquearRegistro(id);
            marcarPendiente(r);
        }
    }
    private void marcarPendiente(Registro r) {
        if (activo(r) && r.getSalidaPrevista() != null && !r.getSalidaPrevista().isAfter(ahora())) {
            r.setEstado("SALIDA_PENDIENTE");
            if (!Boolean.TRUE.equals(r.getAvisoSalidaEmitido())) {
                r.setAvisoSalidaEmitido(true);
                movimiento(r, "SALIDA_PENDIENTE", "SISTEMA", "Salida prevista: " + r.getSalidaPrevista());
            }
        }
    }
    @Transactional
    public List<Map<String, Object>> notificaciones() {
        revisarPendientes();
        LocalDateTime actual = ahora();
        return registros.salidasParaAviso(actual.plusHours(1)).stream().map(r -> {
            Map<String, Object> aviso = new LinkedHashMap<>();
            aviso.put("id", r.getId()); aviso.put("registroId", r.getId()); aviso.put("habitacionId", r.getHabitacionId());
            aviso.put("habitacion", r.getNumeroCuarto() == null ? "#" + r.getHabitacionId() : r.getNumeroCuarto());
            aviso.put("huesped", r.getNombreHuesped() == null ? "Huesped #" + r.getHuespedId() : r.getNombreHuesped());
            boolean vencida = !r.getSalidaPrevista().isAfter(actual);
            aviso.put("salidaPrevista", r.getSalidaPrevista());
            aviso.put("tipo", vencida ? "VENCIDA" : "PROXIMA");
            aviso.put("mensaje", vencida ? "Salida pendiente" : "Salida proxima");
            return aviso;
        }).toList();
    }
    private Registro bloquearRegistro(Long id) {
        return registros.buscarParaActualizar(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Estadia no encontrada"));
    }
    private ResponseStatusException error(HttpStatus status, String message) { return new ResponseStatusException(status, message); }
}
