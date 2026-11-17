package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.VentaProducto;
import com.amauta.amauta_backend.service.VentaProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas-productos")
public class VentaProductoController {

    private final VentaProductoService service;

    public VentaProductoController(VentaProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<VentaProducto> listar() {
        return service.listar();
    }

    @PostMapping
    public VentaProducto guardar(@RequestBody VentaProducto venta) {
        return service.guardar(venta);
    }
}