# ⚡ Motor de Gestión de Inventario con Bloqueo Optimista (Optimistic Locking)

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3.3.3](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-red.svg?logo=flyway)](https://flywaydb.org/)
[![React](https://img.shields.io/badge/React-18-61dafb.svg?logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF.svg?logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-CSS-38bdf8.svg?logo=tailwindcss)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)](https://www.docker.com/)

Sistema integral de gestión de inventario de alta concurrencia diseñado para resolver problemas de **condición de carrera (race conditions)** y **sobreventa (overselling)** utilizando **Control de Concurrencia Optimista (Optimistic Concurrency Control)** mediante JPA/Hibernate (`@Version`) y PostgreSQL, acompañado de un simulador visual interactivo desarrollado en React.

---

## 🎯 Problema que resuelve

En sistemas de e-commerce y gestión de stock con alto tráfico, múltiples transacciones intentan descontar stock simultáneamente sobre un mismo producto.
- **Sin control de concurrencia**: Ocurren lecturas sucias y sobreescrituras ciegas, resultando en inventarios negativos o inconsistencias contables graves.
- **Con Bloqueo Pesimista (`SELECT FOR UPDATE`)**: Se bloquea la fila a nivel de base de datos durante toda la transacción, generando cuellos de botella y riesgos de deadlocks bajo cargas masivas.
- **Con Bloqueo Optimista (`@Version`)**: Cada transacción lee el estado actual junto con un número de versión. Al guardar, se ejecuta:
  ```sql
  UPDATE inventario 
  SET stock_disponible = :nuevoStock, version = version + 1 
  WHERE id = :id AND version = :versionLeida;
  ```
  Si otra transacción modificó el registro en milisegundos intermedios, la condición `WHERE version = :versionLeida` no coincide (`0 rows updated`) y JPA lanza una excepción `ObjectOptimisticLockingFailureException` (409 Conflict), preservando la integridad del stock sin penalizar la disponibilidad del sistema.

---

## 🏗️ Arquitectura y Tecnologías

```
┌────────────────────────────────────────────────────────┐
│            Frontend: React + Vite + Tailwind           │
│   (Simulador Concurrente + Monitor de Logs en Vivo)   │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP / REST (JSON)
                            ▼
┌────────────────────────────────────────────────────────┐
│             Backend: Spring Boot 3.3.3                 │
│      • Controller: InventarioController                │
│      • Service: InventarioService                      │
│      • Entity: Inventario (@Version, @Table)           │
│      • GlobalExceptionHandler (Manejo de 409 Conflict) │
└───────────────────────────┬────────────────────────────┘
                            │ JPA / Hibernate
                            ▼
┌────────────────────────────────────────────────────────┐
│          Database: PostgreSQL 16 (Docker)              │
│      • Migraciones automáticas con Flyway              │
│      • Constraints de integridad (CHECK stock >= 0)   │
└────────────────────────────────────────────────────────┘
```

### Stack Técnico

- **Backend**: Java 21, Spring Boot 3.3.3, Spring Web, Spring Data JPA, Hibernate, Bean Validation, Lombok.
- **Base de Datos**: PostgreSQL 16 Alpine, Flyway Database Migrations.
- **Frontend**: React 18, Vite, Tailwind CSS, Lucide React (iconografía y dashboard interactivo).
- **Infraestructura**: Docker & Docker Compose.

---

## 📁 Estructura del Repositorio

```
.
├── .gitignore
├── README.md
└── inventory-engine/
    ├── docker-compose.yml              # Contenedor de PostgreSQL 16
    ├── pom.xml                         # Dependencias Maven y configuración Spring Boot
    ├── mvnw / mvnw.cmd                 # Maven Wrapper
    ├── src/
    │   ├── main/
    │   │   ├── java/com/inventario/inventory_engine/
    │   │   │   ├── InventoryEngineApplication.java
    │   │   │   ├── config/             # Configuración CORS y beans
    │   │   │   ├── controller/         # Endpoints REST y GlobalExceptionHandler
    │   │   │   ├── domain/             # Entidades JPA (Inventario, Producto) y Excepciones
    │   │   │   ├── repository/         # Spring Data JPA Repositories
    │   │   │   └── service/            # Lógica transaccional de descuento y ajuste
    │   │   └── resources/
    │   │       ├── application.yml     # Configuración de base de datos y Flyway
    │   │       └── db/migration/       # Scripts SQL V1 (esquema) y V2 (seed de prueba)
    │   └── test/                       # Tests unitarios y de integración
    └── inventory-client/               # Aplicación Frontend React
        ├── package.json
        ├── vite.config.js
        ├── tailwind.config.js
        └── src/
            ├── App.jsx                 # Dashboard de control y simulador concurrente
            └── main.jsx
```

---

## 🚀 Guía de Inicio Rápido

### Prerrequisitos

- **Java JDK 21** o superior instalado.
- **Node.js 18+** y npm.
- **Docker** y **Docker Compose** en ejecución.
- **Git**.

---

### Paso 1: Levantar la Base de Datos

En una terminal, sitúate en `inventory-engine` e inicia el contenedor de PostgreSQL:

```bash
cd inventory-engine
docker compose up -d
```

> **Verificación**: El contenedor `inventory-db` quedará expuesto en el puerto `5432` con la base de datos `inventory_db`.

---

### Paso 2: Ejecutar el Backend (Spring Boot)

En la misma carpeta `inventory-engine`, ejecuta:

```bash
# En Windows:
.\mvnw.cmd spring-boot:run

# En Linux/macOS:
./mvnw spring-boot:run
```

Flyway aplicará automáticamente las migraciones (`V1__initial_schema.sql` y `V2__seed_test_data.sql`), creando las tablas e insertando un producto de prueba (`iPhone 15 Pro` con 10 unidades de stock y versión 0).

El backend quedará escuchando en `http://localhost:8080`.

---

### Paso 3: Ejecutar el Frontend (React + Vite)

Abre una nueva terminal y navega a `inventory-client`:

```bash
cd inventory-engine/inventory-client
npm install
npm run dev
```

Abre en tu navegador `http://localhost:5173` para acceder al panel de control y simulador.

---

## 📡 Endpoints de la API REST

| Método | Endpoint | Descripción | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/v1/inventario/{productoId}` | Obtiene el estado actual de inventario (stock y versión). | `200 OK`, `404 Not Found` |
| **POST** | `/api/v1/inventario/descontar` | Descuenta stock validando la versión optimista. | `200 OK`, `400 Bad Request`, `409 Conflict` |
| **PUT** | `/api/v1/inventario/stock` | Ajusta o restablece el stock manualmente. | `200 OK`, `400 Bad Request` |

### Ejemplo de Solicitud de Descuento (`POST /api/v1/inventario/descontar`)

```json
{
  "productoId": 1,
  "cantidad": 2,
  "versionEsperada": 0
}
```

### Respuesta de Conflicto de Versión (`409 Conflict`)

```json
{
  "timestamp": "2026-09-26T00:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Conflicto de concurrencia: el registro fue modificado por otra transacción en paralelo. Por favor, reintente la operación con la versión actualizada."
}
```

---

## 🧪 Simulación de Concurrencia desde el Frontend

El cliente web incluye un simulador de estrés que lanza **peticiones asíncronas concurrentes** (`Promise.all`) con la misma versión leída:

1. **Observa el comportamiento**: Sólo la primera solicitud en alcanzar el servidor y persistir con la versión esperada tiene éxito (`200 OK`).
2. **Detección inmediata**: Las demás solicitudes que compiten por el mismo recurso son rechazadas con `409 Conflict`, ya que la versión fue incrementada por la primera transacción.
3. **Consola en vivo**: La interfaz muestra en tiempo real los éxitos, los conflictos detectados y la actualización reactiva del stock y la versión.

---

## 👨‍💻 Autor

- **Lisandro Gómez** - [@LisandroGomez10](https://github.com/LisandroGomez10)
