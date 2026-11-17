package com.amauta.amauta_backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipamiento-habitaciones")
public class EquipamientoHabitacionController {

    private final JdbcTemplate jdbcTemplate;

    public EquipamientoHabitacionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> listarEquipamiento() {

        String sql = """
            SELECT
                h.id AS habitacion_id,
                h.numero AS habitacion,
                h.tipo,

                COALESCE(SUM(CASE WHEN i.nombre = 'TV' THEN hi.cantidad ELSE 0 END), 0) AS tv,
                COALESCE(SUM(CASE WHEN i.nombre = 'Mesa' THEN hi.cantidad ELSE 0 END), 0) AS mesa,
                COALESCE(SUM(CASE WHEN i.nombre = 'Silla' THEN hi.cantidad ELSE 0 END), 0) AS silla,
                COALESCE(SUM(CASE WHEN i.nombre = 'Cama' THEN hi.cantidad ELSE 0 END), 0) AS cama,
                COALESCE(SUM(CASE WHEN i.nombre = 'Control remoto' THEN hi.cantidad ELSE 0 END), 0) AS control_remoto,
                COALESCE(SUM(CASE WHEN i.nombre = 'Frazada' THEN hi.cantidad ELSE 0 END), 0) AS frazada,
                COALESCE(SUM(CASE WHEN i.nombre = 'Sabana' THEN hi.cantidad ELSE 0 END), 0) AS sabana,
                COALESCE(SUM(CASE WHEN i.nombre = 'Toalla' THEN hi.cantidad ELSE 0 END), 0) AS toalla,

                CASE WHEN hs.cable THEN 'SI' ELSE 'NO' END AS cable,
                CASE WHEN hs.wifi THEN 'SI' ELSE 'NO' END AS wifi,
                CASE WHEN hs.ducha_caliente THEN 'SI' ELSE 'NO' END AS ducha_caliente,
                COALESCE(hs.tipo_bano, 'COMPARTIDO') AS tipo_bano,
                CASE WHEN hs.ventilador THEN 'SI' ELSE 'NO' END AS ventilador,
                CASE WHEN hs.tv_smart THEN 'SI' ELSE 'NO' END AS tv_smart,
                CASE WHEN hs.aire_acondicionado THEN 'SI' ELSE 'NO' END AS aire_acondicionado,
                COALESCE(hs.observacion, '') AS observacion

            FROM habitacion h
            LEFT JOIN habitacion_item hi ON hi.habitacion_id = h.id
            LEFT JOIN catalogo_item_habitacion i ON i.id = hi.item_id
            LEFT JOIN habitacion_servicio hs ON hs.habitacion_id = h.id
            GROUP BY
                h.id,
                h.numero,
                h.tipo,
                hs.cable,
                hs.wifi,
                hs.ducha_caliente,
                hs.tipo_bano,
                hs.ventilador,
                hs.tv_smart,
                hs.aire_acondicionado,
                hs.observacion
            ORDER BY h.numero
        """;

        return jdbcTemplate.queryForList(sql);
    }

    @PostMapping("/guardar")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> guardar(@RequestBody Map<String, Object> body) {
        com.amauta.amauta_backend.service.Validaciones.exigir(body.get("habitacionId") != null, "Selecciona una habitacion");
        com.amauta.amauta_backend.service.Validaciones.exigir(
                java.util.Set.of("PRIVADO", "COMPARTIDO").contains(String.valueOf(body.get("tipoBano"))), "Tipo de baño invalido");

        Long habitacionId = Long.valueOf(body.get("habitacionId").toString());
        com.amauta.amauta_backend.service.Validaciones.exigir(Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM habitacion WHERE id = ?)", Boolean.class, habitacionId)), "Habitacion no encontrada");

        guardarItem(habitacionId, "TV", numero(body.get("tv")));
        guardarItem(habitacionId, "Mesa", numero(body.get("mesa")));
        guardarItem(habitacionId, "Silla", numero(body.get("silla")));
        guardarItem(habitacionId, "Cama", numero(body.get("cama")));
        guardarItem(habitacionId, "Control remoto", numero(body.get("controlRemoto")));
        guardarItem(habitacionId, "Frazada", numero(body.get("frazada")));
        guardarItem(habitacionId, "Sabana", numero(body.get("sabana")));
        guardarItem(habitacionId, "Toalla", numero(body.get("toalla")));

        Boolean cable = booleano(body.get("cable"));
        Boolean wifi = booleano(body.get("wifi"));
        Boolean duchaCaliente = booleano(body.get("duchaCaliente"));
        Boolean ventilador = booleano(body.get("ventilador"));
        Boolean tvSmart = booleano(body.get("tvSmart"));
        Boolean aireAcondicionado = booleano(body.get("aireAcondicionado"));

        String tipoBano = body.get("tipoBano").toString();
        Boolean banoPrivado = tipoBano.equalsIgnoreCase("PRIVADO");

        String observacion = body.get("observacion") == null ? "" : body.get("observacion").toString();

        String sqlServicio = """
            INSERT INTO habitacion_servicio
            (habitacion_id, cable, wifi, ducha_caliente, bano_privado, tipo_bano, tv_smart, ventilador, aire_acondicionado, observacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (habitacion_id)
            DO UPDATE SET
                cable = EXCLUDED.cable,
                wifi = EXCLUDED.wifi,
                ducha_caliente = EXCLUDED.ducha_caliente,
                bano_privado = EXCLUDED.bano_privado,
                tipo_bano = EXCLUDED.tipo_bano,
                tv_smart = EXCLUDED.tv_smart,
                ventilador = EXCLUDED.ventilador,
                aire_acondicionado = EXCLUDED.aire_acondicionado,
                observacion = EXCLUDED.observacion
        """;

        jdbcTemplate.update(
                sqlServicio,
                habitacionId,
                cable,
                wifi,
                duchaCaliente,
                banoPrivado,
                tipoBano,
                tvSmart,
                ventilador,
                aireAcondicionado,
                observacion
        );

        return Map.of("mensaje", "Equipamiento guardado correctamente");
    }

    @DeleteMapping("/{habitacionId}")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> eliminar(@PathVariable Long habitacionId) {

        jdbcTemplate.update(
                "DELETE FROM habitacion_item WHERE habitacion_id = ?",
                habitacionId
        );

        jdbcTemplate.update(
                "DELETE FROM habitacion_servicio WHERE habitacion_id = ?",
                habitacionId
        );

        return Map.of("mensaje", "Equipamiento eliminado correctamente");
    }

    private void guardarItem(Long habitacionId, String nombreItem, Integer cantidad) {

        String sql = """
            INSERT INTO habitacion_item
            (habitacion_id, item_id, cantidad, estado, observacion)
            SELECT ?, id, ?, 'BUENO', 'Actualizado desde sistema'
            FROM catalogo_item_habitacion
            WHERE nombre = ?
            ON CONFLICT (habitacion_id, item_id)
            DO UPDATE SET
                cantidad = EXCLUDED.cantidad,
                estado = EXCLUDED.estado,
                observacion = EXCLUDED.observacion
        """;

        jdbcTemplate.update(sql, habitacionId, cantidad, nombreItem);
    }

    private Integer numero(Object valor) {
        if (valor == null) return 0;
        Integer cantidad = Integer.valueOf(valor.toString());
        com.amauta.amauta_backend.service.Validaciones.noNegativo(cantidad, "Cantidad");
        return cantidad;
    }

    private Boolean booleano(Object valor) {
        if (valor == null) return false;
        return Boolean.valueOf(valor.toString());
    }
}
