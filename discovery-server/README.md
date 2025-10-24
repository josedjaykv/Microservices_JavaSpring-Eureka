# Discovery Server - Servidor de Descubrimiento con Eureka

## Descripción
El **Discovery Server** es el componente central de la arquitectura de microservicios que implementa Netflix Eureka. Su función principal es **registrar y descubrir servicios automáticamente**, eliminando la necesidad de configuración manual de direcciones IP y puertos de los microservicios.

## Características Principales
- **Puerto**: 8761 (Puerto estándar de Eureka)
- **Tecnología**: Netflix Eureka Server
- **Función**: Service Discovery y Service Registry
- **Interface Web**: Dashboard de monitoreo incluido
- **Autoregistro**: No se registra a sí mismo (configuración especial)

## ¿Qué es Netflix Eureka?

### Concepto Fundamental
**Eureka** es una solución de **Service Discovery** desarrollada por Netflix para arquitecturas de microservicios que resuelve el problema de **"¿dónde están mis servicios?"**

### Problema que Resuelve
En una arquitectura tradicional:
```
Cliente → http://inventory-service:8083/api/inventory
Cliente → http://orders-service:8082/api/order
Cliente → http://products-service:8081/api/mediano
```

**Problemas:**
- ¿Qué pasa si el servicio cambia de puerto?
- ¿Cómo manejar múltiples instancias del mismo servicio?
- ¿Cómo detectar servicios que fallan?

### Solución con Eureka
```
Cliente → API Gateway → Eureka (¿dónde está inventory-service?) → instancia disponible
```

## Arquitectura de Eureka

### Componentes Principales

#### 1. **Eureka Server** (Este proyecto)
- **Registro central**: Mantiene la lista de todos los servicios
- **Health monitoring**: Monitorea la salud de los servicios
- **Web dashboard**: Interface visual para administradores

#### 2. **Eureka Client** (Los microservicios)
- **Autoregistro**: Se registran automáticamente al iniciar
- **Heartbeat**: Envían señales de vida cada 30 segundos
- **Service lookup**: Consultan ubicación de otros servicios

### Flujo de Funcionamiento

#### Registro de Servicios
```
1. Microservicio inicia
   ↓
2. Se conecta a Eureka Server (localhost:8761)
   ↓
3. Envía información de registro:
   - Nombre del servicio
   - IP y puerto
   - Metadata adicional
   ↓
4. Eureka confirma registro
   ↓
5. Servicio aparece en dashboard
```

#### Descubrimiento de Servicios
```
1. API Gateway necesita llamar a inventory-service
   ↓
2. Consulta Eureka: "¿Dónde está inventory-service?"
   ↓
3. Eureka responde: [instancia1:8083, instancia2:8084]
   ↓
4. Load balancer elige una instancia
   ↓
5. Se realiza la llamada
```

#### Monitoreo de Salud
```
Cada 30 segundos:
Microservicio → Heartbeat → Eureka Server

Si no hay heartbeat por 90 segundos:
Eureka marca el servicio como DOWN
```

## Configuración del Servidor

### Estructura del Proyecto
```
discovery-server/
├── src/main/java/com/josedjaykv/discovery_server/
│   └── DiscoveryServerApplication.java
└── src/main/resources/
    └── application.properties
```

### Clase Principal
```java
@SpringBootApplication
@EnableEurekaServer  // ← Esta anotación habilita Eureka Server
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

### Configuración Completa
```properties
# Identificación del servicio
spring.application.name=discovery-server
server.port=8761

# Configuración específica de Eureka Server
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false

# Configuración crítica: NO auto-registrarse
eureka.client.register-with-eureka=false  # No se registra a sí mismo
eureka.client.fetch-registry=false        # No busca otros registros
eureka.client.service-url.defaultzone=http://${eureka.instance.hostname}:${server.port}/eureka/

# Configuración adicional para clientes
eureka.client.service-url.default-zone=http://localhost:8761/eureka
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
```

## ¿Cómo se Encuentran los Microservicios Automáticamente?

### 1. **Proceso de Registro Automático**

#### Configuración en Microservicios
Cada microservicio incluye:
```properties
# En products-service/application.properties
eureka.client.service-url.default-zone=http://localhost:8761/eureka
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
```

#### Dependencia en pom.xml
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 2. **Flujo Detallado de Auto-descubrimiento**

#### Al Iniciar un Microservicio
```
products-service inicia:
1. Lee configuración: eureka.client.service-url.default-zone
2. Se conecta a http://localhost:8761/eureka
3. Envía datos de registro:
   {
     "app": "PRODUCTS-SERVICE",
     "instanceId": "products-service:random123",
     "hostName": "localhost",
     "port": 8081,
     "status": "UP",
     "healthCheckUrl": "http://localhost:8081/actuator/health"
   }
4. Eureka almacena la información
5. Servicio visible en dashboard
```

#### Al Buscar un Servicio
```
orders-service necesita inventory-service:
1. Consulta cache local (actualizado cada 30s)
2. Si no está en cache, consulta Eureka:
   GET http://localhost:8761/eureka/apps/INVENTORY-SERVICE
3. Eureka responde:
   {
     "instances": [
       {"hostName": "localhost", "port": 8083, "status": "UP"}
     ]
   }
4. orders-service actualiza su cache
5. Usa la información para hacer la llamada
```

### 3. **Algoritmo de Descubrimiento**

#### Service Registry (Registro)
```
Map<String, List<ServiceInstance>> serviceRegistry = {
  "PRODUCTS-SERVICE": [
    {ip: "localhost", port: 8081, status: "UP", lastHeartbeat: now()}
  ],
  "INVENTORY-SERVICE": [
    {ip: "localhost", port: 8083, status: "UP", lastHeartbeat: now()}
  ],
  "ORDERS-SERVICE": [
    {ip: "localhost", port: 8082, status: "UP", lastHeartbeat: now()}
  ],
  "API-GATEWAY": [
    {ip: "localhost", port: 8080, status: "UP", lastHeartbeat: now()}
  ]
}
```

#### Service Discovery (Búsqueda)
```java
// Cuando API Gateway busca inventory-service:
public List<ServiceInstance> findService(String serviceName) {
    List<ServiceInstance> instances = serviceRegistry.get(serviceName.toUpperCase());
    return instances.stream()
                   .filter(instance -> instance.getStatus().equals("UP"))
                   .filter(instance -> isHealthy(instance))
                   .collect(toList());
}
```

## Dashboard Web de Eureka

### Acceso
- **URL**: `http://localhost:8761`
- **Función**: Monitoreo visual de servicios registrados

### Información Mostrada
1. **System Status**: Estado del propio Eureka Server
2. **DS Replicas**: Réplicas del Discovery Server (en este caso, solo una)
3. **Instances currently registered with Eureka**:
   - Nombre del servicio
   - Estado (UP/DOWN)
   - Zona de disponibilidad
   - Último heartbeat

### Ejemplo de Vista
```
Currently registered services:
┌─────────────────┬────────┬─────────────────────────┐
│ Application     │ AMIs   │ Availability Zones      │
├─────────────────┼────────┼─────────────────────────┤
│ API-GATEWAY     │ n/a(1) │ (1) localhost:8080      │
│ INVENTORY-SERVICE│ n/a(1) │ (1) localhost:8083      │
│ ORDERS-SERVICE  │ n/a(1) │ (1) localhost:8082      │
│ PRODUCTS-SERVICE│ n/a(1) │ (1) localhost:8081      │
└─────────────────┴────────┴─────────────────────────┘
```

## Ventajas del Auto-descubrimiento

### 1. **Escalabilidad Dinámica**
```
Antes:
- Configurar manualmente cada nueva instancia
- Actualizar load balancers
- Cambiar configuraciones de clientes

Con Eureka:
- Nueva instancia se registra automáticamente
- Load balancing automático
- Zero downtime para escalamiento
```

### 2. **Alta Disponibilidad**
```
Fallo de servicio:
1. Heartbeat se detiene
2. Eureka marca como DOWN después de 90s
3. Clientes dejan de llamar instancia fallida
4. Tráfico se redistribuye automáticamente

Recuperación:
1. Servicio se reinicia
2. Se registra automáticamente
3. Recibe tráfico nuevamente
```

### 3. **Configuración Zero-Touch**
```
Desarrollador solo necesita:
1. Agregar dependencia eureka-client
2. Configurar eureka.client.service-url.default-zone
3. El resto es automático
```

## Tecnologías Utilizadas

### Netflix Eureka Server
- **Service Registry**: Almacén central de servicios
- **Health Monitoring**: Monitoreo automático de salud
- **Web Dashboard**: Interface de administración
- **REST API**: API para operaciones programáticas

### Dependencias Principales
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

## Patrones de Diseño Implementados

### 1. **Service Registry Pattern**
- **Registro centralizado**: Todos los servicios se registran en un lugar
- **Lookup distribuido**: Cualquier servicio puede buscar otros

### 2. **Health Check Pattern**
- **Heartbeat mechanism**: Servicios reportan su estado regularmente
- **Automatic failure detection**: Detección automática de fallos

### 3. **Load Balancer Pattern**
- **Multiple instances**: Soporte para múltiples instancias del mismo servicio
- **Automatic distribution**: Distribución automática de carga

## Configuraciones Avanzadas

### Self-Preservation Mode
Eureka puede entrar en "modo de preservación" si detecta muchas desconexiones simultáneas, asumiendo problemas de red en lugar de fallos masivos de servicios.

### Heartbeat Intervals
```properties
# Configuraciones de timing (valores por defecto)
eureka.instance.lease-renewal-interval-in-seconds=30  # Heartbeat cada 30s
eureka.instance.lease-expiration-duration-in-seconds=90  # Marca DOWN después de 90s
eureka.client.registry-fetch-interval-seconds=30  # Actualiza cache cada 30s
```

## Monitoreo y Troubleshooting

### Logs Importantes
```
# Registro exitoso
Eureka server started
...
Registered instance PRODUCTS-SERVICE/products-service:random123 with status UP

# Heartbeat normal
Renewed lease for instance PRODUCTS-SERVICE/products-service:random123

# Detección de fallo
Cancelled instance INVENTORY-SERVICE/inventory-service:random456 (replication=false)
```

### Verificación de Funcionamiento
1. **Dashboard**: `http://localhost:8761`
2. **API REST**: `http://localhost:8761/eureka/apps`
3. **Logs**: Verificar registros y heartbeats
4. **Health**: Verificar estado de servicios registrados

## Ejecución
1. **Iniciar primero**: El Discovery Server debe iniciarse antes que los microservicios
2. **Comando**: `mvn spring-boot:run`
3. **Verificación**: Acceder a `http://localhost:8761`
4. **Iniciar microservicios**: Verificar que aparezcan en el dashboard

El Discovery Server con Eureka es el componente fundamental que permite la **comunicación automática y dinámica** entre microservicios, eliminando la configuración manual y habilitando una arquitectura verdaderamente escalable y resiliente.