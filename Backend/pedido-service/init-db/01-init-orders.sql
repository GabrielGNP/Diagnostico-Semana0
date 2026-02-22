CREATE DATABASE orders_db;

CREATE ROLE pedido_user WITH LOGIN PASSWORD 'pedido_pass';

GRANT ALL PRIVILEGES ON DATABASE orders_db TO pedido_user;

\connect orders_db

GRANT ALL ON SCHEMA public TO pedido_user;
ALTER SCHEMA public OWNER TO pedido_user;

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    id_user INTEGER NOT NULL,
    state VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO pedido_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO pedido_user;

-- Seed data: pedidos de ejemplo
INSERT INTO orders (name, description, id_user, state, active) VALUES
('Pedido A', 'Descripción del pedido A', 1, 'PROCESSING', true),
('Pedido B', 'Entrega urgente', 2, 'PROCESSING', true),
('Pedido C', 'Pedido devuelto', 3, 'DELIVERED', false),
('Pedido D', 'Pedido completado', 4, 'CANCELED', true);