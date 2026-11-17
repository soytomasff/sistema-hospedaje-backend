package com.amauta.amauta_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "huesped")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DATOS PERSONALES
    private String nombre;

    private String apellido;

    @Column(unique = true)
    private String dni;

    // INFORMACIÓN ADICIONAL
    private String procedencia;

    private String pais;

    private String profesion;

    private String telefono;
}