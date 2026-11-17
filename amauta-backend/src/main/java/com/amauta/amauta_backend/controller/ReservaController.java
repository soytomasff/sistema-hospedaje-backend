package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.entity.Huesped;
import com.amauta.amauta_backend.entity.Reserva;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.HuespedRepository;
import com.amauta.amauta_backend.repository.RegistroRepository;
import com.amauta.amauta_backend.repository.ReservaRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/reservas")
public class ReservaController {
    private final ReservaRepository reservas;
    private final HabitacionRepository habitaciones;
    private final HuespedRepository huespedes;
    private final RegistroRepository registros;

    public ReservaController(ReservaRepository reservas, HabitacionRepository habitaciones,
                             HuespedRepository huespedes, RegistroRepository registros) {
        this.reservas = reservas;
        this.habitaciones = habitaciones;
        this.huespedes = huespedes;
        this.registros = registros;
    }

    @GetMapping
    public List<Reserva> listar() { return reservas.findAll(); }

    @GetMapping("/{id}")
    public Reserva obtener(@PathVariable Long id) {
        return reservas.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    }

    @PostMapping
    @Transactional
    @PreAuthorize("@permisosEstadia.recepcion()")
    public Reserva guardar(@RequestBody Reserva datos) {
        Reserva reserva = new Reserva();
        actualizarCampos(reserva, datos, -1L, true);
        reserva.setFechaReserva(LocalDateTime.now());
        return reservas.save(reserva);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("@permisosEstadia.recepcion()")
    public Reserva editar(@PathVariable Long id, @RequestBody Reserva datos) {
        Reserva reserva = reservas.buscarParaActualizar(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
        String actual = normalizar(reserva.getEstado());
        if (actual.equals("CANCELADO") || actual.equals("INGRESADO"))
            throw error(HttpStatus.CONFLICT, "La reserva ya no admite cambios");
        if (normalizar(datos.getEstado()).equals("CANCELADO")) {
            reserva.setEstado("CANCELADO");
            return reservas.save(reserva);
        }
        actualizarCampos(reserva, datos, id, false);
        return reservas.save(reserva);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("@permisosEstadia.recepcion()")
    public void eliminar(@PathVariable Long id) {
        Reserva reserva = reservas.buscarParaActualizar(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
        if (normalizar(reserva.getEstado()).equals("INGRESADO"))
            throw error(HttpStatus.CONFLICT, "La reserva ya tiene un ingreso registrado");
        reservas.delete(reserva);
    }

    private void actualizarCampos(Reserva reserva, Reserva datos, Long excluirId, boolean nueva) {
        if (datos.getHuesped() == null || datos.getHuesped().getId() == null)
            throw error(HttpStatus.BAD_REQUEST, "Selecciona un huesped");
        if (datos.getHabitacion() == null || datos.getHabitacion().getId() == null)
            throw error(HttpStatus.BAD_REQUEST, "Selecciona una habitacion");
        if (datos.getFechaIngreso() == null || datos.getFechaSalida() == null
                || !datos.getFechaSalida().isAfter(datos.getFechaIngreso()))
            throw error(HttpStatus.BAD_REQUEST, "La salida debe ser posterior al ingreso");
        String estado = normalizar(datos.getEstado());
        if (estado.isEmpty()) estado = nueva ? "RESERVADO" : normalizar(reserva.getEstado());
        if (!Set.of("RESERVADO", "CONFIRMADO").contains(estado))
            throw error(HttpStatus.BAD_REQUEST, "Estado de reserva invalido");

        Huesped huesped = huespedes.findById(datos.getHuesped().getId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Huesped no encontrado"));
        Habitacion habitacion = habitaciones.buscarParaActualizar(datos.getHabitacion().getId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Habitacion no encontrada"));

        boolean cambiaIntervalo = nueva || reserva.getHabitacion() == null
                || !Objects.equals(reserva.getHabitacion().getId(), habitacion.getId())
                || !Objects.equals(reserva.getFechaIngreso(), datos.getFechaIngreso())
                || !Objects.equals(reserva.getFechaSalida(), datos.getFechaSalida());
        if (cambiaIntervalo) {
            String estadoHabitacion = normalizar(habitacion.getEstado());
            boolean libre = Set.of("LIBRE", "DISPONIBLE").contains(estadoHabitacion)
                    && habitacion.getLimpiezaPendienteDesde() == null
                    && registros.contarActivos(habitacion.getId(), null) == 0;
            if (!libre) throw error(HttpStatus.CONFLICT, "Habitacion no disponible");
        }
        if (reservas.contarCruces(habitacion.getId(), datos.getFechaIngreso(),
                datos.getFechaSalida(), excluirId) > 0)
            throw error(HttpStatus.CONFLICT, "La habitacion ya tiene una reserva en esas fechas");

        reserva.setHuesped(huesped);
        reserva.setHabitacion(habitacion);
        reserva.setFechaIngreso(datos.getFechaIngreso());
        reserva.setFechaSalida(datos.getFechaSalida());
        reserva.setEstado(estado);
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
    }

    private static ResponseStatusException error(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
