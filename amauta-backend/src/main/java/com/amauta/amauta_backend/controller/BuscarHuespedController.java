package com.amauta.amauta_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/huespedes")
public class BuscarHuespedController {

    private final JdbcTemplate jdbcTemplate;

    public BuscarHuespedController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test-dni")
    public Map<String, Object> test() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Busqueda por DNI funcionando");
        return respuesta;
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<?> buscarPorDni(@PathVariable String dni) {

        String sql = """
            SELECT
                id,
                nombre,
                apellido,
                dni,
                procedencia,
                telefono,
                pais,
                profesion
            FROM huesped
            WHERE dni = ?
        """;

        List<Map<String, Object>> lista = jdbcTemplate.queryForList(sql, dni);

        if (lista.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(lista.get(0));
    }
}