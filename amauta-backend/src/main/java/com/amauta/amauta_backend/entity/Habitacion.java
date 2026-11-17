package com.amauta.amauta_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Entity
@Table(name = "habitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo; // Ej: HAB-101

    private String numero; // Ej: 101
    private String tipo;   // simple, doble, matrimonial
    private Integer capacidad;

    private BigDecimal precio;

    private String estado; // DISPONIBLE, OCUPADO, MANTENIMIENTO

    @Column(name = "limpieza_pendiente_desde")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private java.time.LocalDateTime limpiezaPendienteDesde;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String estadoOperativo;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long camasDisponibles;
}
