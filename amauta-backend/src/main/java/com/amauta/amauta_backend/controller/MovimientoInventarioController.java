package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.MovimientoInventario;
import com.amauta.amauta_backend.service.MovimientoInventarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoInventarioController {

    private final MovimientoInventarioService service;

    public MovimientoInventarioController(MovimientoInventarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<MovimientoInventario> listar() {
        return service.listar();
    }

    @PostMapping
    public MovimientoInventario guardar(@RequestBody MovimientoInventario movimiento) {
        return service.guardar(movimiento);
    }
}