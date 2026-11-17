package com.amauta.amauta_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Getter @Setter @NoArgsConstructor
public class ConfiguracionEstadia {
    @Id private Long id = 1L;
    private LocalTime horaSalida = LocalTime.of(11, 0);
    @Version private Long version;
}
