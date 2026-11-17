package com.amauta.amauta_backend;

import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.entity.Huesped;
import com.amauta.amauta_backend.entity.Usuario;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.HuespedRepository;
import com.amauta.amauta_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:recepcion_http;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
class RecepcionHttpTest {
    @LocalServerPort int port;
    @Autowired UsuarioRepository usuarios;
    @Autowired HuespedRepository huespedes;
    @Autowired HabitacionRepository habitaciones;
    private final JsonMapper json = JsonMapper.builder().build();

    @Test void checkInHttpExigeSesionYCsrF() throws Exception {
        long suffix = System.nanoTime();
        Usuario user = new Usuario();
        user.setUsername("recepcion-http-" + suffix);
        user.setPassword("clave-prueba"); user.setRol("RECEPCIONISTA");
        usuarios.save(user);
        Huesped guest = new Huesped();
        guest.setNombre("Recepcion"); guest.setApellido("Http"); guest.setDni("HTTP-" + suffix);
        guest = huespedes.save(guest);
        Habitacion room = new Habitacion();
        room.setCodigo("HTTP-" + suffix); room.setNumero("960");
        room.setTipo("SIMPLE"); room.setCapacidad(1);
        room.setPrecio(new BigDecimal("90")); room.setEstado("LIBRE");
        room = habitaciones.save(room);
        var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder().cookieHandler(cookies).build();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Lima")).minusMinutes(1);
        String baseEntry = "{\"huespedId\":" + guest.getId() + ",\"habitacionId\":" + room.getId()
                + ",\"fechaIngreso\":\"" + now.toLocalDate() + "\",\"horaIngreso\":\""
                + now.toLocalTime().withSecond(0).withNano(0) + "\"}";

        assertEquals(401, call(client, "GET", "/api/registros", null, null).statusCode());
        String token = json.readTree(call(client, "GET", "/api/usuarios/csrf", null, null)
                .body()).get("token").asString();
        assertEquals(403, call(client, "POST", "/api/recepcion/check-in", baseEntry, null).statusCode());
        var login = call(client, "POST", "/api/usuarios/login",
                "{\"username\":\"" + user.getUsername() + "\",\"password\":\"clave-prueba\"}", token);
        assertEquals(200, login.statusCode(), login.body());
        String reservationBody = "{\"huesped\":{\"id\":" + guest.getId()
                + "},\"habitacion\":{\"id\":" + room.getId()
                + "},\"fechaIngreso\":\"" + now.toLocalDate()
                + "\",\"fechaSalida\":\"" + now.toLocalDate().plusDays(1)
                + "\",\"estado\":\"RESERVADO\"}";
        var reserved = call(client, "POST", "/reservas", reservationBody, token);
        assertEquals(200, reserved.statusCode(), reserved.body());
        long reservationId = json.readTree(reserved.body()).get("id").asLong();
        String entry = baseEntry.substring(0, baseEntry.length() - 1)
                + ",\"reservaId\":" + reservationId + "}";
        var saved = call(client, "POST", "/api/recepcion/check-in", entry, token);
        assertEquals(200, saved.statusCode(), saved.body());
        long id = json.readTree(saved.body()).get("id").asLong();
        assertTrue(id > 0);
        assertEquals("INGRESADO", json.readTree(call(client, "GET", "/reservas/" + reservationId,
                null, null).body()).get("estado").asString());
        assertEquals(409, call(client, "PUT", "/reservas/" + reservationId,
                "{\"estado\":\"CANCELADO\"}", token).statusCode());
        assertEquals(409, call(client, "DELETE", "/reservas/" + reservationId,
                null, token).statusCode());
        assertEquals(409, call(client, "POST", "/api/recepcion/check-in", entry, token).statusCode());
        assertEquals(200, call(client, "POST", "/api/check-out/estadias/" + id + "/confirmar",
                "{\"salidaReal\":null}", token).statusCode());
        assertEquals("PENDIENTE DE LIMPIEZA", habitaciones.findById(room.getId()).orElseThrow().getEstado());
    }

    private HttpResponse<String> call(HttpClient client, String method, String path,
                                      String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("X-XSRF-TOKEN", token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
