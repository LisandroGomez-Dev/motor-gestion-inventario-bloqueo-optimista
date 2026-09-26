package com.inventario.inventory_engine.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para cambiar el stock manualmente de un producto.
 * Se usa cuando la UI quiere fijar un valor directo del inventario.
 */
public record AjustarStockRequest(
    @NotNull(message = "El ID del producto es obligatorio")
    Long productoId,

    @NotNull(message = "El nuevo stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    Integer nuevoStock
) {}

