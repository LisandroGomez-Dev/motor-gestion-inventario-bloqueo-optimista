# Cliente web – Simulador de Concurrencia

Frontend en **React 18 + Vite + Tailwind CSS** del Motor de Gestión de Inventario. Permite consultar el stock, descontar unidades, ajustar el stock manualmente y disparar 10 peticiones simultáneas para observar cómo el bloqueo optimista responde con `409 Conflict`.

## Requisitos

- Node.js 18+ y npm
- Backend corriendo en `http://localhost:8080` (ver el README principal del repositorio)

## Uso

```bash
npm install
npm run dev
```

La aplicación queda disponible en `http://localhost:5173`.

## Scripts

| Comando | Descripción |
| :--- | :--- |
| `npm run dev` | Servidor de desarrollo con recarga en caliente |
| `npm run build` | Compilación de producción |
| `npm run lint` | Análisis estático con Oxlint |
| `npm run preview` | Vista previa de la compilación |
