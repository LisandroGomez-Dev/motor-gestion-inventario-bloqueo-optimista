package com.inventario.inventory_engine.service;

import com.inventario.inventory_engine.domain.exception.RecursoNoEncontradoException;
import com.inventario.inventory_engine.domain.exception.StockInsuficienteException;
import com.inventario.inventory_engine.domain.model.Inventario;
import com.inventario.inventory_engine.repository.InventarioRepository;
import com.inventario.inventory_engine.service.dto.AjustarStockRequest;
import com.inventario.inventory_engine.service.dto.DescontarStockRequest;
import com.inventario.inventory_engine.service.dto.InventarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Clase central de la lógica de negocio del inventario.
 * Aquí se definen las reglas que validan si es posible descontar o ajustar stock.
 */

@Service
@RequiredArgsConstructor
public class InventarioService {

    // Repositorio que accede directamente a la tabla de inventario en la base de datos.
    private final InventarioRepository inventarioRepository;

    /**
     * Descuenta una cantidad determinada del stock disponible de un producto.
     * Si el stock no alcanza, lanza una excepción de negocio.
     */
    @Transactional
    public InventarioResponse descontarStock(DescontarStockRequest request) {
        // 1. Buscamos el inventario asociado al producto.
        Inventario inventario = inventarioRepository.findByProductoId(request.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Inventario no encontrado para el producto con ID: " + request.productoId()));

        // 2. Validamos que haya suficiente stock antes de descontar.
        if (inventario.getStockDisponible() < request.cantidad()) {
            throw new StockInsuficienteException(
                    String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                            inventario.getStockDisponible(), request.cantidad()));
        }

        // 3. El stock disponible se reduce en la cantidad pedida.
        inventario.setStockDisponible(inventario.getStockDisponible() - request.cantidad());

        // 4. Guardamos el cambio.
        // Hibernate se encarga de manejar @Version y detectar conflictos concurrentes.
        Inventario guardado = inventarioRepository.save(inventario);

        // 5. Devolvemos la respuesta final.
        return mapToResponse(guardado);
    }

    /**
     * Ajusta el stock de un producto a un valor específico.
     * En este caso se usa para reponer o fijar el stock manualmente.
     */
    @Transactional
    public InventarioResponse ajustarStock(AjustarStockRequest request) {
        // Restauramos el stock exacto solicitado y también reiniciamos la versión.
        // Esto sirve para forzar un estado conocido del registro.
        inventarioRepository.resetearVersionYStock(request.productoId(), request.nuevoStock());

        // Luego volvemos a leer el registro actualizado para devolver el estado real.
        Inventario inventario = inventarioRepository.findByProductoId(request.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Inventario no encontrado para el producto con ID: " + request.productoId()));

        return new InventarioResponse(
                inventario.getProducto().getId(),
                inventario.getProducto().getSku(),
                inventario.getStockDisponible(),
                inventario.getStockReservado(),
                inventario.getVersion()
        );
    }

    /**
     * Obtiene el estado actual del inventario para un producto dado.
     */
    @Transactional(readOnly = true)
    public InventarioResponse obtenerPorProductoId(Long productoId) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Inventario no encontrado para el producto con ID: " + productoId));

        return mapToResponse(inventario);
    }

    /**
     * Convierte una entidad Inventario en un DTO para devolverlo por HTTP.
     * Esto evita exponer internamente la entidad completa.
     */
    private InventarioResponse mapToResponse(Inventario inventario) {
        return new InventarioResponse(
                inventario.getProducto().getId(),
                inventario.getProducto().getSku(),
                inventario.getStockDisponible(),
                inventario.getStockReservado(),
                inventario.getVersion()
        );
    }


}



















