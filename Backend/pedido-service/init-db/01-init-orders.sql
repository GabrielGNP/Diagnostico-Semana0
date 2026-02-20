-- ================================================
-- PEDIDO SERVICE - DATABASE INITIALIZATION
-- ================================================
-- Database: orders_db
-- User Story: HU-ORD-01
-- Date: 2026-02-19
-- ================================================

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    id_user INTEGER NOT NULL,
    state VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_orders_id_user ON orders(id_user);
CREATE INDEX IF NOT EXISTS idx_orders_active ON orders(active);
CREATE INDEX IF NOT EXISTS idx_orders_state ON orders(state);
CREATE INDEX IF NOT EXISTS idx_orders_user_active ON orders(id_user, active);

-- Insert sample data for testing
INSERT INTO orders (name, description, id_user, state, active) VALUES
    ('Pedido Premium 1', 'Laptop Dell XPS 15', 1, 'PROCESSING', true),
    ('Pedido Express 2', 'iPhone 15 Pro Max', 2, 'DELIVERED', true),
    ('Pedido Standard 3', 'Monitor Samsung 27"', 1, 'SHIPPED', true),
    ('Pedido Cancelado', 'Producto devuelto', 3, 'CANCELED', false),
    ('Pedido Urgente', 'Mouse Logitech MX Master', 2, 'PROCESSING', true)
ON CONFLICT DO NOTHING;

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to auto-update updated_at
DROP TRIGGER IF EXISTS update_orders_updated_at ON orders;
CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions (just in case)
GRANT ALL PRIVILEGES ON TABLE orders TO pedido_user;
GRANT USAGE, SELECT ON SEQUENCE orders_id_seq TO pedido_user;

-- Display confirmation
SELECT 'Database initialized successfully!' AS status;
SELECT COUNT(*) AS total_orders FROM orders;
SELECT COUNT(*) AS active_orders FROM orders WHERE active = true;
