package com.inventario.inventory_engine.domain.exception;

/**
 * Excepción de negocio para validar que no se venda más stock del disponible.
 */
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}

