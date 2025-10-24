# Orders Service - Servicio de Órdenes

## Descripción
El **Orders Service** es un microservicio desarrollado en Spring Boot que gestiona el procesamiento de órdenes de compra. Este servicio coordina con el Inventory Service para validar disponibilidad de stock antes de crear órdenes, implementando un patrón de comunicación entre microservicios.

## Características Principales
- **Puerto**: 8082
- **Base de datos**: MySQL (puerto 3306)
- **Registro automático**: Se registra en Eureka Server (puerto 8761)
- **API REST**: Expone endpoints para gestión de órdenes
- **Comunicación entre servicios**: Utiliza WebClient para consultar Inventory Service
- **Load Balancing**: Implementa balanceador de carga para comunicación con otros servicios

## Arquitectura

### Entidades Principales

#### Order (Orden)
La entidad `Order` representa una orden de compra:
- `id`: Identificador único (Long)
- `orderNumber`: Número único de la orden (String)
- `orderItems`: Lista de items de la orden (Relación OneToMany)

#### OrderItems (Items de Orden)
La entidad `OrderItems` representa cada producto en una orden:
- `id`: Identificador único (Long)
- `sku`: Código del producto (String)
- `price`: Precio del producto (Double)
- `quantity`: Cantidad solicitada (Long)
- `order`: Referencia a la orden padre (Relación ManyToOne)

### Estructura del Proyecto
```
orders-service/
├── src/main/java/com/josedjaykv/orders_service/
│   ├── OrdersServiceApplication.java
│   ├── config/
│   │   └── WebClientConfig.java
│   ├── controllers/
│   │   └── OrderController.java
│   ├── model/
│   │   ├── dtos/
│   │   │   ├── BaseResponse.java
│   │   │   ├── OrderItemRequest.java
│   │   │   ├── OrderItemsResponse.java
│   │   │   ├── OrderRequest.java
│   │   │   └── OrderResponse.java
│   │   └── entities/
│   │       ├── Order.java
│   │       └── OrderItems.java
│   ├── repositories/
│   │   └── OrderRepository.java
│   └── services/
│       └── OrderService.java
└── src/main/resources/
    └── application.properties
```

## API Endpoints

### Base URL
Acceso a través del API Gateway: `http://localhost:8080/api/order`  
Acceso directo: `http://localhost:8082/api/order`

### Endpoints Disponibles

#### 1. Crear Orden
- **Método**: `POST /api/order`
- **Estado HTTP**: `201 CREATED`
- **Descripción**: Crea una nueva orden después de validar disponibilidad de stock
- **Body**: `OrderRequest`
- **Respuesta**: `"Order placed successfully"`

**Ejemplo de uso:**
```http
POST /api/order
Body: {
  "orderItems": [
    {
      "sku": "PROD001",
      "price": 29.99,
      "quantity": 2
    },
    {
      "sku": "PROD002", 
      "price": 15.50,
      "quantity": 1
    }
  ]
}
```

#### 2. Obtener Todas las Órdenes
- **Método**: `GET /api/order`
- **Estado HTTP**: `200 OK`
- **Descripción**: Obtiene la lista completa de órdenes con sus items
- **Respuesta**: Lista de `OrderResponse`

## DTOs (Data Transfer Objects)

### OrderRequest
Utilizado para crear nuevas órdenes:
```java
{
  "orderItems": [
    {
      "sku": "string",      // Código del producto
      "price": "number",    // Precio del producto
      "quantity": "number"  // Cantidad solicitada
    }
  ]
}
```

### OrderResponse
Respuesta con datos de la orden:
```java
{
  "id": "number",
  "orderNumber": "string",
  "orderItems": [
    {
      "sku": "string",
      "price": "number", 
      "quantity": "number"
    }
  ]
}
```

### OrderItemRequest
Representa un item individual en una orden:
```java
{
  "sku": "string",
  "price": "number",
  "quantity": "number"
}
```

## Configuración

### Base de Datos (MySQL)
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.datasource.url=jdbc:mysql://localhost:3306/ms_orders
spring.datasource.username=josedjaykv
spring.datasource.password=Test123
```

### Registro en Eureka
```properties
eureka.client.service-url.default-zone=http://localhost:8761/eureka
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
```

### Configuración de WebClient
El servicio incluye configuración para comunicación entre microservicios:
```java
@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClient() {
        return WebClient.builder();
    }
}
```

## Integración con Docker
El servicio utiliza una base de datos MySQL containerizada:
```yaml
db-orders:
  container_name: db-orders
  image: mysql:8.0.33
  environment:
    MYSQL_DATABASE: ms_orders
    MYSQL_USER: josedjaykv
    MYSQL_PASSWORD: Test123
    MYSQL_ROOT_PASSWORD: root
  ports:
    - 3306:3306
```

## Funcionalidades Clave

### Validación de Stock
Antes de crear una orden, el servicio:
1. **Consulta al Inventory Service**: Verifica disponibilidad de todos los productos
2. **Validación en lote**: Envía todos los items para validación simultánea
3. **Procesamiento condicional**: Solo crea la orden si todos los productos tienen stock

### Comunicación entre Microservicios
- **WebClient con Load Balancing**: Utiliza `@LoadBalanced` para distribuir llamadas
- **Service Discovery**: Encuentra el Inventory Service automáticamente vía Eureka
- **Resiliencia**: Manejo de errores en comunicación entre servicios

### Generación de Números de Orden
- **Números únicos**: Cada orden recibe un número identificador único
- **Trazabilidad**: Permite seguimiento de órdenes en el sistema

## Integración con Otros Servicios

### Inventory Service
- **Validación de stock**: Consulta `POST /api/inventory/in-stock` antes de crear órdenes
- **Prevención de órdenes inválidas**: No permite crear órdenes para productos sin stock
- **Comunicación asíncrona**: Utiliza WebClient reactivo para mejor rendimiento

### Products Service
- **Información de productos**: Aunque no se comunica directamente, utiliza SKUs que deben existir en Products Service
- **Consistencia de datos**: Los SKUs en órdenes deben corresponder a productos existentes

## Descubrimiento de Servicios
- **Nombre del servicio**: `orders-service`
- **Registro automático**: Al iniciar, el servicio se registra automáticamente en Eureka Server
- **Health checks**: Eureka monitorea la salud del servicio automáticamente
- **Load balancing**: El API Gateway y WebClient pueden distribuir carga entre múltiples instancias

## Tecnologías Utilizadas
- **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0**
- **Spring Data JPA**
- **Spring WebFlux (WebClient)**
- **MySQL**
- **Lombok**
- **Netflix Eureka Client**
- **Spring Cloud LoadBalancer**

## Patrones de Diseño
- **Repository Pattern**: Para acceso a datos
- **Service Layer**: Lógica de negocio encapsulada
- **DTO Pattern**: Transferencia de datos optimizada
- **Circuit Breaker**: (Implícito en Spring Cloud) Para manejo de fallos
- **Service Discovery**: Descubrimiento automático de servicios
- **Load Balancing**: Distribución de carga entre instancias

## Flujo de Procesamiento de Órdenes

1. **Recepción de solicitud**: El controller recibe `OrderRequest`
2. **Validación de stock**: Se consulta Inventory Service con todos los items
3. **Validación exitosa**: Si hay stock suficiente, se procede
4. **Creación de orden**: Se genera número único y se persiste
5. **Respuesta al cliente**: Se confirma creación exitosa

## Ejecución
1. Asegurar que MySQL esté ejecutándose (puerto 3306)
2. Verificar que Eureka Server esté activo (puerto 8761)
3. Verificar que Inventory Service esté activo (puerto 8083)
4. Ejecutar el servicio: `mvn spring-boot:run`
5. El servicio estará disponible en el puerto 8082
6. Verificar registro en Eureka: `http://localhost:8761`

## Consideraciones de Rendimiento
- **WebClient reactivo**: Comunicación no bloqueante con otros servicios
- **Load balancing**: Distribución automática de carga
- **Consultas optimizadas**: JPA optimizado para consultas de órdenes
- **Validación en lote**: Verifica múltiples items en una sola llamada

## Manejo de Errores
- **Validación de stock fallida**: No se crea la orden si no hay stock suficiente
- **Comunicación fallida**: Manejo graceful de errores de red
- **Transacciones**: Rollback automático en caso de errores durante creación