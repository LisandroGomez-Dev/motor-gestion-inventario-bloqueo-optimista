package com.inventario.inventory_engine.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la operación de descuento de stock.
 * Representa la cantidad que se quiere retirar de inventario.
 */
public record DescontarStockRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad a descontar debe ser al menos 1")
        Integer cantidad
) {}


