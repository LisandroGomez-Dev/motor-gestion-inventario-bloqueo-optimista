package com.inventario.inventory_engine.domain.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa el inventario de un producto.
 * Guarda la cantidad disponible, la cantidad reservada y la versión para bloqueo optimista.
 */
@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    /**
     * Identificador único del registro de inventario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación con el producto asociado.
     * Un producto tiene un único registro de inventario.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    /**
     * Cantidad de unidades realmente disponibles para vender o consumir.
     */
    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible;

    /**
     * Cantidad de unidades reservadas para operaciones pendientes.
     */
    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado;

    /**
     * Campo de versión para activar control de concurrencia optimista.
     * Si otra transacción modifica el mismo registro, la versión cambia y se detecta el conflicto.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
