package com.inventario.inventory_engine.repository;

import com.inventario.inventory_engine.domain.model.Producto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio del catálogo de productos.
 * Permite buscar productos por su SKU y acceder a sus datos.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su código SKU único.
     * El SKU es como el identificador comercial del producto.
     */
    Optional<Producto> findBySku(String sku);
}

