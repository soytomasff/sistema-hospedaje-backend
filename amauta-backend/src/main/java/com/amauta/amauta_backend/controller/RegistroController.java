package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.Registro;
import com.amauta.amauta_backend.service.RegistroService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registros")
@org.springframework.security.access.prepost.PreAuthorize("@permisosEstadia.recepcion()")
public class RegistroController {
    private final RegistroService service;

    public RegistroController(RegistroService service) {
        this.service = service;
    }

    @GetMapping
    public List<Registro> listar() {
        return service.listar();
    }

    @PostMapping
    public Registro guardar(@RequestBody Registro registro) {
        return service.guardar(registro);
    }

    @PutMapping("/{id}")
    public Registro actualizar(@PathVariable Long id, @RequestBody Registro datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
