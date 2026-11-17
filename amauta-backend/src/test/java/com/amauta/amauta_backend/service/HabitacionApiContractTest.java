package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.controller.HabitacionController;
import com.amauta.amauta_backend.controller.RegistroController;
import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.exception.ApiExceptionHandler;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.RegistroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HabitacionApiContractTest {
    @ParameterizedTest
    @CsvSource({"301,0,3", "301,1,2", "303,2,1", "305,3,0", "303,0,3", "305,0,3"})
    void calculaAlConsultarSinGuardar(String numero, long activos, long esperadas) throws Exception {
        HabitacionRepository habitaciones=mock(HabitacionRepository.class);
        RegistroRepository registros=mock(RegistroRepository.class);
        Habitacion h=RegistroCompartidoServiceTest.habitacion(5L,numero,"LIBRE"); h.setCapacidad(3);
        when(habitaciones.findAll()).thenReturn(List.of(h));
        when(registros.contarActivos(5L,null)).thenReturn(activos);
        var mvc=MockMvcBuilders.standaloneSetup(new HabitacionController(habitaciones,
                new HabitacionService(habitaciones,registros,java.time.Clock.systemUTC()))).build();
        mvc.perform(get("/api/habitaciones")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].camasDisponibles").value(esperadas));
        assertEquals("LIBRE",h.getEstado()); verify(habitaciones,never()).save(any());
        verify(registros,never()).save(any());
    }
    @Test void normalDevuelveNullSinContar() throws Exception {
        HabitacionRepository habitaciones=mock(HabitacionRepository.class);
        RegistroRepository registros=mock(RegistroRepository.class);
        Habitacion h=RegistroCompartidoServiceTest.habitacion(2L,"202","LIBRE");
        h.setCamasDisponibles(99L); when(habitaciones.findAll()).thenReturn(List.of(h));
        var resultado=new HabitacionService(habitaciones,registros,java.time.Clock.systemUTC()).listar();
        var json=JsonMapper.builder().build().readTree(JsonMapper.builder().build().writeValueAsString(resultado));
        assertTrue(json.get(0).has("camasDisponibles")); assertTrue(json.get(0).get("camasDisponibles").isNull());
        verify(registros,never()).contarActivos(any(),any());
    }
    @Test void recalculaEnCadaListado() {
        HabitacionRepository habitaciones=mock(HabitacionRepository.class);
        RegistroRepository registros=mock(RegistroRepository.class);
        Habitacion h=RegistroCompartidoServiceTest.habitacion(5L,"301","LIBRE"); h.setCapacidad(3);
        when(habitaciones.findAll()).thenReturn(List.of(h));
        when(registros.contarActivos(5L,null)).thenReturn(0L,2L);
        HabitacionService service=new HabitacionService(habitaciones,registros,java.time.Clock.systemUTC());
        assertEquals(3L,service.listar().getFirst().getCamasDisponibles());
        assertEquals(1L,service.listar().getFirst().getCamasDisponibles());
    }
    @ParameterizedTest @ValueSource(strings={"POST","PUT"})
    void clienteNoPuedeAsignarCamas(String metodo) throws Exception {
        HabitacionRepository habitaciones=mock(HabitacionRepository.class);
        HabitacionService service=mock(HabitacionService.class);
        when(service.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(service.actualizar(eq(5L),any())).thenAnswer(i -> i.getArgument(1));
        var mvc=MockMvcBuilders.standaloneSetup(new HabitacionController(habitaciones,service)).build();
        var request="POST".equals(metodo) ? post("/api/habitaciones") : put("/api/habitaciones/5");
        mvc.perform(request.contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":\"301\",\"capacidad\":3,\"camasDisponibles\":99}"))
                .andExpect(status().isOk());
        var captor=org.mockito.ArgumentCaptor.forClass(Habitacion.class);
        if ("POST".equals(metodo)) verify(service).guardar(captor.capture());
        else verify(service).actualizar(eq(5L),captor.capture());
        assertNull(captor.getValue().getCamasDisponibles());
    }
    @ParameterizedTest
    @CsvSource({"409,La habitacion no tiene camas disponibles",
            "400,Debe indicar una observacion para un precio distinto del precio base"})
    void exponeMensajeYEstadoSinBaseDeDatos(int codigo,String mensaje) throws Exception {
        RegistroService service=mock(RegistroService.class);
        when(service.guardar(any())).thenThrow(new ResponseStatusException(HttpStatus.valueOf(codigo),mensaje));
        var mvc=MockMvcBuilders.standaloneSetup(new RegistroController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is(codigo))
                .andExpect(content().json("{\"message\":\""+mensaje+"\"}"));
    }
}
