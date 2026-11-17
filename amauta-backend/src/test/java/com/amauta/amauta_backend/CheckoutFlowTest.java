package com.amauta.amauta_backend;

import com.amauta.amauta_backend.entity.*;
import com.amauta.amauta_backend.repository.*;
import com.amauta.amauta_backend.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import java.time.*;
import java.math.BigDecimal;
import java.util.List;
import java.net.*;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:checkout;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "app.checkout.revision-ms=86400000"
})
@Import(CheckoutFlowTest.TimeConfig.class)
class CheckoutFlowTest {
    @TestConfiguration static class TimeConfig {
        @Bean @Primary MutableClock testClock() { return new MutableClock(); }
    }
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-09-15T14:00:00Z");
        void at(String local) { now = LocalDateTime.parse(local).atZone(ZoneId.of("America/Lima")).toInstant(); }
        public ZoneId getZone() { return ZoneId.of("America/Lima"); }
        public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        public Instant instant() { return now; }
    }
    @Autowired MutableClock clock;
    @Autowired CheckoutService checkout;
    @Autowired RegistroService estadias;
    @Autowired HabitacionService cuartos;
    @Autowired RegistroRepository registros;
    @Autowired HabitacionRepository habitaciones;
    @Autowired HuespedRepository huespedes;
    @Autowired ReservaRepository reservas;
    @Autowired MovimientoEstadiaRepository movimientos;
    @Autowired ConfiguracionEstadiaRepository configRepo;
    @Autowired ConfiguracionEstadiaService config;
    @Autowired UsuarioRepository usuarios;
    @LocalServerPort int port;
    final JsonMapper json = JsonMapper.builder().build();
    Habitacion habitacion;
    Huesped huesped;

    @BeforeEach void preparar() {
        movimientos.deleteAll(); reservas.deleteAll(); registros.deleteAll(); habitaciones.deleteAll(); huespedes.deleteAll(); configRepo.deleteAll(); usuarios.deleteAll();
        clock.at("2026-09-15T09:00:00"); rol("ADMINISTRADOR");
        huesped = new Huesped(); huesped.setNombre("Ana"); huesped.setApellido("Prueba"); huesped.setDni("12345678");
        huesped = huespedes.save(huesped);
        habitacion = new Habitacion(); habitacion.setCodigo("CHECKOUT"); habitacion.setNumero("201");
        habitacion.setPrecio(BigDecimal.valueOf(60)); habitacion.setCapacidad(1); habitacion.setEstado("LIBRE");
        habitacion = habitaciones.save(habitacion);
    }
    @AfterEach void limpiarContexto() { SecurityContextHolder.clearContext(); }
    void rol(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("prueba", null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
    Registro ingreso() {
        Registro r = new Registro(); r.setHuespedId(huesped.getId()); r.setHabitacionId(habitacion.getId());
        r.setFechaIngreso(LocalDate.of(2026, 9, 14)); r.setHoraIngreso(LocalTime.NOON);
        return estadias.guardar(r);
    }
    String estado() { return habitaciones.findById(habitacion.getId()).orElseThrow().getEstado(); }

    @ParameterizedTest @ValueSource(strings = {"09:00", "11:00", "13:00"})
    void salidaTempranaPuntualYTardiaExigeLimpieza(String hora) {
        Reserva reserva = new Reserva(); reserva.setHabitacion(habitacion); reserva.setHuesped(huesped);
        reserva.setFechaIngreso(LocalDate.of(2026, 9, 14)); reserva.setFechaSalida(LocalDate.of(2026, 9, 15));
        reserva.setEstado("CONFIRMADA"); reservas.save(reserva);
        Registro r = ingreso();
        assertEquals(LocalDateTime.parse("2026-09-15T11:00:00"), r.getSalidaPrevista());
        clock.at("2026-09-15T" + hora + ":00");
        checkout.revisarPendientes();
        assertEquals(hora.equals("09:00") ? 0 : 1, checkout.notificaciones().size());
        assertEquals("OCUPADO", estado());
        var real = checkout.ahora();
        checkout.confirmar(r.getId(), real, "recepcion");
        assertEquals("PENDIENTE DE LIMPIEZA", estado());
        assertTrue(checkout.notificaciones().isEmpty());
        assertEquals(1, registros.count()); assertEquals(1, huespedes.count());
        assertEquals(1, reservas.count());
        assertEquals(real.toLocalTime(), registros.findById(r.getId()).orElseThrow().getHoraSalida());
        assertThrows(ResponseStatusException.class, this::ingreso);
        long cantidad = movimientos.count();
        checkout.confirmar(r.getId(), real, "recepcion");
        assertEquals(cantidad, movimientos.count());
        checkout.limpiar(habitacion.getId(), "recepcion");
        assertEquals("LIBRE", estado());
        long limpio = movimientos.count(); checkout.limpiar(habitacion.getId(), "recepcion");
        assertEquals(limpio, movimientos.count());
        assertTrue(movimientos.findByRegistroIdOrderByIdAsc(r.getId()).stream().anyMatch(m -> m.getTipo().equals("SALIDA_CONFIRMADA")));
    }
    @Test void salidaConservaReservaYHuesped() {
        Reserva reserva = new Reserva(); reserva.setHabitacion(habitacion); reserva.setHuesped(huesped);
        reserva.setFechaIngreso(LocalDate.of(2026, 9, 14)); reserva.setFechaSalida(LocalDate.of(2026, 9, 15));
        reserva.setEstado("CONFIRMADA"); reserva = reservas.save(reserva);
        Registro r = ingreso(); checkout.confirmar(r.getId(), checkout.ahora(), "recepcion");
        checkout.limpiar(habitacion.getId(), "recepcion");
        assertEquals("CONFIRMADA", reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(1, reservas.count()); assertEquals(1, huespedes.count()); assertEquals(1, registros.count());
    }

    @Test void revisionesConcurrentesNoDuplicanAvisos() throws Exception {
        Registro r = ingreso(); clock.at("2026-09-15T11:00:00");
        try (var workers = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var primero = workers.submit(() -> checkout.revisarPendientes());
            var segundo = workers.submit(() -> checkout.revisarPendientes());
            primero.get(10, java.util.concurrent.TimeUnit.SECONDS); segundo.get(10, java.util.concurrent.TimeUnit.SECONDS);
        }
        assertEquals(1, checkout.notificaciones().size());
        assertEquals(1, movimientos.findByRegistroIdOrderByIdAsc(r.getId()).stream()
                .filter(m -> m.getTipo().equals("SALIDA_PENDIENTE")).count());
    }

    @Test void recuperaEstadiaAnteriorAlDespliegueSinBorrarHistoria() {
        Registro antigua = new Registro(); antigua.setHuespedId(huesped.getId()); antigua.setHabitacionId(habitacion.getId());
        antigua.setFechaIngreso(LocalDate.of(2026, 9, 14)); antigua.setHoraIngreso(LocalTime.NOON);
        antigua.setFechaSalida(LocalDate.of(2026, 9, 15)); // Legacy planned date without confirmed time.
        antigua.setNumeroCuarto("201"); antigua.setNombreHuesped("Ana Prueba");
        antigua = registros.save(antigua);
        habitacion.setEstado("OCUPADO"); habitaciones.save(habitacion);
        clock.at("2026-09-17T15:00:00"); checkout.revisarPendientes(); checkout.revisarPendientes();
        Registro recuperada = registros.findById(antigua.getId()).orElseThrow();
        assertEquals(LocalDateTime.parse("2026-09-15T11:00:00"), recuperada.getSalidaPrevista());
        assertEquals(LocalDate.of(2026, 9, 15), recuperada.getFechaSalida()); assertNull(recuperada.getHoraSalida());
        assertEquals("SALIDA_PENDIENTE", recuperada.getEstado()); assertEquals("OCUPADO", estado());
        assertEquals(1, checkout.notificaciones().size());
        assertEquals(1, movimientos.findByRegistroIdOrderByIdAsc(antigua.getId()).stream()
                .filter(m -> m.getTipo().equals("PREVISION_INICIALIZADA")).count());
        assertEquals(1, huespedes.count()); assertEquals(1, registros.count());
    }

    @Test void noInventaFechaParaEstadiaAnteriorSinPrevision() {
        Registro antigua = new Registro(); antigua.setHuespedId(huesped.getId()); antigua.setHabitacionId(habitacion.getId());
        antigua.setFechaIngreso(LocalDate.of(2026,9,14)); antigua.setHoraIngreso(LocalTime.NOON);
        antigua = registros.save(antigua); habitacion.setEstado("OCUPADO"); habitaciones.save(habitacion);
        clock.at("2026-09-17T15:00:00"); checkout.revisarPendientes();
        assertNull(registros.findById(antigua.getId()).orElseThrow().getSalidaPrevista());
        assertTrue(checkout.notificaciones().isEmpty()); assertEquals("OCUPADO", estado());
        checkout.programar(antigua.getId(), LocalDateTime.parse("2026-09-16T11:00:00"), "admin");
        assertEquals(1, checkout.notificaciones().size());
    }

    @Test void recuperaSalidaDuranteApagadoSinDuplicarAvisos() {
        Registro r = ingreso();
        clock.at("2026-09-17T15:00:00"); // No scheduler ran while the server was off.
        checkout.revisarPendientes(); checkout.revisarPendientes();
        assertEquals(1, checkout.notificaciones().size());
        assertEquals("SALIDA_PENDIENTE", registros.findById(r.getId()).orElseThrow().getEstado());
        assertEquals(1, movimientos.findByRegistroIdOrderByIdAsc(r.getId()).stream().filter(m -> m.getTipo().equals("SALIDA_PENDIENTE")).count());
        assertEquals("OCUPADO", estado());
        assertEquals("SALIDA PENDIENTE", cuartos.listar().getFirst().getEstadoOperativo());
    }
    @Test void administradorAutorizaSalidaTempranaYTardiaYConservaAvisoUnico() {
        Registro r = ingreso();
        checkout.programar(r.getId(), LocalDateTime.parse("2026-09-15T09:30:00"), "admin");
        clock.at("2026-09-15T09:30:00"); assertEquals(1, checkout.notificaciones().size());
        checkout.programar(r.getId(), LocalDateTime.parse("2026-09-15T14:00:00"), "admin");
        assertEquals(0, checkout.notificaciones().size());
        clock.at("2026-09-15T14:00:00"); assertEquals(1, checkout.notificaciones().size());
        assertEquals(1, movimientos.findByRegistroIdOrderByIdAsc(r.getId()).stream().filter(m -> m.getTipo().equals("SALIDA_PENDIENTE")).count());
    }
    @Test void horaGlobalAfectaSoloIngresosNuevosYUsaPeru() {
        config.cambiar(LocalTime.of(10, 30));
        Registro r = ingreso(); assertEquals(LocalTime.of(10, 30), r.getSalidaPrevista().toLocalTime());
        config.cambiar(LocalTime.of(12, 0));
        assertEquals(LocalTime.of(10, 30), registros.findById(r.getId()).orElseThrow().getSalidaPrevista().toLocalTime());
        assertEquals(LocalTime.of(9, 0), checkout.ahora().toLocalTime());
    }
    @Test void noSePuedeBorrarHistorialNiSaltarSalidaYLimpiezaConPut() {
        Registro r = ingreso();
        assertThrows(ResponseStatusException.class, () -> estadias.eliminar(r.getId()));
        r.setFechaSalida(LocalDate.of(2026, 9, 15)); r.setHoraSalida(LocalTime.of(9, 0));
        assertThrows(ResponseStatusException.class, () -> estadias.actualizar(r.getId(), r));
        checkout.confirmar(r.getId(), checkout.ahora(), "recepcion");
        Habitacion datos = habitaciones.findById(habitacion.getId()).orElseThrow(); datos.setEstado("LIBRE");
        assertThrows(ResponseStatusException.class, () -> cuartos.actualizar(habitacion.getId(), datos));
        assertEquals("PENDIENTE DE LIMPIEZA", estado());
    }
    @Test void compartidaNoSeLiberaConOtrosHuespedesYBloqueaCamasSucias() {
        habitacion.setNumero("301"); habitacion.setCapacidad(3); habitaciones.save(habitacion);
        Registro primero = ingreso(); Registro segundo = ingreso();
        checkout.confirmar(primero.getId(), checkout.ahora(), "recepcion");
        assertEquals("PARCIALMENTE OCUPADA", estado());
        assertThrows(ResponseStatusException.class, () -> checkout.limpiar(habitacion.getId(), "recepcion"));
        assertEquals(0L, cuartos.listar().getFirst().getCamasDisponibles());
        assertThrows(ResponseStatusException.class, this::ingreso);
        checkout.confirmar(segundo.getId(), checkout.ahora(), "recepcion");
        assertEquals("PENDIENTE DE LIMPIEZA", estado());
        checkout.limpiar(habitacion.getId(), "recepcion"); assertEquals("LIBRE", estado());
    }
    @Test void rechazaSalidaRealFuturaYAnteriorAlIngreso() {
        Registro r = ingreso();
        assertThrows(ResponseStatusException.class, () -> checkout.confirmar(r.getId(), checkout.ahora().plusMinutes(1), "recepcion"));
        assertThrows(ResponseStatusException.class, () -> checkout.confirmar(r.getId(), LocalDateTime.parse("2026-09-13T12:00:00"), "recepcion"));
        assertEquals("OCUPADO", estado());
    }

    @Test void confirmacionesConcurrentesNoDuplicanSalidaNiAviso() throws Exception {
        Registro r = ingreso(); clock.at("2026-09-15T11:00:00");
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var tasks = List.<java.util.concurrent.Callable<Void>>of(
                () -> { checkout.revisarPendientes(); return null; },
                () -> { checkout.revisarPendientes(); return null; });
            for (var result : executor.invokeAll(tasks)) result.get(10, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(1, movimientos.findByRegistroIdOrderByIdAsc(r.getId()).stream().filter(m -> m.getTipo().equals("SALIDA_PENDIENTE")).count());
            var real = checkout.ahora();
            var exits = List.<java.util.concurrent.Callable<Void>>of(
                () -> { checkout.confirmar(r.getId(), real, "recepcion-1"); return null; },
                () -> { checkout.confirmar(r.getId(), real, "recepcion-2"); return null; });
            for (var result : executor.invokeAll(exits)) result.get(10, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(1, movimientos.findByRegistroIdOrderByIdAsc(r.getId()).stream().filter(m -> m.getTipo().equals("SALIDA_CONFIRMADA")).count());
            assertEquals("PENDIENTE DE LIMPIEZA", estado());
        }
    }

    @Test void recepcionPuedeProgramarHorarioAlCrear() {
        rol("recepcionista");
        Registro r = new Registro(); r.setHuespedId(huesped.getId()); r.setHabitacionId(habitacion.getId());
        r.setFechaIngreso(LocalDate.of(2026,9,14)); r.setHoraIngreso(LocalTime.NOON);
        r.setSalidaPrevista(LocalDateTime.parse("2026-09-15T14:00:00"));
        Registro creado = estadias.guardar(r);
        assertEquals(LocalDateTime.parse("2026-09-15T14:00:00"), creado.getSalidaPrevista());
        assertNull(creado.getHoraSalida());
    }

    HttpResponse<String> call(HttpClient client, String method, String path, String body, String token) throws Exception {
        var req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).header("Content-Type", "application/json");
        if (token != null) req.header("X-XSRF-TOKEN", token);
        req.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void permisosHttpYCsrfProtegenConfiguracionYSalidas() throws Exception {
        Registro r = ingreso();
        for (String role : List.of("RECEPCIONISTA", "ADMINISTRADOR", "LIMPIEZA")) {
            Usuario u = new Usuario(); u.setUsername(role); u.setPassword("test-only-password"); u.setRol(role); usuarios.save(u);
            var client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
            String token = json.readTree(call(client, "GET", "/api/usuarios/csrf", null, null).body()).get("token").asString();
            assertEquals(200, call(client, "POST", "/api/usuarios/login", "{\"username\":\"" + role + "\",\"password\":\"test-only-password\"}", token).statusCode());
            assertEquals(role.equals("ADMINISTRADOR") ? 200 : 403,
                call(client, "PUT", "/api/check-out/configuracion", "{\"horaSalida\":\"10:00\"}", token).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "GET", "/api/check-out/notificaciones", null, null).statusCode());
            assertEquals(role.equals("RECEPCIONISTA") ? 200 : role.equals("ADMINISTRADOR") ? 409 : 403,
                call(client, "PUT", "/api/check-out/estadias/" + r.getId() + "/salida-prevista", "{\"salidaPrevista\":\"2026-09-15T12:00:00\"}", token).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "GET", "/api/habitaciones", null, null).statusCode());
            String roomBody = json.writeValueAsString(habitaciones.findById(habitacion.getId()).orElseThrow());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "PUT", "/api/habitaciones/" + habitacion.getId(), roomBody, token).statusCode());
            if (role.equals("RECEPCIONISTA"))
                assertEquals(403, call(client, "POST", "/api/habitaciones", "{}", token).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "GET", "/api/registros", null, null).statusCode());
            assertEquals(role.equals("ADMINISTRADOR") ? 200 : 403,
                call(client, "GET", "/api/usuarios", null, null).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "POST", "/api/check-out/estadias/" + r.getId() + "/confirmar", "{}", token).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "GET", "/api/check-out/habitaciones/" + habitacion.getId() + "/movimientos", null, null).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "GET", "/api/productos", null, null).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "GET", "/api/ventas-productos", null, null).statusCode());
            assertEquals(role.equals("LIMPIEZA") ? 403 : 200,
                call(client, "GET", "/api/boletas", null, null).statusCode());
            assertEquals(403, call(client, "POST", "/api/check-out/estadias/" + r.getId() + "/confirmar", "{}", null).statusCode());
        }
    }

    @Test void avisoProximoNoConfirmaSalida() {
        Registro r = ingreso();
        clock.at("2026-09-15T10:30:00");
        var avisos = checkout.notificaciones();
        assertEquals(1, avisos.size());
        assertEquals("PROXIMA", avisos.getFirst().get("tipo"));
        assertNull(registros.findById(r.getId()).orElseThrow().getHoraSalida());
        assertEquals("OCUPADO", estado());
        clock.at("2026-09-15T11:00:00");
        assertEquals("VENCIDA", checkout.notificaciones().getFirst().get("tipo"));
        assertEquals("OCUPADO", estado());
    }

    @Test void cambioDeRolSeAplicaASesionExistenteYNoPermiteAutoEscalada() throws Exception {
        Usuario admin = new Usuario(); admin.setUsername("admin-test");
        admin.setPassword("password-test-only"); admin.setRol("ADMINISTRADOR"); usuarios.save(admin);
        Usuario staff = new Usuario(); staff.setUsername("staff-test");
        staff.setPassword("password-test-only"); staff.setRol("RECEPCIONISTA"); staff = usuarios.save(staff);
        var adminClient = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        var staffClient = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        String adminToken = json.readTree(call(adminClient, "GET", "/api/usuarios/csrf", null, null).body()).get("token").asString();
        String staffToken = json.readTree(call(staffClient, "GET", "/api/usuarios/csrf", null, null).body()).get("token").asString();
        assertEquals(200, call(adminClient, "POST", "/api/usuarios/login",
                "{\"username\":\"admin-test\",\"password\":\"password-test-only\"}", adminToken).statusCode());
        assertEquals(200, call(staffClient, "POST", "/api/usuarios/login",
                "{\"username\":\"staff-test\",\"password\":\"password-test-only\"}", staffToken).statusCode());
        assertEquals(200, call(staffClient, "GET", "/api/registros", null, null).statusCode());
        assertEquals(403, call(staffClient, "PUT", "/api/usuarios/" + staff.getId() + "/rol",
                "{\"rol\":\"ADMINISTRADOR\"}", staffToken).statusCode());
        assertEquals(200, call(adminClient, "PUT", "/api/usuarios/" + staff.getId() + "/rol",
                "{\"rol\":\"ADMINISTRADOR\"}", adminToken).statusCode());
        assertEquals(200, call(staffClient, "GET", "/api/registros", null, null).statusCode());
        assertEquals(200, call(staffClient, "GET", "/api/habitaciones", null, null).statusCode());
        assertEquals(200, call(staffClient, "GET", "/api/usuarios", null, null).statusCode());
        assertEquals(200, call(adminClient, "POST", "/api/usuarios",
                "{\"username\":\"new-cleaner\",\"password\":\"test-password-123\",\"rol\":\"RECEPCIONISTA\"}", adminToken).statusCode());
        assertEquals(400, call(adminClient, "POST", "/api/usuarios",
                "{\"username\":\"legacy\",\"password\":\"test-password-123\",\"rol\":\"JEFE\"}", adminToken).statusCode());
    }

    @Test void limpiezaPuedeFinalizarSinDuplicarMovimiento() throws Exception {
        Registro r = ingreso();
        checkout.confirmar(r.getId(), null, "recepcion-test");
        assertEquals("PENDIENTE DE LIMPIEZA", estado());
        Usuario cleaner = new Usuario(); cleaner.setUsername("cleaner-test");
        cleaner.setPassword("password-test-only"); cleaner.setRol("RECEPCIONISTA"); usuarios.save(cleaner);
        var client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        String token = json.readTree(call(client, "GET", "/api/usuarios/csrf", null, null).body()).get("token").asString();
        assertEquals(200, call(client, "POST", "/api/usuarios/login",
                "{\"username\":\"cleaner-test\",\"password\":\"password-test-only\"}", token).statusCode());
        String path = "/api/check-out/habitaciones/" + habitacion.getId() + "/limpieza";
        assertEquals(200, call(client, "POST", path, "{}", token).statusCode());
        assertEquals("LIBRE", estado());
        long before = movimientos.count();
        assertEquals(200, call(client, "POST", path, "{}", token).statusCode());
        assertEquals(before, movimientos.count());
        assertEquals(200, call(client, "GET", "/api/registros", null, null).statusCode());
    }
}
