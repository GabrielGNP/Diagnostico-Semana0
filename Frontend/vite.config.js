import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',  // Necesario para ser accesible fuera del contenedor
    port: 5173,
    strictPort: true,  // Falla si el puerto está ocupado (evita confusiones)
    watch: {
      usePolling: true,       // CRÍTICO en Windows/WSL2: el sistema de archivos
      interval: 300,          // no emite eventos inotify a través de volúmenes Docker
    },
    hmr: {
      // El navegador debe conectar al puerto EXPUESTO en el host (3000),
      // no al puerto interno del contenedor (5173)
      clientPort: 3000,
      host: 'localhost',
    },
  },
})
