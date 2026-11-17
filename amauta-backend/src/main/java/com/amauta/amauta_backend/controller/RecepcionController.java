package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.Registro;
import com.amauta.amauta_backend.service.RecepcionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/recepcion")
@PreAuthorize("@permisosEstadia.recepcion()")
public class RecepcionController {
    public record Entrada(Long huespedId, Long habitacionId, LocalDate fechaIngreso,
                          LocalTime horaIngreso, Long reservaId, java.time.LocalDateTime salidaPrevista) {
        public Entrada(Long huespedId, Long habitacionId, LocalDate fechaIngreso,
                       LocalTime horaIngreso, Long reservaId) {
            this(huespedId, habitacionId, fechaIngreso, horaIngreso, reservaId, null);
        }
    }

    private final RecepcionService service;
    public RecepcionController(RecepcionService service) { this.service = service; }

    @PostMapping("/check-in")
    public Registro checkIn(@RequestBody Entrada entrada) { return service.registrar(entrada); }
}
