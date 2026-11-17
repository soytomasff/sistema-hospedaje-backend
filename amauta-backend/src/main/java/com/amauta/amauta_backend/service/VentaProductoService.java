package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.MovimientoInventario;
import com.amauta.amauta_backend.entity.Producto;
import com.amauta.amauta_backend.entity.VentaProducto;
import com.amauta.amauta_backend.repository.MovimientoInventarioRepository;
import com.amauta.amauta_backend.repository.ProductoRepository;
import com.amauta.amauta_backend.repository.VentaProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaProductoService {

    private final VentaProductoRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public VentaProductoService(
            VentaProductoRepository ventaRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public List<VentaProducto> listar() {
        return ventaRepository.findAll();
    }

    @Transactional
    public VentaProducto guardar(VentaProducto venta) {
        Validaciones.metodoPago(venta.getMetodoPago());

        if (venta.getProducto() == null || venta.getProducto().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto");
        }

        if (venta.getCantidad() == null || venta.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        if (venta.getMetodoPago() == null || venta.getMetodoPago().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un metodo de pago");
        }

        Producto producto = productoRepository.buscarParaActualizar(venta.getProducto().getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (producto.getStock() == null || producto.getStock() < venta.getCantidad()) {
            throw new IllegalArgumentException("Stock insuficiente");
        }

        producto.setStock(producto.getStock() - venta.getCantidad());
        productoRepository.save(producto);

        BigDecimal precioUnitario = producto.getPrecio();
        Validaciones.precio(precioUnitario);
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(venta.getCantidad()));

        venta.setProducto(producto);
        venta.setPrecioUnitario(precioUnitario);
        venta.setSubtotal(subtotal);
        venta.setMetodoPago(venta.getMetodoPago().toUpperCase());
        venta.setFecha(LocalDateTime.now());

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento("SALIDA");
        movimiento.setCantidad(venta.getCantidad());
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setObservacion("Venta de vitrina - " + venta.getMetodoPago());

        movimientoRepository.save(movimiento);

        return ventaRepository.save(venta);
    }
}
