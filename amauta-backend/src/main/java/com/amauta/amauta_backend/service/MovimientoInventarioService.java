package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.MovimientoInventario;
import com.amauta.amauta_backend.entity.Producto;
import com.amauta.amauta_backend.repository.MovimientoInventarioRepository;
import com.amauta.amauta_backend.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoInventarioService(
            MovimientoInventarioRepository movimientoRepository,
            ProductoRepository productoRepository
    ) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    public List<MovimientoInventario> listar() {
        return movimientoRepository.findAll();
    }

    @Transactional
    public MovimientoInventario guardar(MovimientoInventario movimiento) {

        if (movimiento.getProducto() == null || movimiento.getProducto().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto");
        }

        if (movimiento.getTipoMovimiento() == null || movimiento.getTipoMovimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de movimiento");
        }

        if (movimiento.getCantidad() == null || movimiento.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Producto producto = productoRepository.buscarParaActualizar(movimiento.getProducto().getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        String tipo = movimiento.getTipoMovimiento().trim().toUpperCase();
        if (producto.getStock() == null) producto.setStock(0);

        switch (tipo) {
            case "ENTRADA":
                producto.setStock(producto.getStock() + movimiento.getCantidad());
                break;

            case "SALIDA":
            case "DANADO":
            case "EXTRAVIADO":
                if (producto.getStock() < movimiento.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente");
                }

                producto.setStock(producto.getStock() - movimiento.getCantidad());
                break;

            default:
                throw new IllegalArgumentException("Tipo de movimiento no valido");
        }

        productoRepository.save(producto);

        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setFecha(LocalDateTime.now());

        return movimientoRepository.save(movimiento);
    }
}
