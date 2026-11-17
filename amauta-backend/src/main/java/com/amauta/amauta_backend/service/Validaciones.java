package com.amauta.amauta_backend.service;

import java.math.BigDecimal;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Validaciones {
    private Validaciones() {}
    public static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
    public static void texto(String valor, String nombre) {
        exigir(valor != null && !valor.isBlank() && valor.length() <= 255, nombre + " es obligatorio (maximo 255 caracteres)");
    }
    public static void metodoPago(String metodo) {
        exigir(metodo != null && Set.of("EFECTIVO", "YAPE", "PLIN", "TARJETA", "TRANSFERENCIA")
                .contains(metodo.toUpperCase()), "Selecciona un metodo de pago valido");
    }
    public static void noNegativo(Integer valor, String nombre) {
        exigir(valor != null && valor >= 0, nombre + " no puede ser negativo");
    }
    public static void precio(BigDecimal valor) {
        exigir(valor != null && valor.signum() >= 0, "El precio no puede ser negativo");
    }
}
