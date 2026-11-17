package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.*;
import com.amauta.amauta_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistroCompartidoServiceTest {
    RegistroRepository registros;
    HabitacionRepository habitaciones;
    RegistroService service;
    Habitacion compartida, normal;
    Registro original;

    @BeforeEach void preparar() {
        registros = mock(RegistroRepository.class);
        habitaciones = mock(HabitacionRepository.class);
        HuespedRepository huespedes = mock(HuespedRepository.class);
        Huesped huesped = new Huesped();
        huesped.setNombre("Ana"); huesped.setApellido("Prueba"); huesped.setDni("12345678");
        when(huespedes.findById(1L)).thenReturn(Optional.of(huesped));
        CheckoutService checkout = mock(CheckoutService.class);
        when(checkout.ahora()).thenReturn(java.time.LocalDateTime.of(2026, 9, 15, 12, 0));
        service = new RegistroService(registros, habitaciones, huespedes, checkout);
        compartida = habitacion(5L, "301", "LIBRE");
        normal = habitacion(2L, "202", "LIBRE");
        original = registro(5L);
        original.setPrecio(new BigDecimal("30.00"));
        when(habitaciones.buscarParaActualizar(5L)).thenReturn(Optional.of(compartida));
        when(habitaciones.buscarParaActualizar(2L)).thenReturn(Optional.of(normal));
        when(registros.buscarParaActualizar(10L)).thenReturn(Optional.of(original));
        when(registros.save(any())).thenAnswer(i -> i.getArgument(0));
    }
    static Habitacion habitacion(Long id, String numero, String estado) {
        Habitacion h = new Habitacion(); h.setId(id); h.setNumero(numero);
        h.setEstado(estado); h.setCapacidad(2); h.setPrecio(new BigDecimal("30.00")); return h;
    }
    static Registro registro(Long id) {
        Registro r = new Registro(); r.setHabitacionId(id); r.setHuespedId(1L);
        r.setFechaIngreso(LocalDate.of(2026, 9, 14)); r.setHoraIngreso(LocalTime.NOON); return r;
    }
    static void salida(Registro r) {
        r.setFechaSalida(LocalDate.of(2026,9,15)); r.setHoraSalida(LocalTime.NOON);
    }
    void error(int codigo, Runnable action) {
        assertEquals(codigo, assertThrows(ResponseStatusException.class, action::run).getStatusCode().value());
    }

    @ParameterizedTest @CsvSource({"301,0,PARCIALMENTE OCUPADA", "303,1,PARCIALMENTE OCUPADA", "305,2,OCUPADA"})
    void ingresosHastaTres(String numero, long otros, String estado) {
        compartida.setNumero(numero);
        compartida.setEstado(otros > 0 ? "PARCIALMENTE OCUPADA" : "LIBRE");
        when(registros.contarActivos(5L,null)).thenReturn(otros);
        Registro nuevo = registro(5L); service.guardar(nuevo);
        assertEquals(estado, compartida.getEstado()); assertEquals(3, compartida.getCapacidad());
        assertEquals(new BigDecimal("30.00"), nuevo.getPrecio());
    }
    @Test void cuartoHuespedNoEscribe() {
        when(registros.contarActivos(5L,null)).thenReturn(3L);
        error(409, () -> service.guardar(registro(5L)));
        verify(registros,never()).save(any()); verify(habitaciones,never()).save(any());
    }
    @Test void mantenimientoNoAdmiteHuespedes() {
        compartida.setEstado("MANTENIMIENTO"); error(409, () -> service.guardar(registro(5L)));
    }
    @ParameterizedTest @CsvSource({"0,LIBRE", "1,PARCIALMENTE OCUPADA", "2,PARCIALMENTE OCUPADA"})
    void salidaRecalcula(long otros, String estado) {
        compartida.setEstado("OCUPADA"); when(registros.contarActivos(5L,10L)).thenReturn(otros);
        Registro datos=registro(5L); salida(datos); error(409, () -> service.actualizar(10L,datos));
        assertEquals("OCUPADA",compartida.getEstado()); assertEquals(new BigDecimal("30.00"),original.getPrecio());
    }
    @ParameterizedTest @CsvSource({"0,LIBRE", "1,PARCIALMENTE OCUPADA", "2,PARCIALMENTE OCUPADA"})
    void eliminarRecalcula(long otros, String estado) {
        compartida.setEstado("OCUPADA"); when(registros.contarActivos(5L,10L)).thenReturn(otros);
        error(409, () -> service.eliminar(10L)); verify(registros,never()).delete(any()); assertEquals("OCUPADA",compartida.getEstado());
    }
    @Test void editarPropioConTresOcupantesNoCuentaDosVeces() {
        compartida.setEstado("OCUPADA"); when(registros.contarActivos(5L,10L)).thenReturn(2L);
        service.actualizar(10L,registro(5L)); assertEquals("OCUPADA",compartida.getEstado());
    }
    @Test void fechaSinHoraSigueOcupando() {
        Registro datos=registro(5L); datos.setFechaSalida(LocalDate.now());
        error(409, () -> service.actualizar(10L,datos)); verify(registros,never()).save(any());
    }
    @Test void trasladoActualizaAmbasYBloqueaEnOrden() {
        compartida.setEstado("OCUPADA"); when(registros.contarActivos(5L,10L)).thenReturn(2L);
        service.actualizar(10L,registro(2L));
        assertEquals("PARCIALMENTE OCUPADA",compartida.getEstado()); assertEquals("OCUPADO",normal.getEstado());
        var orden=inOrder(habitaciones); orden.verify(habitaciones).buscarParaActualizar(2L);
        orden.verify(habitaciones).buscarParaActualizar(5L);
    }
    @Test void trasladoSinPlazaNoCambiaRegistro() {
        original.setHabitacionId(2L); when(registros.contarActivos(5L,10L)).thenReturn(3L);
        error(409, () -> service.actualizar(10L,registro(5L)));
        assertEquals(2L,original.getHabitacionId()); verify(registros,never()).save(any());
    }
    @Test void reaperturaNecesitaPlaza() {
        salida(original); when(registros.contarActivos(5L,10L)).thenReturn(3L);
        error(409, () -> service.actualizar(10L,registro(5L)));
    }
    @Test void normalAdmiteSoloUnoAunqueCapacidadSeaTres() {
        normal.setCapacidad(3); when(registros.contarActivos(2L,null)).thenReturn(1L);
        error(409, () -> service.guardar(registro(2L)));
    }
    @Test void normalConservaCicloOcupadoLibre() {
        original.setHabitacionId(2L); service.guardar(registro(2L)); assertEquals("OCUPADO",normal.getEstado());
        error(409, () -> service.eliminar(10L)); assertEquals("OCUPADO",normal.getEstado());
    }
    @Test void normalEditarOcupacionPropia() {
        normal.setEstado("OCUPADO"); original.setHabitacionId(2L);
        service.actualizar(10L,registro(2L)); assertEquals("OCUPADO",normal.getEstado());
    }
    @Test void normalNoLiberaOtraEstancia() {
        normal.setEstado("OCUPADO"); original.setHabitacionId(2L);
        when(registros.contarActivos(2L,10L)).thenReturn(1L);
        error(409, () -> service.eliminar(10L)); assertEquals("OCUPADO",normal.getEstado());
    }
    @Test void precioBaseReferenciaSeObtieneDelServidor() {
        Registro nuevo=registro(5L); nuevo.setPrecioBaseReferencia(BigDecimal.ONE);
        service.guardar(nuevo); assertEquals(compartida.getPrecio(),nuevo.getPrecioBaseReferencia());
    }
    @Test void tarifaDistintaExigeObservacion() {
        Registro nuevo=registro(5L); nuevo.setPrecio(BigDecimal.TEN); nuevo.setObservacionPrecio("  ");
        error(400, () -> service.guardar(nuevo)); verify(registros,never()).save(any());
    }
    @Test void tarifaTemporalNoCambiaBase() {
        Registro nuevo=registro(5L); nuevo.setPrecio(BigDecimal.TEN); nuevo.setObservacionPrecio(" Precio temporal ");
        service.guardar(nuevo); assertEquals(BigDecimal.TEN,nuevo.getPrecio());
        assertEquals("Precio temporal",nuevo.getObservacionPrecio()); assertEquals(new BigDecimal("30.00"),compartida.getPrecio());
    }
    @ParameterizedTest @CsvSource({"0", "-1"})
    void rechazaTarifaNoPositiva(String precio) {
        Registro nuevo=registro(5L); nuevo.setPrecio(new BigDecimal(precio)); nuevo.setObservacionPrecio("Autorizado");
        error(400, () -> service.guardar(nuevo));
    }
    @Test void editarPrecioExigeMotivoYActualizaReferencia() {
        Registro datos=registro(5L); datos.setPrecio(BigDecimal.TEN);
        error(400, () -> service.actualizar(10L,datos));
        datos.setObservacionPrecio("Autorizado por el casero"); service.actualizar(10L,datos);
        assertEquals(BigDecimal.TEN,original.getPrecio()); assertEquals(compartida.getPrecio(),original.getPrecioBaseReferencia());
    }
    @Test void cambioBaseNoRecalculaPrecioEnSalida() {
        original.setPrecioBaseReferencia(new BigDecimal("30.00")); original.setObservacionPrecio("Anterior");
        compartida.setPrecio(new BigDecimal("50.00"));
        Registro datos=registro(5L); salida(datos); datos.setPrecio(new BigDecimal("30"));
        datos.setPrecioBaseReferencia(BigDecimal.ONE); datos.setObservacionPrecio("Manipulado");
        error(409, () -> service.actualizar(10L,datos));
        assertEquals(new BigDecimal("30.00"),original.getPrecio());
        assertEquals(new BigDecimal("30.00"),original.getPrecioBaseReferencia());
        assertEquals("Anterior",original.getObservacionPrecio());
    }
    @Test void historicoNoAceptaCambioDePrecio() {
        salida(original); Registro datos=registro(5L); salida(datos); datos.setPrecio(BigDecimal.TEN);
        datos.setObservacionPrecio("Autorizado"); error(409, () -> service.actualizar(10L,datos));
        assertEquals(new BigDecimal("30.00"),original.getPrecio());
    }
    @Test void historicoSinReferenciaSeConserva() {
        salida(original); Registro datos=registro(5L); salida(datos); error(409, () -> service.actualizar(10L,datos));
        assertNull(original.getPrecioBaseReferencia()); assertEquals(new BigDecimal("30.00"),original.getPrecio());
    }
    @Test void falloDeRelacionProvocaRollback() {
        PlatformTransactionManager manager=mock(PlatformTransactionManager.class);
        TransactionStatus status=mock(TransactionStatus.class);
        when(manager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        TransactionInterceptor advice=new TransactionInterceptor(); advice.setTransactionManager(manager);
        advice.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        ProxyFactory factory=new ProxyFactory(service); factory.setProxyTargetClass(true); factory.addAdvice(advice);
        RegistroService proxy=(RegistroService)factory.getProxy();
        doThrow(new org.springframework.dao.DataIntegrityViolationException("boleta relacionada"))
                .when(registros).delete(original);
        assertThrows(ResponseStatusException.class, () -> proxy.eliminar(10L));
        verify(manager).rollback(status); verify(manager,never()).commit(any());
    }
}
