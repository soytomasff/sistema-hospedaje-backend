package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.RegistroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HabitacionCompartidaServiceTest {
    HabitacionRepository habitaciones;
    RegistroRepository registros;
    HabitacionService service;
    Habitacion original;

    @BeforeEach void preparar() {
        habitaciones=mock(HabitacionRepository.class); registros=mock(RegistroRepository.class);
        service=new HabitacionService(habitaciones,registros,java.time.Clock.systemUTC());
        original=RegistroCompartidoServiceTest.habitacion(5L,"301","LIBRE");
        when(habitaciones.buscarParaActualizar(5L)).thenReturn(Optional.of(original));
        when(habitaciones.save(any())).thenAnswer(i -> i.getArgument(0));
    }
    @ParameterizedTest @CsvSource({"0,LIBRE", "1,PARCIALMENTE OCUPADA", "2,PARCIALMENTE OCUPADA", "3,OCUPADA"})
    void editarProtegeEstadoYCapacidad(long activos,String esperado) {
        when(registros.contarActivos(5L,null)).thenReturn(activos);
        Habitacion datos=RegistroCompartidoServiceTest.habitacion(999L,"301","LIBRE");
        datos.setCapacidad(1); datos.setPrecio(new BigDecimal("50.00"));
        service.actualizar(5L,datos);
        assertEquals(5L,original.getId()); assertEquals(3,original.getCapacidad());
        assertEquals(esperado,original.getEstado()); assertEquals(new BigDecimal("50.00"),original.getPrecio());
        verify(registros,never()).save(any());
    }
    @Test void normalMantieneEdicionAnterior() {
        original.setNumero("202");
        Habitacion datos=RegistroCompartidoServiceTest.habitacion(null,"202","MANTENIMIENTO");
        datos.setCapacidad(2); service.actualizar(5L,datos);
        assertEquals(2,original.getCapacidad()); assertEquals("MANTENIMIENTO",original.getEstado());
        verify(registros).contarActivos(5L,null);
    }
    @Test void crearCompartidaFijaTresCamas() {
        Habitacion nueva=RegistroCompartidoServiceTest.habitacion(null,"303","OCUPADO");
        service.guardar(nueva); assertEquals(3,nueva.getCapacidad()); assertEquals("LIBRE",nueva.getEstado());
        assertNull(nueva.getId());
    }
    @Test void noPermiteUsarPostParaSobrescribirHabitacion() {
        assertThrows(ResponseStatusException.class, () -> service.guardar(original));
        verify(habitaciones,never()).save(any());
    }
    @Test void noPermiteRenombrarCompartida() {
        Habitacion datos=RegistroCompartidoServiceTest.habitacion(null,"302","LIBRE");
        assertThrows(ResponseStatusException.class, () -> service.actualizar(5L,datos));
        assertEquals("301",original.getNumero());
    }
    @Test void noPermiteMantenimientoConHuespedes() {
        when(registros.contarActivos(5L,null)).thenReturn(1L);
        Habitacion datos=RegistroCompartidoServiceTest.habitacion(null,"301","MANTENIMIENTO");
        assertThrows(ResponseStatusException.class, () -> service.actualizar(5L,datos));
        verify(habitaciones,never()).save(any());
    }
    @Test void mantenimientoVacioYReapertura() {
        Habitacion datos=RegistroCompartidoServiceTest.habitacion(null,"301","MANTENIMIENTO");
        service.actualizar(5L,datos); assertEquals("MANTENIMIENTO",original.getEstado());
        datos.setEstado("LIBRE"); service.actualizar(5L,datos); assertEquals("LIBRE",original.getEstado());
    }
}
