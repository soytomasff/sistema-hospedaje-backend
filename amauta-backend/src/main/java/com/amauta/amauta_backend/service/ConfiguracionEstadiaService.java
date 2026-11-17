package com.amauta.amauta_backend.service;
import com.amauta.amauta_backend.entity.ConfiguracionEstadia;
import com.amauta.amauta_backend.repository.ConfiguracionEstadiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalTime;

@Service
public class ConfiguracionEstadiaService {
    private final ConfiguracionEstadiaRepository repository;
    private final com.amauta.amauta_backend.repository.MovimientoEstadiaRepository movimientos;
    private final java.time.Clock clock;
    public ConfiguracionEstadiaService(ConfiguracionEstadiaRepository repository,
            com.amauta.amauta_backend.repository.MovimientoEstadiaRepository movimientos, java.time.Clock clock) {
        this.repository = repository; this.movimientos = movimientos; this.clock = clock;
    }
    @Transactional(readOnly = true)
    public LocalTime horaSalida() { return repository.findById(1L).map(ConfiguracionEstadia::getHoraSalida).orElse(LocalTime.of(11, 0)); }
    @Transactional
    public LocalTime cambiar(LocalTime hora) {
        if (hora == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica la hora de salida");
        var config = repository.findById(1L).orElseGet(ConfiguracionEstadia::new);
        var anterior = config.getHoraSalida();
        config.setHoraSalida(hora.withSecond(0).withNano(0));
        repository.save(config);
        if (!java.util.Objects.equals(anterior, config.getHoraSalida())) {
            var movimiento = new com.amauta.amauta_backend.entity.MovimientoEstadia();
            movimiento.setTipo("HORA_SALIDA_PREDETERMINADA");
            movimiento.setMomento(java.time.LocalDateTime.ofInstant(clock.instant(), java.time.ZoneId.of("America/Lima")));
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            movimiento.setUsuario(auth == null ? "SISTEMA" : auth.getName());
            movimiento.setDetalle(anterior + " -> " + config.getHoraSalida()); movimientos.save(movimiento);
        }
        return config.getHoraSalida();
    }
}
