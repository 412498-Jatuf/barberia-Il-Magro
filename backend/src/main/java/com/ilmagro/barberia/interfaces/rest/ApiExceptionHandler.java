package com.ilmagro.barberia.interfaces.rest;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.agenda.TurnoNoDisponibleException;
import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Traduce las excepciones de dominio/aplicacion a respuestas HTTP con un mensaje legible. */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(String mensaje, Map<String, String> campos) {
        ErrorResponse(String mensaje) {
            this(mensaje, Map.of());
        }
    }

    @ExceptionHandler(TurnoNoDisponibleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse turnoNoDisponible(TurnoNoDisponibleException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noEncontrado(RecursoNoEncontradoException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(DatoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse datoInvalido(DatoInvalidoException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return new ErrorResponse("Revisa los datos ingresados", campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse requestMalFormado(Exception e) {
        return new ErrorResponse("La solicitud tiene un formato invalido");
    }
}
