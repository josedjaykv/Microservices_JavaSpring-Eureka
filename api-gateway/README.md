# API Gateway - Puerta de Enlace de Microservicios

## Descripción
El **API Gateway** es el punto de entrada único para todas las solicitudes hacia los microservicios del sistema. Actúa como un proxy inteligente que enruta las peticiones a los servicios correspondientes, proporcionando un punto de acceso centralizado y simplificando la comunicación con la arquitectura de microservicios.

## Características Principales
- **Puerto**: 8080 (Punto de entrada principal del sistema)
- **Enrutamiento inteligente**: Dirige peticiones a microservicios específicos
- **Descubrimiento automático**: Se integra con Eureka para encontrar servicios dinámicamente
- **Load Balancing**: Distribuye carga automáticamente entre múltiples instancias
- **Punto único de acceso**: Simplifica la arquitectura desde la perspectiva del cliente

## ¿Qué es un API Gateway?

### Función Principal
Un API Gateway es un **patrón de diseño de microservicios** que actúa como:
- **Punto de entrada único**: Todos los clientes se conectan a una sola URL
- **Proxy reverso**: Redirige peticiones a servicios internos
- **Agregador de servicios**: Puede combinar respuestas de múltiples servicios
- **Punto de control**: Centraliza políticas de seguridad, monitoreo y rate limiting

### Ventajas del API Gateway
1. **Simplificación para clientes**: Los clientes no necesitan conocer múltiples endpoints
2. **Abstracción de servicios**: Los servicios internos pueden cambiar sin afectar clientes
3. **Balanceador de carga**: Distribuye peticiones automáticamente
4. **Punto de monitoreo**: Centraliza logs y métricas
5. **Seguridad centralizada**: Un punto para implementar autenticación y autorización

## Arquitectura y Configuración

### Estructura del Proyecto
```
api-gateway/
├── src/main/java/com/josedjaykv/api_gateway/
│   └── ApiGatewayApplication.java
└── src/main/resources/
    └── application.properties
```

### Configuración de Rutas
El API Gateway está configurado para enrutar peticiones a tres microservicios:

#### 1. Products Service (Medianos)
```properties
# Ruta para el servicio de productos
spring.cloud.gateway.server.webflux.routes[2].id=product-service
spring.cloud.gateway.server.webflux.routes[2].uri=lb://products-service
spring.cloud.gateway.server.webflux.routes[2].predicates[0]=Path=/api/mediano/**
```
- **Path pattern**: `/api/mediano/**`
- **Service name**: `products-service`
- **Load balancer**: `lb://` indica uso de balanceador de carga

#### 2. Inventory Service
```properties
# Ruta para el servicio de inventario
spring.cloud.gateway.server.webflux.routes[0].id=inventory-service
spring.cloud.gateway.server.webflux.routes[0].uri=lb://inventory-service
spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/api/inventory/**
```
- **Path pattern**: `/api/inventory/**`
- **Service name**: `inventory-service`
- **Load balancer**: Distribuye carga entre instancias disponibles

#### 3. Orders Service
```properties
# Ruta para el servicio de órdenes
spring.cloud.gateway.server.webflux.routes[1].id=orders-service
spring.cloud.gateway.server.webflux.routes[1].uri=lb://orders-service
spring.cloud.gateway.server.webflux.routes[1].predicates[0]=Path=/api/order/**
```
- **Path pattern**: `/api/order/**`
- **Service name**: `orders-service`
- **Load balancer**: Automático vía Eureka

## ¿Cómo Encuentra los Microservicios Automáticamente?

### Integración con Eureka Discovery Server

#### 1. Registro Automático
```properties
# Configuración de cliente Eureka
eureka.client.service-url.default-zone=http://localhost:8761/eureka
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
```

#### 2. Proceso de Descubrimiento
El API Gateway encuentra los microservicios a través del siguiente proceso:

1. **Conexión a Eureka**: Al iniciar, se conecta al Discovery Server (puerto 8761)
2. **Obtención del registro**: Descarga la lista de servicios registrados
3. **Resolución de nombres**: Convierte nombres de servicio a direcciones IP/puerto
4. **Actualización continua**: Mantiene la lista actualizada mediante heartbeats

#### 3. Load Balancing Automático
```
Petición: GET http://localhost:8080/api/inventory/PROD001

Proceso interno del API Gateway:
1. Recibe petición en puerto 8080
2. Analiza path: /api/inventory/PROD001
3. Busca en configuración: Path=/api/inventory/**
4. Encuentra coincidencia con inventory-service
5. Consulta Eureka: ¿Dónde está inventory-service?
6. Eureka responde: [instancia1:8083, instancia2:8084]
7. Load balancer elige una instancia
8. Reenvía petición: GET http://instancia1:8083/api/inventory/PROD001
9. Retorna respuesta al cliente
```

### Ventajas del Descubrimiento Automático

#### 1. **Escalabilidad Dinámica**
- Nuevas instancias se registran automáticamente
- El Gateway las detecta sin reconfiguración
- Load balancing se ajusta automáticamente

#### 2. **Alta Disponibilidad**
- Si una instancia falla, Eureka la marca como no disponible
- El Gateway deja de enviar tráfico a instancias fallidas
- Recuperación automática cuando la instancia se restaura

#### 3. **Configuración Simplificada**
- No necesita IPs hardcodeadas
- Configuración basada en nombres de servicio
- Cambios de infraestructura transparentes

## Flujo de Peticiones

### Ejemplo: Crear una Orden
```http
Cliente -> API Gateway -> Orders Service -> Inventory Service

1. POST http://localhost:8080/api/order
   Body: {"orderItems": [...]}

2. API Gateway analiza path: /api/order
   - Coincide con: Path=/api/order/**
   - Servicio objetivo: orders-service

3. Gateway consulta Eureka: ¿Dónde está orders-service?
   - Eureka: "Instancia disponible en localhost:8082"

4. Gateway reenvía: POST http://localhost:8082/api/order
   Body: {"orderItems": [...]}

5. Orders Service procesa internamente:
   - Valida con Inventory Service (también vía Eureka)
   - Crea orden si hay stock

6. Respuesta retorna por la misma ruta al cliente
```

## Tecnologías Utilizadas

### Spring Cloud Gateway
- **WebFlux**: Framework reactivo para mejor rendimiento
- **Predicates**: Reglas para enrutamiento de peticiones
- **Filters**: Procesamiento de peticiones/respuestas
- **Load Balancer**: Distribución automática de carga

### Dependencias Principales
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## Configuración Completa

### application.properties
```properties
# Configuración básica
spring.application.name=api-gateway
server.port=8080

# Configuración de Eureka Client
eureka.client.service-url.default-zone=http://localhost:8761/eureka
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}

# Rutas de microservicios
# Inventory Service
spring.cloud.gateway.server.webflux.routes[0].id=inventory-service
spring.cloud.gateway.server.webflux.routes[0].uri=lb://inventory-service
spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/api/inventory/**

# Orders Service  
spring.cloud.gateway.server.webflux.routes[1].id=orders-service
spring.cloud.gateway.server.webflux.routes[1].uri=lb://orders-service
spring.cloud.gateway.server.webflux.routes[1].predicates[0]=Path=/api/order/**

# Products Service
spring.cloud.gateway.server.webflux.routes[2].id=product-service
spring.cloud.gateway.server.webflux.routes[2].uri=lb://products-service
spring.cloud.gateway.server.webflux.routes[2].predicates[0]=Path=/api/mediano/**
```

## Patrones de Diseño Implementados

### 1. **Gateway Pattern**
- Punto único de entrada para múltiples servicios
- Abstracción de la complejidad interna

### 2. **Service Discovery Pattern**
- Ubicación automática de servicios
- Desacoplamiento de configuración de infraestructura

### 3. **Load Balancer Pattern**
- Distribución de carga entre instancias
- Mejora en rendimiento y disponibilidad

### 4. **Proxy Pattern**
- Intermediario entre cliente y servicios
- Control y monitoreo de comunicaciones

## Ventajas del Diseño Actual

### Para Desarrolladores
- **Desarrollo simplificado**: Un solo endpoint para todos los servicios
- **Testing fácil**: Punto único para pruebas de integración
- **Debugging centralizado**: Logs y métricas en un lugar

### Para Operaciones
- **Monitoreo centralizado**: Visibilidad completa del tráfico
- **Escalamiento inteligente**: Detección automática de nuevas instancias
- **Configuración simplificada**: Cambios sin reinicio de servicios

### Para Clientes
- **URL única**: `http://localhost:8080` para todo el sistema
- **Consistencia**: Misma estructura de respuesta
- **Rendimiento**: Load balancing automático

## Ejecución y Verificación

### Pasos para Iniciar
1. **Iniciar Eureka Server**: `cd discovery-server && mvn spring-boot:run`
2. **Iniciar microservicios**: Orders, Inventory, Products
3. **Iniciar API Gateway**: `cd api-gateway && mvn spring-boot:run`
4. **Verificar registro**: Visitar `http://localhost:8761`

### Verificación de Funcionamiento
```bash
# Verificar enrutamiento a Products Service
curl http://localhost:8080/api/mediano

# Verificar enrutamiento a Inventory Service  
curl http://localhost:8080/api/inventory/PROD001

# Verificar enrutamiento a Orders Service
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{"orderItems": [...]}'
```

## Monitoreo y Observabilidad

### Eureka Dashboard
- **URL**: `http://localhost:8761`
- **Función**: Visualizar servicios registrados y su estado
- **Información mostrada**: Instancias, health status, metadata

### Logs del Gateway
- **Enrutamiento**: Logs de qué peticiones van a qué servicios
- **Load balancing**: Información de distribución de carga
- **Errores**: Fallos de comunicación con servicios

El API Gateway es el componente clave que hace posible la comunicación fluida y escalable en esta arquitectura de microservicios, proporcionando descubrimiento automático, load balancing y un punto de acceso unificado.