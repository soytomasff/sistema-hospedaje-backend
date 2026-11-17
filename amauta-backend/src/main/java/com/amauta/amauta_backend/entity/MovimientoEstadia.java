package com.amauta.amauta_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(indexes = @Index(name = "idx_movimiento_estadia_registro", columnList = "registro_id"))
public class MovimientoEstadia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "registro_id") private Long registroId;
    @Column(name = "habitacion_id") private Long habitacionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Registro registro;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Habitacion habitacion;
    private String tipo;
    private LocalDateTime momento;
    private String usuario;
    @Column(length = 1000) private String detalle;
}
