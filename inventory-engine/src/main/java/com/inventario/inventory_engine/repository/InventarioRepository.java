package com.inventario.inventory_engine.repository;

import com.inventario.inventory_engine.domain.model.Inventario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repositorio del inventario.
 * Aquí se definen consultas directas a la base de datos para buscar y actualizar stock.
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    /**
     * Busca el registro de inventario asociado a un producto.
     * Si un producto no tiene inventario, devuelve un Optional vacío.
     */
    Optional<Inventario> findByProductoId(Long productoId);

    /**
     * Actualiza el stock de un producto y reinicia la versión.
     * Esta query se usa cuando se quiere volver a un estado conocido del inventario.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Inventario i SET i.version = 0, i.stockDisponible = :stock WHERE i.producto.id = :productoId")
    void resetearVersionYStock(@Param("productoId") Long productoId, @Param("stock") Integer stock);
}

