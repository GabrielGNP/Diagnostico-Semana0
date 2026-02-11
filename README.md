# Diagnostico-Semana0

Proyecto con dos microservicios backend (pedido-service, usuario-service) y frontend React (Vite).

## Tests y CI/CD

- **Tests unitarios:** cada subproyecto tiene su propia suite (JUnit en backend, Vitest en frontend).
- **CI:** en cada push o PR a `main`/`master`, GitHub Actions ejecuta los tests de los tres subproyectos. Ver [docs/TESTING_AND_CI.md](docs/TESTING_AND_CI.md) para detalles y cómo exigir que los tests pasen antes de merge.