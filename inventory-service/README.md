# Inventory Service - Servicio de Inventario

## Descripción
El **Inventory Service** es un microservicio desarrollado en Spring Boot que gestiona el inventario de productos. Su función principal es verificar la disponibilidad de stock de productos mediante su SKU. Este servicio es crítico para validar órdenes antes de su procesamiento.

## Características Principales
- **Puerto**: 8083
- **Base de datos**: PostgreSQL (puerto 5431)
- **Registro automático**: Se registra en Eureka Server (puerto 8761)
- **API REST**: Expone endpoints para consultas de inventario
- **Validación de stock**: Verifica disponibilidad individual y en lotes

## Arquitectura

### Entidad Principal: Inventory
La entidad `Inventory` representa el stock de un producto:
- `id`: Identificador único (Long)
- `sku`: Código único del producto (String)
- `quantity`: Cantidad disponible en stock (Long)

### Estructura del Proyecto
```
inventory-service/
├── src/main/java/com/josedjaykv/inventory_service/
│   ├── InventoryServiceApplication.java
│   ├── controllers/
│   │   └── InventoryController.java
│   ├── model/
│   │   ├── dtos/
│   │   │   ├── BaseResponse.java
│   │   │   └── OrderItemRequest.java
│   │   └── entities/
│   │       └── Inventory.java
│   ├── repositories/
│   │   └── InventoryRepository.java
│   ├── services/
│   │   └── InventoryService.java
│   └── utils/
│       └── DataLoader.java
└── src/main/resources/
    └── application.properties
```

## API Endpoints

### Base URL
Acceso a través del API Gateway: `http://localhost:8080/api/inventory`  
Acceso directo: `http://localhost:8083/api/inventory`

### Endpoints Disponibles

#### 1. Verificar Stock Individual
- **Método**: `GET /api/inventory/{sku}`
- **Estado HTTP**: `200 OK`
- **Descripción**: Verifica si un producto específico tiene stock disponible
- **Parámetros**: 
  - `sku` (path): Código único del producto
- **Respuesta**: `boolean` (true si hay stock, false si no)

**Ejemplo de uso:**
```http
GET /api/inventory/PROD001
Response: true
```

#### 2. Verificar Stock en Lote
- **Método**: `POST /api/inventory/in-stock`
- **Estado HTTP**: `200 OK`
- **Descripción**: Verifica la disponibilidad de múltiples productos simultáneamente
- **Body**: Lista de `OrderItemRequest`
- **Respuesta**: `BaseResponse` con resultado de la validación

**Ejemplo de uso:**
```http
POST /api/inventory/in-stock
Body: [
  {"sku": "PROD001", "quantity": 5},
  {"sku": "PROD002", "quantity": 3}
]
```

## DTOs (Data Transfer Objects)

### OrderItemRequest
Utilizado para solicitar verificación de stock:
```java
{
  "sku": "string",      // Código del producto
  "quantity": "number"  // Cantidad requerida
}
```

### BaseResponse
Respuesta estándar del servicio:
```java
{
  "message": "string",  // Mensaje descriptivo
  "success": "boolean"  // Resultado de la operación
}
```

## Configuración

### Base de Datos (PostgreSQL)
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.url=jdbc:postgresql://localhost:5431/ms_inventory
spring.datasource.username=josedjaykv
spring.datasource.password=Test123
```

### Registro en Eureka
```properties
eureka.client.service-url.default-zone=http://localhost:8761/eureka
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
```

## Integración con Docker
El servicio utiliza una base de datos PostgreSQL containerizada en puerto diferente:
```yaml
db-inventory:
  container_name: db-inventory
  image: postgres:15.2
  environment:
    POSTGRES_DB: ms_inventory
    POSTGRES_USER: josedjaykv
    POSTGRES_PASSWORD: Test123
  ports:
    - 5431:5431  # Puerto diferente para evitar conflictos
  command: -p 5431
```

## Funcionalidades Clave

### Validación de Stock
- **Individual**: Verifica si un SKU específico tiene stock (quantity > 0)
- **En lote**: Valida múltiples productos simultáneamente
- **Respuesta rápida**: Optimizado para consultas frecuentes desde Orders Service

### Carga de Datos Inicial
El servicio incluye `DataLoader.java` para poblar datos iniciales de inventario.

## Integración con Otros Servicios

### Orders Service
- El Orders Service consulta este endpoint antes de procesar órdenes
- Valida que todos los productos de una orden tengan stock suficiente
- Previene la creación de órdenes para productos agotados

## Descubrimiento de Servicios
- **Nombre del servicio**: `inventory-service`
- **Registro automático**: Al iniciar, el servicio se registra automáticamente en Eureka Server
- **Health checks**: Eureka monitorea la salud del servicio automáticamente
- **Load balancing**: El API Gateway puede distribuir carga entre múltiples instancias

## Tecnologías Utilizadas
- **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Netflix Eureka Client**

## Patrones de Diseño
- **Repository Pattern**: Para acceso a datos
- **Service Layer**: Lógica de negocio encapsulada
- **DTO Pattern**: Transferencia de datos optimizada
- **Microservice Pattern**: Responsabilidad única (gestión de inventario)

## Ejecución
1. Asegurar que PostgreSQL esté ejecutándose (puerto 5431)
2. Verificar que Eureka Server esté activo (puerto 8761)
3. Ejecutar el servicio: `mvn spring-boot:run`
4. El servicio estará disponible en el puerto 8083
5. Verificar registro en Eureka: `http://localhost:8761`

## Consideraciones de Rendimiento
- **Consultas optimizadas**: Búsquedas por SKU indexadas
- **Respuestas rápidas**: Endpoints diseñados para baja latencia
- **Escalabilidad**: Múltiples instancias soportadas por Eureka y API Gateway