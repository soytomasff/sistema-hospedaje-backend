package com.amauta.amauta_backend.service;
import com.amauta.amauta_backend.entity.Registro;
import com.amauta.amauta_backend.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BoletaHospedajeServiceTest {
    @Test void calculaNochesConTarifaHistoricaYEvitaDuplicados() {
        var boletas = mock(BoletaHospedajeRepository.class);
        var registros = mock(RegistroRepository.class);
        Registro registro = new Registro();
        registro.setFechaIngreso(LocalDate.of(2026,9,1)); registro.setFechaSalida(LocalDate.of(2026,9,3));
        registro.setHoraSalida(LocalTime.NOON); registro.setPrecio(new BigDecimal("45.50"));
        registro.setNombreHuesped("Ana Prueba"); registro.setNumeroCuarto("101");
        when(registros.buscarParaActualizar(1L)).thenReturn(Optional.of(registro));
        when(boletas.obtenerUltimoNumero()).thenReturn(5L);
        when(boletas.save(any())).thenAnswer(i -> i.getArgument(0));
        var service = new BoletaHospedajeService(boletas, registros);
        var bill = service.generarBoleta(1L, "YAPE");
        assertEquals(new BigDecimal("91.00"), bill.getTotal());
        assertEquals(2, bill.getNoches()); assertEquals(6L, bill.getNumero());
        verify(boletas).bloquearNumeracion();
        when(boletas.existsByRegistroId(1L)).thenReturn(true);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.generarBoleta(1L, "YAPE"));
        verify(boletas, times(1)).save(any());
    }
}

