package com.amauta.amauta_backend;

import com.amauta.amauta_backend.controller.RecepcionController.Entrada;
import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.entity.Huesped;
import com.amauta.amauta_backend.entity.Registro;
import com.amauta.amauta_backend.entity.Reserva;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.HuespedRepository;
import com.amauta.amauta_backend.repository.ReservaRepository;
import com.amauta.amauta_backend.service.CheckoutService;
import com.amauta.amauta_backend.service.RecepcionService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecepcionFlowTest {
    @Autowired RecepcionService recepcion;
    @Autowired CheckoutService checkout;
    @Autowired HuespedRepository huespedes;
    @Autowired HabitacionRepository habitaciones;
    @Autowired ReservaRepository reservas;

    @BeforeEach void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("recepcion-prueba", null,
                        List.of(new SimpleGrantedAuthority("ROLE_RECEPCIONISTA"))));
    }
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test @Transactional void ingresoDirectoRechazaDuplicado() {
        Huesped guest = guest("RCPT-1"); Habitacion room = room("951");
        LocalDateTime now = checkout.ahora().minusMinutes(1);
        Entrada entry = new Entrada(guest.getId(), room.getId(), now.toLocalDate(), now.toLocalTime(), null);
        Registro stay = recepcion.registrar(entry);
        assertNotNull(stay.getId());
        assertEquals("EN_CURSO", stay.getEstado());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> recepcion.registrar(entry));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test @Transactional void recepcionGuardaHoraPrevistaElegidaSinConfirmarSalida() {
        Huesped guest = guest("RCPT-PREVISTA"); Habitacion room = room("955");
        LocalDateTime now = checkout.ahora().minusMinutes(1);
        LocalDateTime planned = now.plusDays(2).withHour(14).withMinute(30).withSecond(0);
        Registro stay = recepcion.registrar(new Entrada(guest.getId(), room.getId(),
                now.toLocalDate(), now.toLocalTime(), null, planned));
        assertEquals(planned, stay.getSalidaPrevista());
        assertNull(stay.getFechaSalida());
        assertNull(stay.getHoraSalida());
        assertEquals("OCUPADO", room.getEstado());
    }

    @Test @Transactional void reservaSeConsumeYNoAdmiteSegundoIngreso() {
        Huesped guest = guest("RCPT-2"); Habitacion room = room("952");
        LocalDateTime now = checkout.ahora().minusMinutes(1);
        Reserva reservation = new Reserva();
        reservation.setHuesped(guest); reservation.setHabitacion(room);
        reservation.setFechaIngreso(now.toLocalDate());
        reservation.setFechaSalida(now.toLocalDate().plusDays(1));
        reservation.setEstado("RESERVADO");
        reservation = reservas.save(reservation);
        Entrada entry = new Entrada(guest.getId(), room.getId(), now.toLocalDate(),
                now.toLocalTime(), reservation.getId());
        Registro stay = recepcion.registrar(entry);
        assertEquals("INGRESADO", reservation.getEstado());
        assertEquals(reservation.getFechaSalida(), stay.getSalidaPrevista().toLocalDate());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> recepcion.registrar(entry));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test @Transactional void habitacionOcupadaRechazaOtroHuesped() {
        Huesped first = guest("RCPT-3"); Huesped second = guest("RCPT-4");
        Habitacion room = room("953");
        LocalDateTime now = checkout.ahora().minusMinutes(1);
        recepcion.registrar(new Entrada(first.getId(), room.getId(), now.toLocalDate(), now.toLocalTime(), null));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> recepcion.registrar(new Entrada(second.getId(), room.getId(),
                        now.toLocalDate(), now.toLocalTime(), null)));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test @Transactional void checkoutMarcaLimpiezaPendiente() {
        Huesped guest = guest("RCPT-5"); Habitacion room = room("954");
        LocalDateTime now = checkout.ahora().minusMinutes(1);
        Registro stay = recepcion.registrar(new Entrada(guest.getId(), room.getId(),
                now.toLocalDate(), now.toLocalTime(), null));
        Registro closed = checkout.confirmar(stay.getId(), null, "recepcion-prueba");
        assertEquals("FINALIZADA", closed.getEstado());
        assertNotNull(room.getLimpiezaPendienteDesde());
        assertNotEquals("LIBRE", room.getEstado());
    }

    private Huesped guest(String document) {
        Huesped guest = new Huesped();
        guest.setNombre("Recepcion"); guest.setApellido("Prueba"); guest.setDni(document);
        return huespedes.save(guest);
    }
    private Habitacion room(String number) {
        Habitacion room = new Habitacion();
        room.setCodigo("RCPT-" + number); room.setNumero(number);
        room.setTipo("SIMPLE"); room.setCapacidad(1); room.setEstado("LIBRE");
        room.setPrecio(new BigDecimal("100.00"));
        return habitaciones.save(room);
    }
}
