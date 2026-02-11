# Dockerfile for Frontend

# Stage 1: Build
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
# Instalación estándar de dependencias (npm install) para compatibilidad
RUN npm install
COPY . .
# Ejecutar tests; si fallan, la construcción se detiene
RUN npm run test
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
