CREATE TABLE producto (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio_unitario NUMERIC(12, 2) NOT NULL
);

CREATE TABLE inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL UNIQUE,
    stock_disponible INT NOT NULL DEFAULT 0,
    stock_reservado INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT chk_stock_disponible CHECK (stock_disponible >= 0),
    CONSTRAINT chk_stock_reservado CHECK (stock_reservado >= 0)
);

CREATE INDEX idx_producto_sku ON producto(sku);