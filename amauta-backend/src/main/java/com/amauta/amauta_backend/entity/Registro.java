package com.amauta.amauta_backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "registro", indexes = @Index(name = "idx_registro_salida_prevista", columnList = "salida_prevista"))
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "huesped_id")
    private Long huespedId;

    @Column(name = "habitacion_id")
    private Long habitacionId;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "hora_ingreso")
    private LocalTime horaIngreso;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    private String dni;

    private String estado;

    // Peru local time. Existing fechaSalida/horaSalida remain the confirmed exit.
    @Column(name = "salida_prevista")
    private LocalDateTime salidaPrevista;

    @Column(name = "aviso_salida_emitido")
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private Boolean avisoSalidaEmitido;

    public LocalDateTime getSalidaPrevista() { return salidaPrevista; }
    public void setSalidaPrevista(LocalDateTime value) { salidaPrevista = value; }
    public Boolean getAvisoSalidaEmitido() { return avisoSalidaEmitido; }
    public void setAvisoSalidaEmitido(Boolean value) { avisoSalidaEmitido = value; }

    @Column(name = "hora_entrada")
    private LocalDateTime horaEntrada;

    @Column(name = "nombre_huesped")
    private String nombreHuesped;

    @Column(name = "numero_cuarto")
    private String numeroCuarto;

    @Column(name = "precio_cuarto")
    private Double precioCuarto;

    private String procedencia;

    private BigDecimal precio;

    @Column(name = "precio_base_referencia", precision = 38, scale = 2)
    private BigDecimal precioBaseReferencia;

    @Column(name = "observacion_precio", columnDefinition = "text")
    private String observacionPrecio;

    public BigDecimal getPrecioBaseReferencia() { return precioBaseReferencia; }
    public void setPrecioBaseReferencia(BigDecimal precioBaseReferencia) {
        this.precioBaseReferencia = precioBaseReferencia;
    }
    public String getObservacionPrecio() { return observacionPrecio; }
    public void setObservacionPrecio(String observacionPrecio) {
        this.observacionPrecio = observacionPrecio;
    }

    public Registro() {}

    public Long getId() {
        return id;
    }

    public Long getHuespedId() {
        return huespedId;
    }

    public void setHuespedId(Long huespedId) {
        this.huespedId = huespedId;
    }

    public Long getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(Long habitacionId) {
        this.habitacionId = habitacionId;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalTime getHoraIngreso() {
        return horaIngreso;
    }

    public void setHoraIngreso(LocalTime horaIngreso) {
        this.horaIngreso = horaIngreso;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalDateTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getNombreHuesped() {
        return nombreHuesped;
    }

    public void setNombreHuesped(String nombreHuesped) {
        this.nombreHuesped = nombreHuesped;
    }

    public String getNumeroCuarto() {
        return numeroCuarto;
    }

    public void setNumeroCuarto(String numeroCuarto) {
        this.numeroCuarto = numeroCuarto;
    }

    public Double getPrecioCuarto() {
        return precioCuarto;
    }

    public void setPrecioCuarto(Double precioCuarto) {
        this.precioCuarto = precioCuarto;
    }

    public String getProcedencia() {
        return procedencia;
    }

    public void setProcedencia(String procedencia) {
        this.procedencia = procedencia;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
