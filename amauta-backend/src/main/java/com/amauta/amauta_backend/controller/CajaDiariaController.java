package com.amauta.amauta_backend.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/caja-diaria")
public class CajaDiariaController {

    private final JdbcTemplate jdbcTemplate;

    public CajaDiariaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> obtenerCajaDiaria(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        List<Map<String, Object>> movimientos = obtenerMovimientos(fecha);
        Map<String, Object> resumen = obtenerResumen(fecha);
        List<Map<String, Object>> porMetodo = obtenerPorMetodo(fecha);

        Boolean cerrado = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM cierre_caja WHERE fecha = ?)",
                Boolean.class,
                Date.valueOf(fecha)
        );

        resumen.put("cerrado", cerrado != null && cerrado);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("fecha", fecha.toString());
        respuesta.put("movimientos", movimientos);
        respuesta.put("resumen", resumen);
        respuesta.put("porMetodo", porMetodo);

        return respuesta;
    }

    private List<Map<String, Object>> obtenerMovimientos(LocalDate fecha) {

        String sql = """
                SELECT
                    fecha,
                    hora,
                    tipo,
                    habitacion,
                    detalle,
                    metodo_pago,
                    ingreso,
                    egreso,
                    observacion
                FROM vista_caja_diaria
                WHERE fecha = ?
                ORDER BY hora
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> fila = new LinkedHashMap<>();

            fila.put("fecha", rs.getDate("fecha").toLocalDate().toString());
            fila.put("hora", rs.getString("hora"));
            fila.put("tipo", rs.getString("tipo"));
            fila.put("habitacion", rs.getString("habitacion"));
            fila.put("detalle", rs.getString("detalle"));
            fila.put("metodoPago", rs.getString("metodo_pago"));
            fila.put("ingreso", rs.getBigDecimal("ingreso"));
            fila.put("egreso", rs.getBigDecimal("egreso"));
            fila.put("observacion", rs.getString("observacion"));

            return fila;
        }, Date.valueOf(fecha));
    }

    private Map<String, Object> obtenerResumen(LocalDate fecha) {

        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN tipo = 'HOSPEDAJE' THEN ingreso ELSE 0 END), 0) AS total_hospedaje,
                    COALESCE(SUM(CASE WHEN tipo = 'VENTA VITRINA' THEN ingreso ELSE 0 END), 0) AS total_vitrina,
                    COALESCE(SUM(ingreso), 0) AS total_ingresos,
                    COALESCE(SUM(egreso), 0) AS total_egresos,
                    COALESCE(SUM(ingreso - egreso), 0) AS total_dia,

                    COALESCE(SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN ingreso - egreso ELSE 0 END), 0) AS efectivo,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'YAPE' THEN ingreso - egreso ELSE 0 END), 0) AS yape,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'PLIN' THEN ingreso - egreso ELSE 0 END), 0) AS plin,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'TARJETA' THEN ingreso - egreso ELSE 0 END), 0) AS tarjeta,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'TRANSFERENCIA' THEN ingreso - egreso ELSE 0 END), 0) AS transferencia
                FROM vista_caja_diaria
                WHERE fecha = ?
                """;

        Map<String, Object> row = jdbcTemplate.queryForMap(sql, Date.valueOf(fecha));

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalHospedaje", row.get("total_hospedaje"));
        resumen.put("totalVitrina", row.get("total_vitrina"));
        resumen.put("totalIngresos", row.get("total_ingresos"));
        resumen.put("totalEgresos", row.get("total_egresos"));
        resumen.put("totalDia", row.get("total_dia"));

        resumen.put("efectivo", row.get("efectivo"));
        resumen.put("yape", row.get("yape"));
        resumen.put("plin", row.get("plin"));
        resumen.put("tarjeta", row.get("tarjeta"));
        resumen.put("transferencia", row.get("transferencia"));

        return resumen;
    }

    private List<Map<String, Object>> obtenerPorMetodo(LocalDate fecha) {

        String sql = """
                SELECT
                    metodo_pago,
                    COALESCE(SUM(ingreso), 0) AS ingresos,
                    COALESCE(SUM(egreso), 0) AS egresos,
                    COALESCE(SUM(ingreso - egreso), 0) AS total
                FROM vista_caja_diaria
                WHERE fecha = ?
                GROUP BY metodo_pago
                ORDER BY metodo_pago
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> fila = new LinkedHashMap<>();

            fila.put("metodoPago", rs.getString("metodo_pago"));
            fila.put("ingresos", rs.getBigDecimal("ingresos"));
            fila.put("egresos", rs.getBigDecimal("egresos"));
            fila.put("total", rs.getBigDecimal("total"));

            return fila;
        }, Date.valueOf(fecha));
    }

    @PostMapping("/egresos")
    public Map<String, Object> registrarEgreso(@RequestBody EgresoRequest request) {
        com.amauta.amauta_backend.service.Validaciones.metodoPago(request.metodoPago);

        if (request.descripcion == null || request.descripcion.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripcion es obligatoria");
        }

        if (request.monto == null || request.monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a cero");
        }

        LocalDate fecha = LocalDate.now();

        if (request.fecha != null && !request.fecha.trim().isEmpty()) {
            fecha = LocalDate.parse(request.fecha);
        }

        Boolean cerrado = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM cierre_caja WHERE fecha = ?)",
                Boolean.class,
                Date.valueOf(fecha)
        );

        if (cerrado != null && cerrado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La caja de esa fecha ya esta cerrada");
        }

        Timestamp fechaHora = Timestamp.valueOf(fecha.atTime(LocalTime.now()));

        String sql = """
                INSERT INTO egreso_caja
                (fecha, descripcion, metodo_pago, monto, observacion)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                fechaHora,
                request.descripcion,
                request.metodoPago,
                request.monto,
                request.observacion
        );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Egreso registrado correctamente");

        return respuesta;
    }

    @PostMapping("/cerrar")
    public Map<String, Object> cerrarCaja(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam(defaultValue = "admin")
            String usuario,
            java.security.Principal principal
    ) {
        usuario = principal.getName();

        String sql = """
                INSERT INTO cierre_caja (
                    fecha,
                    total_hospedaje,
                    total_vitrina,
                    total_ingresos,
                    total_egresos,
                    total_final,
                    efectivo,
                    yape,
                    plin,
                    tarjeta,
                    transferencia,
                    cerrado,
                    fecha_cierre,
                    usuario,
                    observacion
                )
                SELECT
                    ? AS fecha,

                    COALESCE(SUM(CASE WHEN tipo = 'HOSPEDAJE' THEN ingreso ELSE 0 END), 0) AS total_hospedaje,
                    COALESCE(SUM(CASE WHEN tipo = 'VENTA VITRINA' THEN ingreso ELSE 0 END), 0) AS total_vitrina,

                    COALESCE(SUM(ingreso), 0) AS total_ingresos,
                    COALESCE(SUM(egreso), 0) AS total_egresos,
                    COALESCE(SUM(ingreso - egreso), 0) AS total_final,

                    COALESCE(SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN ingreso - egreso ELSE 0 END), 0) AS efectivo,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'YAPE' THEN ingreso - egreso ELSE 0 END), 0) AS yape,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'PLIN' THEN ingreso - egreso ELSE 0 END), 0) AS plin,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'TARJETA' THEN ingreso - egreso ELSE 0 END), 0) AS tarjeta,
                    COALESCE(SUM(CASE WHEN metodo_pago = 'TRANSFERENCIA' THEN ingreso - egreso ELSE 0 END), 0) AS transferencia,

                    TRUE AS cerrado,
                    CURRENT_TIMESTAMP AS fecha_cierre,
                    ? AS usuario,
                    'Cierre de caja diario' AS observacion

                FROM vista_caja_diaria
                WHERE fecha = ?

                ON CONFLICT (fecha)
                DO UPDATE SET
                    total_hospedaje = EXCLUDED.total_hospedaje,
                    total_vitrina = EXCLUDED.total_vitrina,
                    total_ingresos = EXCLUDED.total_ingresos,
                    total_egresos = EXCLUDED.total_egresos,
                    total_final = EXCLUDED.total_final,
                    efectivo = EXCLUDED.efectivo,
                    yape = EXCLUDED.yape,
                    plin = EXCLUDED.plin,
                    tarjeta = EXCLUDED.tarjeta,
                    transferencia = EXCLUDED.transferencia,
                    cerrado = TRUE,
                    fecha_cierre = CURRENT_TIMESTAMP,
                    usuario = EXCLUDED.usuario,
                    observacion = EXCLUDED.observacion
                """;

        jdbcTemplate.update(
                sql,
                Date.valueOf(fecha),
                usuario,
                Date.valueOf(fecha)
        );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Caja cerrada correctamente");
        respuesta.put("fecha", fecha.toString());

        return respuesta;
    }

    public static class EgresoRequest {
        public String fecha;
        public String descripcion;
        public String metodoPago;
        public BigDecimal monto;
        public String observacion;
    }
}
