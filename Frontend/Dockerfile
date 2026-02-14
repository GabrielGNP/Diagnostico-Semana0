# Dockerfile for Frontend

# Stage 1: Build
FROM node:20-slim AS builder
WORKDIR /app
ARG VITE_APIUSER=http://localhost:8083
ARG VITE_APIORDER=http://localhost:8082
ENV VITE_APIUSER=$VITE_APIUSER
ENV VITE_APIORDER=$VITE_APIORDER
COPY package.json package-lock.json ./
# Instalación estándar de dependencias (npm install) para compatibilidad
RUN npm install
RUN npm install --no-save @rollup/rollup-linux-x64-gnu@4.57.1
COPY . .
# Ejecutar tests; si fallan, la construcción se detiene
RUN npm run test
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
