package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.RegistroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Set;
import java.util.List;

@Service
public class HabitacionService {
    private static final Set<String> COMPARTIDAS = Set.of("301", "303", "305");
    private final HabitacionRepository habitaciones;
    private final RegistroRepository registros;
    private final java.time.Clock clock;

    public HabitacionService(HabitacionRepository habitaciones, RegistroRepository registros, java.time.Clock clock) {
        this.habitaciones = habitaciones;
        this.registros = registros;
        this.clock = clock;
    }

    public static boolean esCompartida(Habitacion habitacion) {
        return habitacion.getNumero() != null && COMPARTIDAS.contains(habitacion.getNumero());
    }

    @Transactional(readOnly = true)
    public List<Habitacion> listar() {
        List<Habitacion> lista = habitaciones.findAll();
        for (Habitacion habitacion : lista) {
            habitacion.setCamasDisponibles(esCompartida(habitacion) && habitacion.getCapacidad() != null
                    ? (habitacion.getLimpiezaPendienteDesde() != null ? 0 : Math.max(0, habitacion.getCapacidad() - registros.contarActivos(habitacion.getId(), null)))
                    : null);
            var ahora = java.time.LocalDateTime.ofInstant(clock.instant(), java.time.ZoneId.of("America/Lima"));
            habitacion.setEstadoOperativo(registros.contarSalidasVencidas(habitacion.getId(), ahora) > 0
                    ? "SALIDA PENDIENTE" : habitacion.getEstado());
        }
        return lista;
    }

    // El llamador debe tener bloqueada la habitacion dentro de su transaccion.
    public static void actualizarEstado(Habitacion habitacion, long activos) {
        if (activos == 0 && habitacion.getLimpiezaPendienteDesde() != null) {
            if (!"MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) habitacion.setEstado("PENDIENTE DE LIMPIEZA");
            return;
        }
        if (esCompartida(habitacion)) {
            habitacion.setCapacidad(3);
            if (!"MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
                habitacion.setEstado(activos == 0 ? "LIBRE"
                        : activos < 3 ? "PARCIALMENTE OCUPADA" : "OCUPADA");
            }
        } else if (activos > 0) {
            if (!"MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
                habitacion.setEstado("OCUPADO");
            }
        } else if ("OCUPADO".equalsIgnoreCase(habitacion.getEstado())) {
            habitacion.setEstado("LIBRE");
        }
    }

    @Transactional
    public Habitacion guardar(Habitacion habitacion) {
        if (habitacion.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una habitacion nueva no debe incluir ID");
        }
        if (!Set.of("LIBRE", "DISPONIBLE", "MANTENIMIENTO").contains(String.valueOf(habitacion.getEstado()).toUpperCase(java.util.Locale.ROOT))) {
            if (!esCompartida(habitacion)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Una habitacion nueva debe estar libre o en mantenimiento");
        }
        if (esCompartida(habitacion)) actualizarEstado(habitacion, 0);
        return habitaciones.save(habitacion);
    }

    @Transactional
    public Habitacion actualizar(Long id, Habitacion datos) {
        Habitacion habitacion = habitaciones.buscarParaActualizar(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Habitacion no encontrada"));
        if (!Objects.equals(habitacion.getNumero(), datos.getNumero())
                && (esCompartida(habitacion) || esCompartida(datos))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cambiar el numero de una habitacion hacia o desde las compartidas");
        }
        habitacion.setCodigo(datos.getCodigo());
        habitacion.setNumero(datos.getNumero());
        habitacion.setTipo(datos.getTipo());
        habitacion.setPrecio(datos.getPrecio());
        long activos = registros.contarActivos(id, null);
        if (habitacion.getLimpiezaPendienteDesde() != null && !Objects.equals(habitacion.getEstado(), datos.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Confirma la limpieza desde la accion especifica");
        if (activos == 0 && habitacion.getLimpiezaPendienteDesde() == null
                && !Set.of("LIBRE", "DISPONIBLE", "MANTENIMIENTO").contains(String.valueOf(datos.getEstado()).toUpperCase(java.util.Locale.ROOT)))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La ocupacion se gestiona desde las estadias");
        if (activos > 0 && "MANTENIMIENTO".equalsIgnoreCase(datos.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede poner en mantenimiento una habitacion con huespedes activos");
        if (esCompartida(habitacion)) {
            if ("MANTENIMIENTO".equalsIgnoreCase(datos.getEstado()) && activos > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede poner en mantenimiento una habitacion con huespedes activos");
            }
            habitacion.setEstado(datos.getEstado());
            actualizarEstado(habitacion, activos);
        } else {
            habitacion.setCapacidad(datos.getCapacidad());
            habitacion.setEstado(datos.getEstado());
            actualizarEstado(habitacion, activos);
        }
        return habitaciones.save(habitacion);
    }
}
