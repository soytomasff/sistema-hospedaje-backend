package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.BoletaHospedaje;
import com.amauta.amauta_backend.entity.Registro;
import com.amauta.amauta_backend.repository.BoletaHospedajeRepository;
import com.amauta.amauta_backend.repository.RegistroRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BoletaHospedajeService {

    private final BoletaHospedajeRepository boletaRepository;
    private final RegistroRepository registroRepository;

    public BoletaHospedajeService(
            BoletaHospedajeRepository boletaRepository,
            RegistroRepository registroRepository
    ) {
        this.boletaRepository = boletaRepository;
        this.registroRepository = registroRepository;
    }

    public List<BoletaHospedaje> listar() {
        return boletaRepository.findAll();
    }

    @Transactional
    public BoletaHospedaje generarBoleta(Long registroId, String metodoPago) {
        com.amauta.amauta_backend.service.Validaciones.metodoPago(metodoPago);
        boletaRepository.bloquearNumeracion();
        if (boletaRepository.existsByRegistroId(registroId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Ya existe una boleta para este hospedaje");
        }

        Registro registro = registroRepository.buscarParaActualizar(registroId)
                .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado"));

        if (registro.getFechaIngreso() == null || registro.getFechaSalida() == null || registro.getHoraSalida() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Confirma la fecha y hora de salida antes de emitir la boleta");
        }

        long nochesCalculadas = ChronoUnit.DAYS.between(
                registro.getFechaIngreso(),
                registro.getFechaSalida()
        );

        if (nochesCalculadas <= 0) {
            nochesCalculadas = 1;
        }

        BigDecimal precioNoche;

        if (registro.getPrecio() != null) {
            precioNoche = registro.getPrecio();
        } else {
            if (registro.getPrecioCuarto() == null) throw new IllegalArgumentException("El hospedaje no tiene tarifa");
            precioNoche = BigDecimal.valueOf(registro.getPrecioCuarto());
        }

        BigDecimal subtotal = precioNoche.multiply(BigDecimal.valueOf(nochesCalculadas));

        BigDecimal igv = BigDecimal.ZERO;

        BigDecimal total = subtotal.add(igv);

        Long nuevoNumero = boletaRepository.obtenerUltimoNumero() + 1;

        BoletaHospedaje boleta = new BoletaHospedaje();

        boleta.setRegistro(registro);
        boleta.setSerie("B001");
        boleta.setNumero(nuevoNumero);

        boleta.setNombreHuesped(registro.getNombreHuesped());
        boleta.setDni(registro.getDni());
        boleta.setNumeroHabitacion(registro.getNumeroCuarto());

        boleta.setFechaIngreso(registro.getFechaIngreso());
        boleta.setFechaSalida(registro.getFechaSalida());
        boleta.setNoches((int) nochesCalculadas);

        boleta.setPrecioNoche(precioNoche);
        boleta.setSubtotal(subtotal);
        boleta.setIgv(igv);
        boleta.setTotal(total);

        boleta.setMetodoPago(metodoPago.toUpperCase());
        boleta.setFechaEmision(LocalDateTime.now());
        boleta.setEstado("PAGADO");

        return boletaRepository.save(boleta);
    }
}
