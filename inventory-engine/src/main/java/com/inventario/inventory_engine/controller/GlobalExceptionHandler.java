package com.inventario.inventory_engine.controller;

import com.inventario.inventory_engine.domain.exception.RecursoNoEncontradoException;
import com.inventario.inventory_engine.domain.exception.StockInsuficienteException;
import com.inventario.inventory_engine.service.dto.ErrorResponse;
import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



/**
 * Clase global para manejar errores de la API.
 * En vez de que cada controlador tenga su propio try/catch, aquí centralizamos todas las respuestas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 Not Found.
     * Se usa cuando no existe un producto o un inventario asociado.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    /**
     * 400 Bad Request.
     * Se lanza cuando la regla de negocio dice que no puede descontar más stock del disponible.
     */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> manejarStockInsuficiente(StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    /**
     * 409 Conflict.
     * Ocurre cuando dos operaciones intentan modificar el mismo registro al mismo tiempo.
     * Spring detecta el conflicto por la versión @Version.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> manejarConflictoConcurrencia(OptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Conflicto de concurrencia: el stock fue modificado por otra transacción simultánea. Por favor, reintente."
                ));
    }

    /**
     * 400 Bad Request.
     * Se dispara cuando un cliente manda datos inválidos en el cuerpo de la petición.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Error de validación", errores));
    }

    /**
     * 500 Internal Server Error.
     * Para cualquier error inesperado que no esté previsto por las excepciones anteriores.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ocurrió un error inesperado en el servidor"));
    }
}

