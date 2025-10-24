# Products Service - Servicio de Medianos

## Descripción
El **Products Service** es un microservicio desarrollado en Spring Boot que gestiona el catálogo de productos "Medianos". Este servicio forma parte de una arquitectura de microservicios y se registra automáticamente en el servidor de descubrimiento Eureka.

## Características Principales
- **Puerto**: 8081
- **Base de datos**: PostgreSQL (puerto 5432)
- **Registro automático**: Se registra en Eureka Server (puerto 8761)
- **API REST**: Expone endpoints para operaciones CRUD de productos

## Arquitectura

### Entidad Principal: Medianos
La entidad `Medianos` representa un producto con las siguientes propiedades:
- `id`: Identificador único (Long)
- `sku`: Código único del producto (String)
- `name`: Nombre del producto (String)  
- `height`: Altura del mediano (Float)
- `weight`: Peso del mediano (Float)
- `description`: Descripción del producto (String)
- `price`: Precio del producto (Double)
- `status`: Estado activo/inactivo (Boolean)

### Estructura del Proyecto
```
products-service/
├── src/main/java/com/josedjaykv/products_service/
│   ├── ProductsServiceApplication.java
│   ├── controllers/
│   │   └── MedianosController.java
│   ├── model/
│   │   ├── dtos/
│   │   │   ├── MedianoRequest.java
│   │   │   └── MedianoResponse.java
│   │   └── entities/
│   │       └── Medianos.java
│   ├── repositories/
│   │   └── MedianosRepository.java
│   └── services/
│       └── MedianoService.java
└── src/main/resources/
    └── application.properties
```

## API Endpoints

### Base URL
Acceso a través del API Gateway: `http://localhost:8080/api/mediano`  
Acceso directo: `http://localhost:8081/api/mediano`

### Endpoints Disponibles

#### 1. Crear Producto
- **Método**: `POST /api/mediano`
- **Estado HTTP**: `201 CREATED`
- **Descripción**: Crea un nuevo producto mediano
- **Body**: `MedianoRequest`

#### 2. Obtener Todos los Productos
- **Método**: `GET /api/mediano`  
- **Estado HTTP**: `200 OK`
- **Descripción**: Obtiene la lista completa de productos medianos
- **Respuesta**: Lista de `MedianoResponse`

## Configuración

### Base de Datos (PostgreSQL)
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.url=jdbc:postgresql://localhost:5432/ms_products
spring.datasource.username=josedjaykv
spring.datasource.password=Test123
```

### Registro en Eureka
```properties
eureka.client.service-url.default-zone=http://localhost:8761/eureka
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
```

## Integración con Docker
El servicio utiliza una base de datos PostgreSQL containerizada:
```yaml
db-products:
  container_name: db-products
  image: postgres:15.2
  environment:
    POSTGRES_DB: ms_products
    POSTGRES_USER: josedjaykv
    POSTGRES_PASSWORD: Test123
  ports:
    - 5432:5432
```

## Descubrimiento de Servicios
- **Nombre del servicio**: `products-service`
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

## Ejecución
1. Asegurar que PostgreSQL esté ejecutándose (puerto 5432)
2. Verificar que Eureka Server esté activo (puerto 8761)
3. Ejecutar el servicio: `mvn spring-boot:run`
4. El servicio estará disponible en el puerto 8081
5. Verificar registro en Eureka: `http://localhost:8761`
