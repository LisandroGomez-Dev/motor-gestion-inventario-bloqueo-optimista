package com.inventario.inventory_engine.controller;

import com.inventario.inventory_engine.service.InventarioService;
import com.inventario.inventory_engine.service.dto.AjustarStockRequest;
import com.inventario.inventory_engine.service.dto.DescontarStockRequest;
import com.inventario.inventory_engine.service.dto.InventarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints de gestión y concurrencia del inventario.
 */
@RestController
@RequestMapping("/api/v1/inventario")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    /**
     * GET /api/v1/inventario/{productoId}
     * Consulta el estado actual de inventario, incluyendo stock y versión JPA.
     */
    @GetMapping("/{productoId}")
    public ResponseEntity<InventarioResponse> obtenerInventario(@PathVariable("productoId") Long productoId) {
        return ResponseEntity.ok(inventarioService.obtenerPorProductoId(productoId));
    }

    /**
     * POST /api/v1/inventario/descontar
     * Ejecuta el descuento de stock bajo validación de versión optimista (@Version).
     */
    @PostMapping("/descontar")
    public ResponseEntity<InventarioResponse> descontarStock(@Valid @RequestBody DescontarStockRequest request) {
        return ResponseEntity.ok(inventarioService.descontarStock(request));
    }

    /**
     * PUT /api/v1/inventario/stock
     * Ajusta manualmente el stock y restablece el estado para pruebas.
     */
    @PutMapping("/stock")
    public ResponseEntity<InventarioResponse> ajustarStock(@Valid @RequestBody AjustarStockRequest request) {
        return ResponseEntity.ok(inventarioService.ajustarStock(request));
    }
}

