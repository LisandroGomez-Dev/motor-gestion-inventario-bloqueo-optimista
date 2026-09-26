
package com.inventario.inventory_engine.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

/**
 * Entidad que representa un producto dentro del catálogo del sistema.
 * Contiene la información básica del artículo y su precio unitario.
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    /**
     * Identificador único del producto.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código único del producto, usado como referencia comercial o interna.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    /**
     * Nombre legible del producto, visible para operaciones del negocio.
     */
    @Column(nullable = false, length = 255)
    private String nombre;

    /**
     * Descripción detallada del producto, usada como información adicional.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Precio unitario del producto, con dos decimales para el manejo monetario.
     */
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;
}

