package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.Huesped;
import com.amauta.amauta_backend.repository.HuespedRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/huespedes")
public class HuespedController {

    private final HuespedRepository huespedRepository;

    public HuespedController(HuespedRepository huespedRepository) {
        this.huespedRepository = huespedRepository;
    }

    @GetMapping
    public List<Huesped> listar() {
        return huespedRepository.findAll();
    }

    @PostMapping
    public Huesped guardar(@RequestBody Huesped huesped) {
        validar(huesped);
        huesped.setId(null);
        return huespedRepository.save(huesped);
    }

    @PutMapping("/{id}")
    public Huesped actualizar(@PathVariable Long id, @RequestBody Huesped datos) {
        validar(datos);
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Huesped no encontrado"));

        huesped.setNombre(datos.getNombre());
        huesped.setApellido(datos.getApellido());
        huesped.setDni(datos.getDni());
        huesped.setProcedencia(datos.getProcedencia());
        huesped.setPais(datos.getPais());
        huesped.setProfesion(datos.getProfesion());
        huesped.setTelefono(datos.getTelefono());

        return huespedRepository.save(huesped);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        huespedRepository.deleteById(id);
    }

    private void validar(Huesped huesped) {
        com.amauta.amauta_backend.service.Validaciones.texto(huesped.getNombre(), "Nombre");
        com.amauta.amauta_backend.service.Validaciones.texto(huesped.getApellido(), "Apellido");
        com.amauta.amauta_backend.service.Validaciones.exigir(huesped.getDni() != null
                && huesped.getDni().matches("[A-Za-z0-9-]{5,20}"), "Documento invalido");
    }
}
