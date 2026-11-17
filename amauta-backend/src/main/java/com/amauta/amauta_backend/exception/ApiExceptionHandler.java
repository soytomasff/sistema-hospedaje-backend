package com.amauta.amauta_backend.exception;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejar(ResponseStatusException exception) {
        String mensaje = exception.getReason() == null ? "No se pudo completar la operacion" : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode()).headers(exception.getHeaders()).body(Map.of("message", mensaje));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException exception) {
        return ResponseEntity.status(409).body(Map.of("message", "Hay un código o documento duplicado, o el registro tiene información relacionada."));
    }
    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class, java.time.DateTimeException.class})
    public ResponseEntity<Map<String, String>> formato(Exception exception) {
        String message = exception.getClass() == IllegalArgumentException.class && exception.getMessage() != null
                ? exception.getMessage() : "Revisa los datos y el formato de los campos.";
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
