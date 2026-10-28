# Microservicio de Devoluciones y Garantías

Microservicio de la plataforma de E-Commerce encargado de administrar las solicitudes de devolución de productos y las reclamaciones de garantía.

Permite registrar solicitudes, consultar su estado, validar artículos y cantidades, resolver solicitudes mediante decisiones administrativas y mantener un historial de los cambios de estado de las devoluciones.

\---

## Responsabilidades

* Crear y consultar solicitudes de devolución.
* Consultar las devoluciones del usuario autenticado mediante paginación.
* Validar que el pedido, el usuario y los productos correspondan y que se cumplan los plazos y cantidades permitidos.
* Registrar la recepción de productos devueltos.
* Aprobar o rechazar devoluciones y guardar el historial de cambios de estado.
* Registrar reclamaciones de garantía y consultar su estado.
* Validar la vigencia de la garantía y los productos incluidos en una reclamación.
* Aprobar o rechazar reclamaciones de garantía.
* Devolver respuestas HTTP y mensajes de error controlados para los distintos escenarios de validación y negocio.

> \\\*\\\*Integración temporal:\\\*\\\* actualmente, la consulta de usuarios y pedidos utiliza `StubOrderGateway`, que contiene datos de prueba en memoria. Debe sustituirse por la integración real con los microservicios correspondientes cuando esté disponible.

\---

## Tecnologías

* Java 17+
* Spring Boot
* Spring Web MVC
* Spring Data JPA / Hibernate
* Spring Security con HTTP Basic
* Bean Validation
* MySQL 8+
* Maven Wrapper
* JUnit 5, Mockito y Spring Test
* JaCoCo para medir la cobertura de pruebas
* H2 para la base de datos de pruebas

\---

## Base de datos

El microservicio utiliza la base de datos `garantias`. El esquema SQL se encuentra en el directorio `DB/`.

### Tablas principales

|Tabla|Descripción|
|-|-|
|`return\\\_requests`|Datos principales de cada solicitud de devolución: pedido, usuario, estado y fechas.|
|`return\\\_items`|Productos y cantidades asociadas a una solicitud de devolución.|
|`return\\\_status\\\_history`|Historial de cambios de estado de una devolución, quién lo realizó y cuándo.|
|`warranty\\\_claims`|Datos principales de cada reclamación de garantía.|
|`warranty\\\_items`|Productos incluidos en cada reclamación, motivo, descripción y fechas de vigencia.|

### Estados y motivos

**Devoluciones**

* Estados: `PENDIENTE`, `RECIBIDO`, `APROBADO`, `RECHAZADO`.
* Motivos: `ITEM\\\_DEFECTUOSO`, `ITEM\\\_EQUIVOCADO`, `NO\\\_COMO\\\_DESCRITO`, `CAMBIO\\\_OPINION`, `OTRO`.

**Garantías**

* Estados: `PENDIENTE`, `APROBADO`, `RECHAZADO`.
* Motivos: `DEFECTUOSO`, `FALLA\\\_FUNCIONAL`, `DANO\\\_EN\\\_TRANSPORTE`, `OTRO`.

### Diagrama de relaciones

```mermaid
erDiagram
    RETURN\\\_REQUESTS ||--o{ RETURN\\\_ITEMS : incluye
    RETURN\\\_REQUESTS ||--o{ RETURN\\\_STATUS\\\_HISTORY : registra
    WARRANTY\\\_CLAIMS ||--o{ WARRANTY\\\_ITEMS : incluye

    RETURN\\\_REQUESTS {
        BIGINT id\\\_request PK
        BIGINT order\\\_id
        BIGINT user\\\_id
        VARCHAR status
        DATETIME created\\\_at
        DATETIME updated\\\_at
    }

    RETURN\\\_ITEMS {
        BIGINT id\\\_r\\\_item PK
        BIGINT id\\\_request FK
        INT item\\\_id
        VARCHAR item\\\_name
        INT quantity
        VARCHAR reason
    }

    RETURN\\\_STATUS\\\_HISTORY {
        BIGINT id\\\_history PK
        BIGINT id\\\_request FK
        VARCHAR previous\\\_status
        VARCHAR new\\\_status
        VARCHAR changed\\\_by
        DATETIME changed\\\_at
    }

    WARRANTY\\\_CLAIMS {
        BIGINT id\\\_claim PK
        BIGINT order\\\_id
        BIGINT user\\\_id
        VARCHAR status
        DATETIME created\\\_at
        DATETIME updated\\\_at
    }

    WARRANTY\\\_ITEMS {
        BIGINT id\\\_w\\\_item PK
        BIGINT id\\\_claim FK
        BIGINT item\\\_id
        VARCHAR item\\\_name
        INT quantity
        VARCHAR reason
        VARCHAR description
        DATETIME purchase\\\_date
        DATETIME warranty\\\_expires\\\_at
    }
```

### Scripts de base de datos

Los scripts se encuentran en `DB/`:

1. `returns\\\_database\\\_V2.sql`: crea la base de datos `garantias` y las tablas de devoluciones y reclamaciones de garantía.
2. `03\\\_return\\\_status\\\_history.sql`: crea la tabla `return\\\_status\\\_history` para registrar los cambios de estado de las devoluciones.

Ejecuta primero `returns\\\_database\\\_V2.sql` y después `03\\\_return\\\_status\\\_history.sql`. La aplicación utiliza `spring.jpa.hibernate.ddl-auto=validate`, por lo que las tablas deben existir y coincidir con las entidades JPA antes de iniciar el servicio.

> Las claves foráneas hacia las tablas de usuarios y pedidos de otros microservicios están pendientes en el esquema actual; la verificación correspondiente se realiza mediante el gateway temporal.

\---

## API REST

**URL base local:** `http://localhost:8080`

### Devoluciones

|Método|Endpoint|Descripción|Respuesta habitual|
|-|-|-|-|
|`POST`|`/api/returns`|Crear una solicitud de devolución.|`201 Created`|
|`GET`|`/api/returns/{id}`|Consultar una solicitud por su ID.|`200 OK` o `404 Not Found`|
|`GET`|`/api/returns/me`|Consultar las devoluciones del usuario autenticado; admite paginación.|`200 OK`|
|`PATCH`|`/api/returns/admin/{id}/received`|Registrar la recepción de una devolución aprobada.|`200 OK`|
|`PATCH`|`/api/admin/returns/{id}/decision`|Aprobar o rechazar una devolución y registrar quién tomó la decisión.|`204 No Content`|

El endpoint `/api/returns/me` utiliza paginación de Spring Data. Por defecto muestra 10 elementos por página y ordena por `createdAt` descendente. Se pueden indicar parámetros como `page`, `size` y `sort`.

### Garantías

|Método|Endpoint|Descripción|Respuesta habitual|
|-|-|-|-|
|`POST`|`/api/warranties/claims`|Crear una reclamación de garantía.|`201 Created`|
|`GET`|`/api/warranties/claims/{id}`|Consultar el estado de una reclamación.|`200 OK`|
|`PATCH`|`/api/warranties/claims/admin/{id}/decision`|Resolver una reclamación de garantía.|`200 OK`|
|`PATCH`|`/api/warranties/claims/{id}/decision`|Ruta alternativa para resolver una reclamación de garantía.|`200 OK`|

### Ejemplos de solicitudes

#### Crear una devolución

`POST /api/returns`

```json
{
  "orderId": 300,
  "userId": 1,
  "items": \\\[
    {
      "itemId": 25,
      "quantity": 1,
      "reason": "ITEM\\\_DEFECTUOSO"
    }
  ]
}
```

#### Decidir una devolución

`PATCH /api/admin/returns/1/decision`

```json
{
  "decision": "APROBADO"
}
```

La decisión de una devolución debe corresponder a un estado final permitido, como `APROBADO` o `RECHAZADO`.

#### Crear una reclamación de garantía

`POST /api/warranties/claims`

```json
{
  "userId": 1,
  "orderId": 302,
  "items": \\\[
    {
      "itemId": 40,
      "quantity": 1,
      "reason": "DEFECTUOSO",
      "description": "El producto no funciona correctamente"
    }
  ]
}
```

#### Resolver una reclamación de garantía

`PATCH /api/warranties/claims/1/decision`

```json
{
  "decision": "APROBADO",
  "status": null,
  "reason": "Reclamación revisada"
}
```

Los ejemplos utilizan IDs disponibles en el `StubOrderGateway` incluido actualmente en el proyecto; no representan datos de una base de datos de pedidos real.

\---

## Seguridad

En el perfil `local`, la aplicación configura usuarios en memoria y valida sus contraseñas mediante un `PasswordEncoder`. Las contraseñas se leen de estas variables de entorno:

* `MS05\\\_USER101\\\_PASSWORD`: usuario `usuario101`, rol `USER`.
* `MS05\\\_USER102\\\_PASSWORD`: usuario `usuario102`, rol `USER`.
* `MS05\\\_ADMIN\\\_PASSWORD`: usuario `admin`, rol `ADMIN`.

La configuración actual de `SecurityConfig` requiere el rol `ADMIN` para `/api/admin/returns/\\\*\\\*` y el rol `USER` para `GET /api/returns/me`. El resto de las rutas `/api/returns/\\\*\\\*` queda permitido por la configuración general. Por su parte, `WarrantySecurityConfig` permite actualmente el acceso a las rutas `/api/warranties/\\\*\\\*`.

> \\\*\\\*Revisión recomendada antes de desplegar:\\\*\\\* comprobar si las rutas de recepción de devoluciones y de resolución de garantías deben exigir autenticación y autorización por rol. La configuración descrita refleja el código actual y no debe interpretarse como una política de seguridad de producción.

\---

## Inicialización del proyecto

### 1\. Requisitos

* Java 17 o superior.
* MySQL 8 o compatible.
* Git.

### 2\. Clonar el repositorio

```bash
git clone <URL\\\_DEL\\\_REPOSITORIO>
cd devoluciones.garantias
```

### 3\. Crear y configurar la base de datos

1. Inicia MySQL.
2. Ejecuta `DB/returns\\\_database\\\_V2.sql`.
3. Ejecuta `DB/03\\\_return\\\_status\\\_history.sql`.
4. Verifica que la base de datos `garantias` y sus tablas estén creadas.

### 4\. Configurar la conexión local

Revisa o crea `src/main/resources/application-local.properties` con las credenciales de tu propio MySQL. Ejemplo de configuración —reemplaza los valores de usuario y contraseña por los de tu entorno—:

```properties
spring.application.name=devoluciones.garantias
spring.datasource.url=jdbc:mysql://localhost:3306/garantias
spring.datasource.username=TU\\\_USUARIO
spring.datasource.password=TU\\\_PASSWORD
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format\\\_sql=true
warranty.period-months=3
```

El archivo `application.properties` activa el perfil `local`. El archivo `application-local.properties` está en `.gitignore`; no subas credenciales reales al repositorio ni las compartas en capturas o documentación.

### 5\. Configurar las contraseñas de usuarios locales

En PowerShell, establece las contraseñas para la sesión actual. Sustituye los marcadores por contraseñas locales propias:

```powershell
$env:MS05\\\_USER101\\\_PASSWORD="<contraseña-usuario101>"
$env:MS05\\\_USER102\\\_PASSWORD="<contraseña-usuario102>"
$env:MS05\\\_ADMIN\\\_PASSWORD="<contraseña-admin>"
```

Las tres variables son necesarias para crear los usuarios de prueba en el perfil `local`.

### 6\. Ejecutar el microservicio

En Windows PowerShell:

```powershell
.\\\\mvnw.cmd spring-boot:run
```

En macOS/Linux:

```bash
./mvnw spring-boot:run
```

Cuando inicie correctamente, la aplicación estará disponible en `http://localhost:8080`.

\---

## Pruebas unitarias y cobertura

El proyecto utiliza JUnit 5 y Mockito para probar servicios, controladores, DTO, validaciones, manejadores de excepciones, configuración de usuarios y gateway temporal. Las pruebas usan H2 para el contexto de pruebas cuando corresponde.

### Ejecutar las pruebas

Windows PowerShell:

```powershell
.\\\\mvnw.cmd test
```

Ejecutar todas las pruebas y comprobar la cobertura:

```powershell
.\\\\mvnw.cmd clean verify
```

macOS/Linux:

```bash
./mvnw clean verify
```

JaCoCo está configurado para exigir un mínimo de **95 % de cobertura de líneas** en el conjunto de clases analizado. Si no se alcanza el mínimo, la fase `verify` falla.

El reporte HTML se genera en:

```text
target/site/jacoco/index.html
```

La última captura del reporte mostró aproximadamente **99 % de cobertura de instrucciones**, con **475 de 478 líneas cubiertas** (aproximadamente **99,37 % de cobertura de líneas**) y 100 % de cobertura de las ramas contabilizadas. Este resultado puede cambiar cuando se modifique el código o las pruebas.

\---

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/com/ecommerce/devoluciones/garantias/
│   │   ├── config/       # Seguridad y resolución de usuario
│   │   ├── controller/   # Endpoints REST
│   │   ├── dto/          # Objetos de solicitud y respuesta
│   │   ├── exception/    # Manejo de excepciones
│   │   ├── gateway/      # Acceso temporal a datos de pedidos y usuarios
│   │   ├── model/        # Entidades y enumeraciones
│   │   ├── repository/   # Persistencia y validaciones de negocio
│   │   └── service/      # Reglas de devoluciones y garantías
│   └── resources/        # Configuración de Spring
└── test/
    ├── java/             # Pruebas unitarias y de contexto
    └── resources/        # Configuración del perfil de pruebas

DB/                       # Scripts de creación de base de datos
```



