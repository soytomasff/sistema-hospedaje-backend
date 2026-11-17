package com.amauta.amauta_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    private String nombre;
    private String categoria;
    private Integer stock;
    private Integer stockMinimo;
    private BigDecimal precio;
    private String descripcion;
    private String unidad;
    private String estado;
    private String ubicacion;
}