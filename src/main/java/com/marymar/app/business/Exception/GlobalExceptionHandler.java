package com.marymar.app.business.Exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "Violación de integridad en la base de datos",
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex){

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex){

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                mensaje,
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(org.hibernate.exception.ConstraintViolationException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "El correo ya está registrado",
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                "Error interno del servidor",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex){

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex){

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(error);
    }
}

