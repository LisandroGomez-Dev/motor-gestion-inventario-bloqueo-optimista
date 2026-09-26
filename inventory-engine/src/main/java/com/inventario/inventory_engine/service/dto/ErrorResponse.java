package com.inventario.inventory_engine.service.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO estándar para devolver errores desde la API.
 * Así el frontend puede mostrar mensajes claros al usuario.
 */
public record ErrorResponse(
        int estado,
        String mensaje,
        LocalDateTime timestamp,
        List<String> detalles
) {
    public ErrorResponse(int estado, String mensaje) {
        this(estado, mensaje, LocalDateTime.now(), List.of());
    }

    public ErrorResponse(int estado, String mensaje, List<String> detalles) {
        this(estado, mensaje, LocalDateTime.now(), detalles);
    }
}



