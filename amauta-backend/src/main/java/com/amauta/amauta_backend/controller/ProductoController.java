package com.amauta.amauta_backend.controller;

import com.amauta.amauta_backend.entity.Producto;
import com.amauta.amauta_backend.repository.ProductoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // LISTAR
    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    // GUARDAR
    @PostMapping
    public Producto guardar(@RequestBody Producto producto) {
        validar(producto);
        producto.setId(null);
        return productoRepository.save(producto);
    }

    // ACTUALIZAR
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        validar(producto);
        if (!productoRepository.existsById(id)) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Producto no encontrado");
        producto.setId(id);
        return productoRepository.save(producto);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        productoRepository.deleteById(id);
    }

    private void validar(Producto producto) {
        com.amauta.amauta_backend.service.Validaciones.texto(producto.getCodigo(), "Codigo");
        com.amauta.amauta_backend.service.Validaciones.texto(producto.getNombre(), "Nombre");
        com.amauta.amauta_backend.service.Validaciones.noNegativo(producto.getStock(), "Stock");
        com.amauta.amauta_backend.service.Validaciones.noNegativo(producto.getStockMinimo(), "Stock minimo");
        com.amauta.amauta_backend.service.Validaciones.precio(producto.getPrecio());
    }
}
