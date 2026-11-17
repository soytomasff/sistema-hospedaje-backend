package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.BoletaHospedaje;
import com.amauta.amauta_backend.service.BoletaHospedajeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boletas")
public class BoletaHospedajeController {

    private final BoletaHospedajeService service;

    public BoletaHospedajeController(BoletaHospedajeService service) {
        this.service = service;
    }

    @GetMapping
    public List<BoletaHospedaje> listar() {
        return service.listar();
    }

    @PostMapping
    public BoletaHospedaje generar(@RequestBody Map<String, Object> body) {
        if (body.get("registroId") == null || body.get("metodoPago") == null) {
            throw new IllegalArgumentException("Selecciona hospedaje y metodo de pago");
        }
        Long registroId = Long.valueOf(body.get("registroId").toString());
        String metodoPago = body.get("metodoPago").toString();

        return service.generarBoleta(registroId, metodoPago);
    }
}
