package com.amauta.amauta_backend.service;

import com.amauta.amauta_backend.entity.Habitacion;
import com.amauta.amauta_backend.entity.Registro;
import com.amauta.amauta_backend.repository.HabitacionRepository;
import com.amauta.amauta_backend.repository.RegistroRepository;
import com.amauta.amauta_backend.repository.HuespedRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.math.BigDecimal;

@Service
public class RegistroService {
    private final RegistroRepository registros;
    private final HabitacionRepository habitaciones;
    private final HuespedRepository huespedes;
    private final CheckoutService checkout;

    public RegistroService(RegistroRepository registros, HabitacionRepository habitaciones, HuespedRepository huespedes, CheckoutService checkout) {
        this.registros = registros;
        this.habitaciones = habitaciones;
        this.huespedes = huespedes;
        this.checkout = checkout;
    }

    @Transactional
    public List<Registro> listar() {
        checkout.revisarPendientes();
        return registros.findAll();
    }

    @Transactional
    public Registro guardar(Registro registro) {
        validarFechas(registro);
        if (registro.getId() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una estadia nueva no debe incluir ID");
        checkout.prepararIngreso(registro);
        Habitacion habitacion = bloquearHabitacion(registro.getHabitacionId());
        completarDatos(registro, habitacion);
        aplicarPrecio(registro, registro.getPrecio(), registro.getObservacionPrecio(), habitacion);
        long otros = registros.contarActivos(habitacion.getId(), null);
        if (activo(registro)) {
            validarDisponible(habitacion, otros, false);
        }
        HabitacionService.actualizarEstado(habitacion, otros + (activo(registro) ? 1 : 0));
        habitaciones.save(habitacion);
        Registro guardado = registros.save(registro);
        checkout.movimiento(guardado, "INGRESO", usuarioActual(), "Ingreso registrado");
        return guardado;
    }

    @Transactional
    public Registro actualizar(Long id, Registro datos) {
        validarFechas(datos);
        Registro registro = buscarRegistro(id);
        if (!activo(registro)) throw new ResponseStatusException(HttpStatus.CONFLICT, "El historial de una estadia finalizada no se modifica");
        if (!Objects.equals(registro.getFechaSalida(), datos.getFechaSalida()) || !Objects.equals(registro.getHoraSalida(), datos.getHoraSalida()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Confirma la salida desde check-out");
        // Legacy clients may omit the planned exit on an ordinary edit.
        var salidaPrevistaAnterior = registro.getSalidaPrevista();
        var salidaPrevistaNueva = datos.getSalidaPrevista() == null
                ? salidaPrevistaAnterior : datos.getSalidaPrevista();
        Long anteriorId = registro.getHabitacionId();
        Long nuevaId = datos.getHabitacionId();
        if (nuevaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una habitacion");
        }
        // Orden estable para que dos cambios de habitacion no bloqueen en orden inverso.
        TreeSet<Long> ids = new TreeSet<>();
        if (anteriorId != null) ids.add(anteriorId);
        ids.add(nuevaId);
        Map<Long, Habitacion> bloqueadas = new HashMap<>();
        for (Long habitacionId : ids) {
            bloqueadas.put(habitacionId, bloquearHabitacion(habitacionId));
        }
        boolean mismaHabitacion = Objects.equals(anteriorId, nuevaId);
        boolean estabaActivo = activo(registro);
        Habitacion nueva = bloqueadas.get(nuevaId);
        long otrosNueva = registros.contarActivos(nuevaId, id);
        long otrosAnterior = mismaHabitacion || anteriorId == null ? 0
                : registros.contarActivos(anteriorId, id);
        if (activo(datos)) {
            validarDisponible(nueva, otrosNueva, mismaHabitacion && estabaActivo);
        }

        // Una edicion ordinaria conserva el precio y su referencia historica.
        boolean cambiaPrecio = datos.getPrecio() != null && (registro.getPrecio() == null
                || datos.getPrecio().compareTo(registro.getPrecio()) != 0);
        if (cambiaPrecio) {
            if (!estabaActivo) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede cambiar el precio de un registro cerrado");
            }
            aplicarPrecio(registro, datos.getPrecio(), datos.getObservacionPrecio(), nueva);
        }

        registro.setHuespedId(datos.getHuespedId());
        registro.setHabitacionId(nuevaId);
        registro.setFechaIngreso(datos.getFechaIngreso());
        registro.setHoraIngreso(datos.getHoraIngreso());
        registro.setFechaSalida(datos.getFechaSalida());
        registro.setHoraSalida(datos.getHoraSalida());
        registro.setSalidaPrevista(salidaPrevistaNueva);
        completarDatos(registro, nueva);
        if (registro.getSalidaPrevista() != null && registro.getSalidaPrevista().isBefore(registro.getFechaIngreso().atTime(registro.getHoraIngreso())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ingreso no puede ser posterior a la salida prevista");
        Registro actualizado = registros.save(registro);

        if (!mismaHabitacion && anteriorId != null) {
            bloqueadas.get(anteriorId).setLimpiezaPendienteDesde(checkout.ahora());
            guardarEstado(bloqueadas.get(anteriorId), otrosAnterior);
        }
        guardarEstado(nueva, otrosNueva + (activo(registro) ? 1 : 0));
        checkout.movimiento(registro, "ESTADIA_EDITADA", usuarioActual(), "Habitacion: " + anteriorId + " -> " + nuevaId);
        if (!Objects.equals(salidaPrevistaAnterior, salidaPrevistaNueva))
            checkout.movimiento(registro, "SALIDA_REPROGRAMADA", usuarioActual(),
                    salidaPrevistaAnterior + " -> " + salidaPrevistaNueva);
        return actualizado;
    }

    @Transactional
    public void eliminar(Long id) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Las estadias se conservan en el historial. Confirma la salida sin eliminar el registro");
    }

    private String usuarioActual() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "SISTEMA" : auth.getName();
    }

    private Registro buscarRegistro(Long id) {
        return registros.buscarParaActualizar(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
    }

    private Habitacion bloquearHabitacion(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una habitacion");
        }
        return habitaciones.buscarParaActualizar(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Habitacion no encontrada"));
    }

    private boolean activo(Registro registro) {
        return registro.getFechaSalida() == null || registro.getHoraSalida() == null;
    }

    private void validarFechas(Registro registro) {
        if (registro.getFechaIngreso() == null || registro.getHoraIngreso() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica fecha y hora de ingreso");
        }
        if (registro.getHoraSalida() != null && registro.getFechaSalida() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica la fecha de salida");
        }
        if (registro.getFechaSalida() != null && (registro.getFechaSalida().isBefore(registro.getFechaIngreso())
                || (registro.getFechaSalida().equals(registro.getFechaIngreso())
                && registro.getHoraSalida() != null && registro.getHoraSalida().isBefore(registro.getHoraIngreso())))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La salida no puede ser anterior al ingreso");
        }
    }

    private void completarDatos(Registro registro, Habitacion habitacion) {
        if (registro.getHuespedId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un huesped");
        }
        var huesped = huespedes.findById(registro.getHuespedId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Huesped no encontrado"));
        registro.setNombreHuesped(huesped.getNombre() + " " + huesped.getApellido());
        registro.setDni(huesped.getDni());
        registro.setProcedencia(huesped.getProcedencia());
        registro.setNumeroCuarto(habitacion.getNumero());
    }

    private void validarDisponible(Habitacion habitacion, long otros, boolean ocupacionPropia) {
        String estado = habitacion.getEstado();
        if (habitacion.getLimpiezaPendienteDesde() != null && !ocupacionPropia)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La habitacion tiene limpieza pendiente");
        boolean disponible = "LIBRE".equalsIgnoreCase(estado) || "DISPONIBLE".equalsIgnoreCase(estado);
        if (HabitacionService.esCompartida(habitacion)) {
            if (otros >= 3) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La habitacion no tiene camas disponibles");
            }
            if (disponible || "PARCIALMENTE OCUPADA".equalsIgnoreCase(estado)
                    || "OCUPADA".equalsIgnoreCase(estado) || "OCUPADO".equalsIgnoreCase(estado)) return;
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La habitacion no esta disponible");
        }
        if (otros > 0
                || (!disponible && !(ocupacionPropia && ("OCUPADO".equalsIgnoreCase(estado) || "OCUPADA".equalsIgnoreCase(estado))))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La habitacion no esta disponible");
        }
    }

    private void guardarEstado(Habitacion habitacion, long activos) {
        HabitacionService.actualizarEstado(habitacion, activos);
        habitaciones.save(habitacion);
    }

    private void aplicarPrecio(Registro registro, BigDecimal solicitado, String observacion,
                               Habitacion habitacion) {
        BigDecimal base = habitacion.getPrecio();
        BigDecimal aplicado = solicitado == null ? base : solicitado;
        if (base == null || base.signum() <= 0 || aplicado == null || aplicado.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que cero");
        }
        if (aplicado.compareTo(base) != 0 && (observacion == null || observacion.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar una observacion para un precio distinto del precio base");
        }
        registro.setPrecio(aplicado);
        registro.setPrecioBaseReferencia(base);
        registro.setObservacionPrecio(observacion == null || observacion.isBlank() ? null : observacion.trim());
    }
}
