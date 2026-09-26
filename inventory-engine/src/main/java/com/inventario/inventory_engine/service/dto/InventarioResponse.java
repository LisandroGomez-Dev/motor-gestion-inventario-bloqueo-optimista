package com.inventario.inventory_engine.service.dto;

/**
 * DTO de salida del inventario.
 * Es la respuesta que la API devuelve al frontend después de una operación.
 */
public record InventarioResponse(
        Long productoId,
        String sku,
        Integer stockDisponible,
        Integer stockReservado,
        Long version
) {}

