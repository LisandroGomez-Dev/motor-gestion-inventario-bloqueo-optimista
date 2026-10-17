# ⚡ Motor de Gestión de Inventario con Bloqueo Optimista (Optimistic Locking)

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3.3.3](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-red.svg?logo=flyway)](https://flywaydb.org/)
[![React](https://img.shields.io/badge/React-18-61dafb.svg?logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF.svg?logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-CSS-38bdf8.svg?logo=tailwindcss)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)](https://www.docker.com/)

Sistema integral de gestión de inventario de alta concurrencia diseñado para resolver problemas de condición de carrera (race conditions) y sobreventa (overselling) utilizando bloqueo optimista basado en versiones.

---

## 🎯 Problema que resuelve

En sistemas de e-commerce y gestión de stock con alto tráfico, múltiples transacciones pueden intentar descontar stock simultáneamente sobre un mismo producto.

- Sin control de concurrencia: pueden producirse lecturas inconsistentes y sobreescrituras que llevan a inventarios negativos.
- Bloqueo pesimista (`SELECT FOR UPDATE`): garantiza consistencia pero puede generar cuellos de botella y deadlocks bajo carga alta.
- Bloqueo optimista (`@Version`): cada entidad mantiene un número de versión. Al actualizar se compara la versión leída y se incrementa; si otra transacción cambió la fila, la actualización falla y se detecta el conflicto.

Ejemplo SQL que realiza la verificación de versión:

```sql
UPDATE inventario
SET stock_disponible = :nuevoStock, version = version + 1
WHERE id = :id AND version = :versionLeida;
```

Si la condición `WHERE version = :versionLeida` no coincide (0 filas actualizadas), se lanza una excepción de optimización (por ejemplo, `ObjectOptimisticLockingFailureException` en Spring Data) y la operación debe reintentar o informar el conflicto al usuario.

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
│      • Constraints de integridad (CHECK stock >= 0)    │
└────────────────────────────────────────────────────────┘
```

### Stack Técnico

- Backend: Java 21, Spring Boot 3.3.3, Spring Web, Spring Data JPA, Hibernate, Bean Validation, Lombok.
- Base de Datos: PostgreSQL 16, Flyway para migraciones.
- Frontend: React 18, Vite, Tailwind CSS.
- Infraestructura: Docker & Docker Compose.

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

- Java JDK 21 o superior.
- Node.js 18+ y npm.
- Docker y Docker Compose.
- Git.

### Paso 1: Levantar la Base de Datos

En una terminal, sitúate en `inventory-engine` e inicia el contenedor de PostgreSQL:

```bash
cd inventory-engine
docker compose up -d
```

Verifica que el contenedor `inventory-db` esté corriendo y expuesto en el puerto `5432`.

### Paso 2: Ejecutar el Backend (Spring Boot)

En la carpeta `inventory-engine`, ejecuta:

```bash
# En Windows:
.\mvnw.cmd spring-boot:run

# En Linux/macOS:
./mvnw spring-boot:run
```

Flyway aplicará automáticamente las migraciones (`V1__initial_schema.sql` y `V2__seed_test_data.sql`), creando las tablas e insertando datos de prueba (por ejemplo: un producto de prueba con stock inicial).

El backend quedará escuchando en `http://localhost:8080`.

### Paso 3: Ejecutar el Frontend (React + Vite)

Abre una nueva terminal y navega a `inventory-engine/inventory-client`:

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
| GET | `/api/v1/inventario/{productoId}` | Obtiene el estado actual de inventario (stock y versión). | `200 OK`, `404 Not Found` |
| POST | `/api/v1/inventario/descontar` | Descuenta stock validando la versión optimista. | `200 OK`, `400 Bad Request`, `409 Conflict` |
| PUT | `/api/v1/inventario/stock` | Ajusta o restablece el stock manualmente. | `200 OK`, `400 Bad Request` |

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

El cliente web incluye un simulador de estrés que lanza peticiones asíncronas concurrentes (`Promise.all`) con la misma versión leída:

1. Observa el comportamiento: sólo la primera solicitud que persiste con la versión esperada tiene éxito (`200 OK`).
2. Las demás solicitudes que compiten por el mismo recurso recibirán `409 Conflict` y pueden reintentar según la lógica de cliente.
3. La interfaz muestra en tiempo real los éxitos, conflictos y la actualización reactiva del stock y la versión.

---

## 🧾 Migraciones (Flyway)

Coloca los scripts SQL en `src/main/resources/db/migration` con el prefijo `V{n}__{descripcion}.sql`. Flyway los aplicará automáticamente al arrancar la aplicación.

Ejemplo mínimo de `V1__initial_schema.sql`:

```sql
CREATE TABLE producto (
  id BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL
);

CREATE TABLE inventario (
  id BIGSERIAL PRIMARY KEY,
  producto_id BIGINT NOT NULL REFERENCES producto(id),
  stock_disponible INT NOT NULL CHECK (stock_disponible >= 0),
  version INT NOT NULL DEFAULT 0
);
```

---

## 🧑‍💻 Contribuciones

Si querés colaborar:

1. Forkea el repositorio.
2. Crea una rama feature: `git checkout -b feature/nombre`.
3. Haz commits claros y push.
4. Abre un Pull Request describiendo los cambios.

---

## 👨‍💻 Autor

- **Lisandro Gómez** - [@LisandroGomez-Dev](https://github.com/LisandroGomez-Dev)

---

Si querés, puedo también actualizar el README del cliente (inventory-client) para que esté en español y tenga instrucciones específicas; decime si lo hago.