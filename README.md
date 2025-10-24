# Arquitectura de Microservicios - Sistema de Gestión de Medianos

## Descripción General
Este proyecto implementa una **arquitectura de microservicios completa** usando Spring Boot y Spring Cloud para gestionar un sistema de productos "Medianos", inventario y órdenes. El sistema demuestra patrones fundamentales de microservicios como Service Discovery, API Gateway, comunicación entre servicios y load balancing.

## 🏗️ Arquitectura del Sistema

```
                                    ┌─────────────────────┐
                                    │   Discovery Server  │
                                    │     (Eureka)        │
                                    │    Puerto: 8761     │
                                    └─────────┬───────────┘
                                              │ Service Registry
                    ┌─────────────────────────┼─────────────────────────┐
                    │                         │                         │
    ┌───────────────▼──────────────┐         │         ┌─────────────────▼─────────────┐
    │         Cliente              │         │         │      API Gateway             │
    │                              │         │         │     Puerto: 8080             │
    │  http://localhost:8080       │◄────────┼────────►│   Punto único de entrada     │
    └──────────────────────────────┘         │         └─────────────┬─────────────────┘
                                              │                       │ Load Balancing
                                              │         ┌─────────────┼─────────────────┐
                                              │         │             │                 │
                ┌─────────────────────────────┼─────────▼─┐ ┌─────────▼──┐ ┌──────────▼─────┐
                │                             │           │ │            │ │                │
    ┌───────────▼──────────────┐ ┌───────────▼──────────────▼─────────────▼──────────────────▼─┐
    │   Products Service       │ │                    Business Logic Layer                     │
    │   Puerto: 8081          │ │                                                              │
    │   Base: PostgreSQL      │ │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
    │   /api/mediano/**       │ │  │ Products Service│ │ Inventory Service│ │ Orders Service  │ │
    └─────────┬───────────────┘ │  │   Puerto: 8081  │ │   Puerto: 8083  │ │   Puerto: 8082  │ │
              │                 │  │   PostgreSQL    │ │   PostgreSQL    │ │   MySQL         │ │
              │                 │  │                 │ │                 │ │                 │ │
    ┌─────────▼───────────────┐ │  │  Gestión de     │ │  Validación de  │ │  Procesamiento  │ │
    │   db-products           │ │  │  productos      │ │  inventario     │ │  de órdenes     │ │
    │   PostgreSQL:5432       │ │  │  "Medianos"     │ │  stock          │ │                 │ │
    └─────────────────────────┘ │  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
                                └──────┬─────────────────────┬─────────────────────┬───────────┘
                                       │                     │                     │
                        ┌──────────────▼──────────┐ ┌───────▼───────────┐ ┌───────▼──────────┐
                        │     db-products         │ │   db-inventory    │ │   db-orders      │
                        │   PostgreSQL:5432       │ │ PostgreSQL:5431   │ │  MySQL:3306      │
                        └─────────────────────────┘ └───────────────────┘ └──────────────────┘
```

## 🎯 Componentes del Sistema

### 1. **Discovery Server (Eureka)** - `discovery-server/`
- **Puerto**: 8761
- **Función**: Registro y descubrimiento automático de servicios
- **Tecnología**: Netflix Eureka Server
- **Dashboard**: `http://localhost:8761`

### 2. **API Gateway** - `api-gateway/`
- **Puerto**: 8080 (Punto de entrada principal)
- **Función**: Enrutamiento inteligente y load balancing
- **Tecnología**: Spring Cloud Gateway
- **Rutas configuradas**:
  - `/api/mediano/**` → products-service
  - `/api/inventory/**` → inventory-service  
  - `/api/order/**` → orders-service

### 3. **Products Service** - `products-service/`
- **Puerto**: 8081
- **Base de datos**: PostgreSQL (puerto 5432)
- **Función**: Gestión del catálogo de productos "Medianos"
- **Endpoints**:
  - `POST /api/mediano` - Crear producto
  - `GET /api/mediano` - Listar productos

### 4. **Inventory Service** - `inventory-service/`
- **Puerto**: 8083
- **Base de datos**: PostgreSQL (puerto 5431)
- **Función**: Gestión de inventario y validación de stock
- **Endpoints**:
  - `GET /api/inventory/{sku}` - Verificar stock individual
  - `POST /api/inventory/in-stock` - Verificar stock en lote

### 5. **Orders Service** - `orders-service/`
- **Puerto**: 8082
- **Base de datos**: MySQL (puerto 3306)
- **Función**: Procesamiento de órdenes con validación de inventario
- **Endpoints**:
  - `POST /api/order` - Crear orden
  - `GET /api/order` - Listar órdenes

## 🔄 Flujo de Comunicación

### Flujo Típico: Crear una Orden
```
1. Cliente → API Gateway
   POST http://localhost:8080/api/order
   
2. API Gateway → Eureka
   "¿Dónde está orders-service?"
   
3. Eureka → API Gateway  
   "orders-service está en localhost:8082"
   
4. API Gateway → Orders Service
   POST http://localhost:8082/api/order
   
5. Orders Service → Eureka
   "¿Dónde está inventory-service?"
   
6. Orders Service → Inventory Service
   POST http://localhost:8083/api/inventory/in-stock
   
7. Inventory Service → Orders Service
   {"success": true, "message": "Stock disponible"}
   
8. Orders Service → Base de datos
   Guarda la orden en MySQL
   
9. Orders Service → API Gateway → Cliente
   "Order placed successfully"
```

## 🚀 Tecnologías Utilizadas

### Framework Principal
- **Spring Boot**: 3.5.6
- **Spring Cloud**: 2025.0.0
- **Java**: 21

### Microservicios
- **Spring Cloud Gateway**: API Gateway
- **Netflix Eureka**: Service Discovery
- **Spring Data JPA**: Persistencia de datos
- **Spring WebFlux**: Comunicación reactiva

### Bases de Datos
- **PostgreSQL**: 15.2 (Products e Inventory)
- **MySQL**: 8.0.33 (Orders)

### Herramientas
- **Maven**: Gestión de dependencias
- **Docker Compose**: Orquestación de bases de datos
- **Lombok**: Reducción de código boilerplate

## 🛠️ Configuración y Ejecución

### Prerrequisitos
- Java 21
- Maven 3.6+
- Docker y Docker Compose

### 1. Iniciar Bases de Datos
```bash
docker-compose up -d
```

### 2. Iniciar Servicios (En orden)
```bash
# 1. Discovery Server (OBLIGATORIO PRIMERO)
cd discovery-server
mvn spring-boot:run

# 2. API Gateway
cd ../api-gateway  
mvn spring-boot:run

# 3. Microservicios (pueden iniciarse en paralelo)
cd ../products-service
mvn spring-boot:run &

cd ../inventory-service
mvn spring-boot:run &

cd ../orders-service
mvn spring-boot:run &
```

### 3. Verificar Funcionamiento
```bash
# Verificar registro en Eureka
curl http://localhost:8761

# Probar API Gateway
curl http://localhost:8080/api/mediano
curl http://localhost:8080/api/inventory/PROD001
```

## 📊 Puertos y Servicios

| Servicio | Puerto | Base de datos | URL de acceso |
|----------|---------|---------------|---------------|
| Discovery Server | 8761 | - | http://localhost:8761 |
| API Gateway | 8080 | - | http://localhost:8080 |
| Products Service | 8081 | PostgreSQL:5432 | http://localhost:8080/api/mediano |
| Orders Service | 8082 | MySQL:3306 | http://localhost:8080/api/order |
| Inventory Service | 8083 | PostgreSQL:5431 | http://localhost:8080/api/inventory |

## 🔧 Bases de Datos

### Docker Compose - Configuración
```yaml
# PostgreSQL para Products (puerto 5432)
db-products:
  image: postgres:15.2
  environment:
    POSTGRES_DB: ms_products
    POSTGRES_USER: josedjaykv
    POSTGRES_PASSWORD: Test123

# PostgreSQL para Inventory (puerto 5431)  
db-inventory:
  image: postgres:15.2
  environment:
    POSTGRES_DB: ms_inventory
    POSTGRES_USER: josedjaykv
    POSTGRES_PASSWORD: Test123
  command: -p 5431

# MySQL para Orders (puerto 3306)
db-orders:
  image: mysql:8.0.33
  environment:
    MYSQL_DATABASE: ms_orders
    MYSQL_USER: josedjaykv
    MYSQL_PASSWORD: Test123
```

## 🎛️ Patrones de Microservicios Implementados

### 1. **Service Discovery Pattern**
- Descubrimiento automático de servicios vía Eureka
- No configuración manual de IPs/puertos
- Registro y desregistro automático

### 2. **API Gateway Pattern**  
- Punto único de entrada para clientes
- Enrutamiento inteligente basado en paths
- Load balancing automático

### 3. **Database per Service Pattern**
- Cada microservicio tiene su propia base de datos
- Aislamiento completo de datos
- Diferentes tecnologías de BD por servicio

### 4. **Inter-Service Communication**
- Comunicación síncrona vía HTTP/REST
- WebClient con load balancing
- Service-to-service discovery

### 5. **Circuit Breaker Pattern** (Implícito)
- Spring Cloud incluye resilencia por defecto
- Manejo de fallos en comunicación
- Timeouts y reintentos automáticos

## 🧪 Testing de la Arquitectura

### Crear Producto
```bash
curl -X POST http://localhost:8080/api/mediano \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "MEDIANO001",
    "name": "Grumpy el Gruñón", 
    "height": 1.2,
    "weight": 45.5,
    "description": "Mediano especialista en minería",
    "price": 299.99,
    "status": true
  }'
```

### Verificar Inventario
```bash
curl http://localhost:8080/api/inventory/MEDIANO001
```

### Crear Orden
```bash
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderItems": [
      {
        "sku": "MEDIANO001",
        "price": 299.99,
        "quantity": 2
      }
    ]
  }'
```

## 📈 Ventajas de la Arquitectura

### Escalabilidad
- **Horizontal**: Múltiples instancias por servicio
- **Automática**: Load balancing transparente
- **Selectiva**: Escalar solo servicios necesarios

### Resiliencia
- **Fault tolerance**: Fallos aislados por servicio
- **Health monitoring**: Detección automática de fallos
- **Self-healing**: Recuperación automática

### Desarrollo
- **Equipos independientes**: Cada servicio puede desarrollarse independientemente
- **Tecnologías diversas**: Diferentes stacks por servicio
- **Deploy independiente**: Releases sin afectar otros servicios

### Mantenibilidad
- **Responsabilidad única**: Cada servicio con propósito específico
- **Bajo acoplamiento**: Servicios independientes
- **Alta cohesión**: Funcionalidad relacionada agrupada

## 🔍 Monitoreo y Observabilidad

### Eureka Dashboard
- **URL**: http://localhost:8761
- **Información**: Servicios registrados, estado, heartbeats

### Logs Distribuidos
Cada servicio mantiene logs independientes para:
- Requests HTTP
- Comunicación inter-servicio
- Errores y excepciones
- Heartbeats con Eureka

### Health Checks
Spring Boot Actuator proporciona endpoints de salud automáticos para cada servicio.

## 📚 Documentación Individual

Cada componente tiene documentación detallada:
- [Discovery Server README](discovery-server/README.md)
- [API Gateway README](api-gateway/README.md)  
- [Products Service README](products-service/README.md)
- [Inventory Service README](inventory-service/README.md)
- [Orders Service README](orders-service/README.md)

## 🚀 Próximos Pasos

### Mejoras Sugeridas
1. **Seguridad**: Implementar OAuth2/JWT
2. **Monitoring**: Añadir Micrometer + Prometheus
3. **Tracing**: Implementar Sleuth para trazabilidad
4. **Configuration**: Spring Cloud Config Server
5. **Testing**: Contratos con Spring Cloud Contract

Esta arquitectura proporciona una base sólida para un sistema de microservicios escalable, mantenible y resiliente.
