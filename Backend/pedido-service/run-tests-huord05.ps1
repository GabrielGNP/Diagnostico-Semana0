# Script para ejecutar tests de HU-ORD-05 usando Docker
# Uso: .\run-tests-huord05.ps1

Write-Host "🧪 Ejecutando tests de HU-ORD-05 con Docker..." -ForegroundColor Cyan

# Navegar al directorio del proyecto
$projectRoot = "C:\Users\Santiago\Documents\Equipo 2\Diagnostico-Semana0"
Set-Location $projectRoot

Write-Host "`n📦 Construyendo imagen de pedido-service..." -ForegroundColor Yellow
docker build -t pedido-service-test -f Backend/pedido-service/Dockerfile Backend/pedido-service

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al construir la imagen" -ForegroundColor Red
    exit 1
}

Write-Host "`n🔬 Ejecutando tests unitarios..." -ForegroundColor Yellow
docker run --rm `
    -v "${projectRoot}/Backend/pedido-service:/app" `
    -w /app `
    eclipse-temurin:21-jdk `
    sh -c "chmod +x ./mvnw && ./mvnw test -Dtest=OrderServiceHuOrd05Test"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Todos los tests pasaron! Fase GREEN completada!" -ForegroundColor Green
} else {
    Write-Host "`n❌ Algunos tests fallaron. Revisar implementación." -ForegroundColor Red
    exit 1
}
