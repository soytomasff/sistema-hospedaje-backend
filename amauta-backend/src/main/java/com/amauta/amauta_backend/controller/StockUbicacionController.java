package com.amauta.amauta_backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-ubicacion")
public class StockUbicacionController {

    private final JdbcTemplate jdbcTemplate;

    public StockUbicacionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> listarStockPorUbicacion() {

        String sql = """
            SELECT 
                p.id AS producto_id,
                p.codigo,
                p.nombre AS producto,
                COALESCE(SUM(CASE WHEN u.nombre = 'Almacen principal' THEN su.stock ELSE 0 END), 0) AS almacen_principal,
                COALESCE(SUM(CASE WHEN u.nombre = 'Administracion' THEN su.stock ELSE 0 END), 0) AS administracion,
                COALESCE(SUM(CASE WHEN u.nombre = 'Habitaciones' THEN su.stock ELSE 0 END), 0) AS habitaciones,
                COALESCE(SUM(CASE WHEN u.nombre = 'Recepcion' THEN su.stock ELSE 0 END), 0) AS recepcion,
                COALESCE(SUM(su.stock), 0) AS total
            FROM producto p
            LEFT JOIN stock_ubicacion su ON su.producto_id = p.id
            LEFT JOIN ubicacion_inventario u ON u.id = su.ubicacion_id
            WHERE p.codigo LIKE 'INV-%'
            GROUP BY p.id, p.codigo, p.nombre
            ORDER BY p.nombre
        """;

        return jdbcTemplate.queryForList(sql);
    }

    @PostMapping("/guardar")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> guardarStock(@RequestBody Map<String, Object> body) {
        for (String campo : List.of("productoId", "almacenPrincipal", "administracion", "habitaciones", "recepcion")) {
            com.amauta.amauta_backend.service.Validaciones.exigir(body.get(campo) != null, "Falta " + campo);
        }

        Long productoId = Long.valueOf(body.get("productoId").toString());

        Integer almacenPrincipal = Integer.valueOf(body.get("almacenPrincipal").toString());
        Integer administracion = Integer.valueOf(body.get("administracion").toString());
        Integer habitaciones = Integer.valueOf(body.get("habitaciones").toString());
        Integer recepcion = Integer.valueOf(body.get("recepcion").toString());
        for (Integer stock : List.of(almacenPrincipal, administracion, habitaciones, recepcion)) {
            com.amauta.amauta_backend.service.Validaciones.noNegativo(stock, "Stock");
        }
        com.amauta.amauta_backend.service.Validaciones.exigir(Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM producto WHERE id = ? AND codigo LIKE 'INV-%')", Boolean.class, productoId)),
                "Selecciona un producto con codigo INV-");

        guardarOActualizar(productoId, "Almacen principal", almacenPrincipal);
        guardarOActualizar(productoId, "Administracion", administracion);
        guardarOActualizar(productoId, "Habitaciones", habitaciones);
        guardarOActualizar(productoId, "Recepcion", recepcion);

        return Map.of("mensaje", "Stock actualizado correctamente");
    }

    private void guardarOActualizar(Long productoId, String ubicacion, Integer stock) {

        String sql = """
            INSERT INTO stock_ubicacion (producto_id, ubicacion_id, stock)
            SELECT ?, u.id, ?
            FROM ubicacion_inventario u
            WHERE u.nombre = ?
            ON CONFLICT (producto_id, ubicacion_id)
            DO UPDATE SET stock = EXCLUDED.stock
        """;

        jdbcTemplate.update(sql, productoId, stock, ubicacion);
    }
}
