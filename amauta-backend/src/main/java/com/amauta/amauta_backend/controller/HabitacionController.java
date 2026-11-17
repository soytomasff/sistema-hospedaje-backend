package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.service.HabitacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionController {

    private final HabitacionRepository habitacionRepository;
    private final HabitacionService service;

    public HabitacionController(HabitacionRepository habitacionRepository, HabitacionService service) {
        this.habitacionRepository = habitacionRepository;
        this.service = service;
    }

    // LISTAR
    @GetMapping
    public List<Habitacion> listar() {
        return service.listar();
    }

    // GUARDAR
    @PostMapping
    public Habitacion guardar(@RequestBody Habitacion habitacion) {
        return service.guardar(habitacion);
    }

    // EDITAR
    @PutMapping("/{id}")
    public Habitacion actualizar(
            @PathVariable Long id,
            @RequestBody Habitacion datos
    ) {

        return service.actualizar(id, datos);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        habitacionRepository.deleteById(id);
    }
}
