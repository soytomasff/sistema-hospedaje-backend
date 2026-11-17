package com.amauta.amauta_backend;

import com.amauta.amauta_backend.entity.Usuario;
import com.amauta.amauta_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.json.JsonMapper;
import java.net.*;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebFlowTest {
    @LocalServerPort int port;
    @Autowired UsuarioRepository usuarios;
    final JsonMapper json = JsonMapper.builder().build();

    @Test void corsPermiteSoloElFrontendConfigurado() throws Exception {
        var client = HttpClient.newHttpClient();
        var uri = URI.create("http://localhost:" + port + "/api/huespedes");
        var permitida = client.send(HttpRequest.newBuilder(uri).method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://localhost:4200").header("Access-Control-Request-Method", "GET").build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, permitida.statusCode());
        assertEquals("http://localhost:4200", permitida.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        var rechazada = client.send(HttpRequest.newBuilder(uri).method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://otro-sitio.example").header("Access-Control-Request-Method", "GET").build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(403, rechazada.statusCode());
    }

    @Test void sesionCsrfYFlujoDeHospedaje() throws Exception {
        Usuario user = new Usuario();
        user.setUsername("prueba-web"); user.setPassword("clave-de-prueba"); user.setRol("ADMINISTRADOR");
        usuarios.save(user);
        var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder().cookieHandler(cookies).build();

        assertEquals(401, call(client, "GET", "/api/huespedes", null, null).statusCode());
        String token = json.readTree(call(client, "GET", "/api/usuarios/csrf", null, null).body()).get("token").asString();
        assertEquals(403, call(client, "POST", "/api/usuarios/login",
                "{\"username\":\"prueba-web\",\"password\":\"clave-de-prueba\"}", null).statusCode());
        assertEquals(401, call(client, "POST", "/api/usuarios/login",
                "{\"username\":\"prueba-web\",\"password\":\"incorrecta\"}", token).statusCode());
        var login = call(client, "POST", "/api/usuarios/login",
                "{\"username\":\"prueba-web\",\"password\":\"clave-de-prueba\"}", token);
        assertEquals(200, login.statusCode(), login.body());
        assertFalse(login.body().contains("password"));
        assertTrue(usuarios.findByUsername("prueba-web").orElseThrow().getPassword().startsWith("$2"));
        assertEquals(200, call(client, "GET", "/api/usuarios/me", null, null).statusCode());

        var guest = call(client, "POST", "/api/huespedes",
                "{\"nombre\":\"Ana\",\"apellido\":\"Prueba\",\"dni\":\"87654321\"}", token);
        assertEquals(200, guest.statusCode(), guest.body());
        long guestId = json.readTree(guest.body()).get("id").asLong();
        assertEquals(200, call(client, "GET", "/api/huespedes/dni/87654321", null, null).statusCode());
        var room = call(client, "POST", "/api/habitaciones",
                "{\"codigo\":\"HAB-WEB\",\"numero\":\"901\",\"tipo\":\"SIMPLE\",\"capacidad\":1,\"precio\":60,\"estado\":\"LIBRE\"}", token);
        assertEquals(200, room.statusCode(), room.body());
        long roomId = json.readTree(room.body()).get("id").asLong();
        String registration = "{\"huespedId\":" + guestId + ",\"habitacionId\":" + roomId
                + ",\"fechaIngreso\":\"2026-09-01\",\"horaIngreso\":\"12:00\",\"precio\":60}";
        var entry = call(client, "POST", "/api/registros", registration, token);
        assertEquals(200, entry.statusCode(), entry.body());
        assertEquals("Ana Prueba", json.readTree(entry.body()).get("nombreHuesped").asString());
        long entryId = json.readTree(entry.body()).get("id").asLong();
        assertEquals(409, call(client, "POST", "/api/registros", registration, token).statusCode());
        String departure = registration.substring(0, registration.length() - 1)
                + ",\"fechaSalida\":\"2026-09-02\",\"horaSalida\":\"11:00\"}";
        assertEquals(409, call(client, "PUT", "/api/registros/" + entryId, departure, token).statusCode());
        assertEquals(200, call(client, "POST", "/api/check-out/estadias/" + entryId + "/confirmar",
                "{\"salidaReal\":\"2026-09-02T11:00:00\"}", token).statusCode());
        assertEquals("PENDIENTE DE LIMPIEZA", json.readTree(call(client, "GET", "/api/habitaciones", null, null).body()).get(0).get("estado").asString());
        assertEquals(200, call(client, "POST", "/api/check-out/habitaciones/" + roomId + "/limpieza", "{}", token).statusCode());
        assertEquals("LIBRE", json.readTree(call(client, "GET", "/api/habitaciones", null, null).body()).get(0).get("estado").asString());

        var product = call(client, "POST", "/api/productos",
                "{\"codigo\":\"TEST-AGUA\",\"nombre\":\"Agua\",\"stock\":5,\"stockMinimo\":1,\"precio\":3}", token);
        assertEquals(200, product.statusCode(), product.body());
        long productId = json.readTree(product.body()).get("id").asLong();
        var sale = call(client, "POST", "/api/ventas-productos",
                "{\"producto\":{\"id\":" + productId + "},\"cantidad\":2,\"metodoPago\":\"YAPE\"}", token);
        assertEquals(200, sale.statusCode(), sale.body());
        assertEquals(6, json.readTree(sale.body()).get("subtotal").asInt());
        var insufficient = call(client, "POST", "/api/ventas-productos",
                "{\"producto\":{\"id\":" + productId + "},\"cantidad\":9,\"metodoPago\":\"YAPE\"}", token);
        assertEquals(400, insufficient.statusCode());
        assertEquals(3, json.readTree(call(client, "GET", "/api/productos", null, null).body()).get(0).get("stock").asInt());
        assertEquals(204, call(client, "POST", "/api/usuarios/logout", "{}", token).statusCode());
        assertEquals(401, call(client, "GET", "/api/huespedes", null, null).statusCode());
    }

    private HttpResponse<String> call(HttpClient client, String method, String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("X-XSRF-TOKEN", token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
