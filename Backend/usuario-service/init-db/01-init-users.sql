CREATE DATABASE users_db;

CREATE ROLE usuario_user WITH LOGIN PASSWORD 'usuario_pass';

GRANT ALL PRIVILEGES ON DATABASE users_db TO usuario_user;

\connect users_db

GRANT ALL ON SCHEMA public TO usuario_user;
ALTER SCHEMA public OWNER TO usuario_user;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    mail VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO usuario_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO usuario_user;

-- Seed data: usuarios de ejemplo
INSERT INTO users (name, password, mail, active) VALUES
('Alice García', 'passAlice123', 'alice@example.com', true),
('Bob Pérez', 'bobPass!23', 'bob@example.com', true),
('Carla Ruiz', 'carlaPwd', 'carla@example.com', false),
('Diego López', 'diego2026', 'diego@example.com', true);