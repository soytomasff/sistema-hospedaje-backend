package com.amauta.amauta_backend;

import com.amauta.amauta_backend.controller.ReservaController;
import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.entity.Huesped;
import com.amauta.amauta_backend.entity.Reserva;
import com.amauta.amauta_backend.entity.Usuario;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.HuespedRepository;
import com.amauta.amauta_backend.repository.UsuarioRepository;
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

import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservaFlowTest {
    @Autowired ReservaController reservas;
    @Autowired HuespedRepository huespedes;
    @Autowired HabitacionRepository habitaciones;
    @Autowired UsuarioRepository usuarios;

    @BeforeEach void autenticar() {
        if (usuarios.findByUsername("recepcion-prueba").isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setUsername("recepcion-prueba"); usuario.setRol("RECEPCIONISTA");
            usuario.setPassword("solo-prueba-temporal"); usuarios.save(usuario);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("recepcion-prueba", null,
                        List.of(new SimpleGrantedAuthority("ROLE_RECEPCIONISTA"))));
    }
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test @Transactional void validaCrucesYConservaCanceladas() {
        Huesped huesped = new Huesped();
        huesped.setNombre("Reserva"); huesped.setApellido("Prueba"); huesped.setDni("RTEST-9001");
        huesped = huespedes.save(huesped);
        Habitacion habitacion = new Habitacion();
        habitacion.setCodigo("RTEST-9001"); habitacion.setNumero("950");
        habitacion.setTipo("SIMPLE"); habitacion.setCapacidad(1); habitacion.setEstado("LIBRE");
        habitacion = habitaciones.save(habitacion);

        Reserva primera = reservas.guardar(datos(huesped, habitacion, "2027-04-10", "2027-04-12"));
        assertEquals("RESERVADO", primera.getEstado());
        ResponseStatusException cruce = assertThrows(ResponseStatusException.class,
                () -> reservas.guardar(datos(primera.getHuesped(), primera.getHabitacion(),
                        "2027-04-11", "2027-04-13")));
        assertEquals(HttpStatus.CONFLICT, cruce.getStatusCode());
        Reserva contigua = reservas.guardar(datos(huesped, habitacion, "2027-04-12", "2027-04-14"));
        assertNotNull(contigua.getId());

        Reserva cancelacion = new Reserva(); cancelacion.setEstado("CANCELADO");
        Reserva cancelada = reservas.editar(primera.getId(), cancelacion);
        assertEquals("CANCELADO", cancelada.getEstado());
        assertEquals(primera.getFechaIngreso(), cancelada.getFechaIngreso());
        assertNotNull(reservas.guardar(datos(huesped, habitacion, "2027-04-10", "2027-04-12")).getId());
    }

    private static Reserva datos(Huesped huesped, Habitacion habitacion, String ingreso, String salida) {
        Reserva reserva = new Reserva();
        reserva.setHuesped(huesped); reserva.setHabitacion(habitacion);
        reserva.setFechaIngreso(LocalDate.parse(ingreso)); reserva.setFechaSalida(LocalDate.parse(salida));
        reserva.setEstado("RESERVADO");
        return reserva;
    }
}
