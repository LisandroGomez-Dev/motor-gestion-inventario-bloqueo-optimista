package com.inventario.inventory_engine.domain.exception;

/**
 * Excepción de negocio para cuando un recurso no existe.
 * Por ejemplo: no hay inventario para un producto determinado.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

