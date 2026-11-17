package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.controller.RecepcionController.Entrada;
import com.amauta.amauta_backend.entity.Huesped;
import com.amauta.amauta_backend.entity.Registro;
import com.amauta.amauta_backend.entity.Reserva;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class RecepcionService {
    private final EntityManager entidades;
    private final RegistroService registros;
    private final CheckoutService checkout;
    private final ConfiguracionEstadiaService configuracion;

    public RecepcionService(EntityManager entidades, RegistroService registros,
                            CheckoutService checkout, ConfiguracionEstadiaService configuracion) {
        this.entidades = entidades;
        this.registros = registros;
        this.checkout = checkout;
        this.configuracion = configuracion;
    }

    @Transactional
    public Registro registrar(Entrada entrada) {
        if (entrada == null || entrada.huespedId() == null || entrada.habitacionId() == null
                || entrada.fechaIngreso() == null || entrada.horaIngreso() == null)
            throw error(HttpStatus.BAD_REQUEST, "Selecciona huesped, habitacion, fecha y hora de ingreso");
        LocalDateTime ingreso = entrada.fechaIngreso().atTime(entrada.horaIngreso());
        if (ingreso.isAfter(checkout.ahora()))
            throw error(HttpStatus.BAD_REQUEST, "El ingreso no puede ser futuro");

        Huesped huesped = entidades.find(Huesped.class, entrada.huespedId(), LockModeType.PESSIMISTIC_WRITE);
        if (huesped == null) throw error(HttpStatus.NOT_FOUND, "Huesped no encontrado");
        Reserva reserva = null;
        if (entrada.reservaId() != null) {
            reserva = entidades.find(Reserva.class, entrada.reservaId(), LockModeType.PESSIMISTIC_WRITE);
            if (reserva == null) throw error(HttpStatus.NOT_FOUND, "Reserva no encontrada");
            String estado = reserva.getEstado() == null ? ""
                    : reserva.getEstado().trim().toUpperCase(Locale.ROOT);
            if (!Set.of("RESERVADO", "CONFIRMADO").contains(estado))
                throw error(HttpStatus.CONFLICT, "La reserva no admite check-in");
            if (reserva.getHuesped() == null || reserva.getHabitacion() == null
                    || !Objects.equals(reserva.getHuesped().getId(), entrada.huespedId())
                    || !Objects.equals(reserva.getHabitacion().getId(), entrada.habitacionId()))
                throw error(HttpStatus.CONFLICT, "El huesped o la habitacion no coincide con la reserva");
            if (reserva.getFechaIngreso() == null || reserva.getFechaSalida() == null
                    || entrada.fechaIngreso().isBefore(reserva.getFechaIngreso())
                    || !entrada.fechaIngreso().isBefore(reserva.getFechaSalida()))
                throw error(HttpStatus.CONFLICT, "La fecha no corresponde a la reserva");
        } else {
            Long pendientes = entidades.createQuery("""
                    select count(r) from Reserva r where r.huesped.id = :huesped
                    and upper(coalesce(r.estado, '')) in ('RESERVADO', 'CONFIRMADO')
                    and r.fechaIngreso <= :fecha and r.fechaSalida > :fecha
                    """, Long.class).setParameter("huesped", entrada.huespedId())
                    .setParameter("fecha", entrada.fechaIngreso()).getSingleResult();
            if (pendientes > 0)
                throw error(HttpStatus.CONFLICT, "Selecciona la reserva vigente de este huesped");
        }

        Long activos = entidades.createQuery("""
                select count(r) from Registro r where r.huespedId = :huesped
                and (r.fechaSalida is null or r.horaSalida is null)
                """, Long.class).setParameter("huesped", entrada.huespedId()).getSingleResult();
        if (activos > 0) throw error(HttpStatus.CONFLICT, "El huesped ya tiene una estadia activa");

        Registro registro = new Registro();
        registro.setHuespedId(entrada.huespedId());
        registro.setHabitacionId(entrada.habitacionId());
        registro.setFechaIngreso(entrada.fechaIngreso());
        registro.setHoraIngreso(entrada.horaIngreso());
        if (entrada.salidaPrevista() != null)
            registro.setSalidaPrevista(entrada.salidaPrevista());
        else if (reserva != null)
            registro.setSalidaPrevista(reserva.getFechaSalida().atTime(configuracion.horaSalida()));
        Registro guardado = registros.guardar(registro);
        if (reserva != null) reserva.setEstado("INGRESADO");
        entidades.flush();
        return guardado;
    }

    private static ResponseStatusException error(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
