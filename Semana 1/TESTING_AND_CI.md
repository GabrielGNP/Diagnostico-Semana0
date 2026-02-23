# Tests unitarios y CI/CD

Este documento describe la estructura de tests y el pipeline de CI para el proyecto (dos microservicios backend y frontend React).

## Estructura del proyecto

```
Diagnostico-Semana0/
├── .github/
│   └── workflows/
│       └── ci.yml          # Pipeline CI: ejecuta tests en push/PR a main|master
├── Backend/
│   ├── pedido-service/     # Spring Boot, Maven, JUnit 5 + Mockito
│   │   └── src/test/...
│   └── usuario-service/    # Spring Boot, Maven, JUnit 5
│       └── src/test/...
└── Frontend/               # React + Vite, Vitest + Testing Library
    └── src/
        ├── test/
        │   └── setup.js
        └── App.test.jsx
```

## Cómo ejecutar los tests localmente

### Backend (cada microservicio)

Desde la raíz del repo:

```bash
# pedido-service
cd Backend/pedido-service && mvn test && cd ../..

# usuario-service
cd Backend/usuario-service && mvn test && cd ../..
```

O desde cada carpeta del servicio:

```bash
cd Backend/pedido-service
mvn test
```

### Frontend

```bash
cd Frontend
npm install
npm run test
```

## CI/CD con GitHub Actions

- **Workflow:** `.github/workflows/ci.yml`
- **Nombre:** "CI - Tests unitarios"
- **Disparadores:** `push` y `pull_request` a las ramas `main` y `master`

El pipeline tiene tres jobs que se ejecutan en paralelo:

| Job              | Directorio              | Comando      |
|------------------|-------------------------|-------------|
| pedido-service   | `Backend/pedido-service`| `mvn -q test` |
| usuario-service  | `Backend/usuario-service`| `mvn -q test` |
| frontend         | `Frontend`              | `npm ci` + `npm run test` |

Si alguno falla, el check de GitHub (en el commit o en el PR) falla y se puede bloquear el merge a la rama principal.

### Requisitos en la rama principal

Para que el merge solo se permita con todos los tests en verde:

1. En el repo de GitHub: **Settings → Branches → Branch protection rules**.
2. Añade o edita la regla para `main` (o `master`).
3. Activa **Require status checks to pass before merging**.
4. Marca el check **"pedido-service"**, **"usuario-service"** y **"frontend"** (o **"CI - Tests unitarios"** si se exige el workflow completo).

### Nota sobre Java

Los `pom.xml` del backend usan Java 25. En CI se configura `setup-java` con `java-version: '25'`. Si en los runners de GitHub no existiera esa versión, cambia en `ci.yml` a `'21'` y en ambos `pom.xml` ajusta `java.version` y `maven.compiler.source`/`target` a 21.

## Resumen de tecnologías de test

| Subproyecto     | Framework de tests | Ejecución CI      |
|-----------------|--------------------|--------------------|
| pedido-service  | JUnit 5, Mockito   | Maven (JUnit)      |
| usuario-service | JUnit 5            | Maven (JUnit)      |
| Frontend        | Vitest, Testing Library | `npm run test` (Vitest run) |
