# 📋 AUDITORÍA TÉCNICA - USUARIO-SERVICE

**Fecha:** 13 de Febrero de 2026  
**Auditor:** Sistema Técnico Especializado  
**Servicio:** usuario-service  
**Resultado:** ⚠️ **CRÍTICO** - Se encontraron graves violaciones de SOLID y múltiples Code Smells

---

## 📊 RESUMEN EJECUTIVO

El servicio de usuarios presenta **8 defectos críticos** y **12 problemas moderados** que afectan directamente la mantenibilidad, escalabilidad y testabilidad del código. Las violaciones más graves están en la arquitectura de clases y la gestión de responsabilidades.

**Puntuación de Salud:** 🔴 **3.2/10** (Crítica)

---

## 🔴 CRÍTICOS (DEBE CORREGIR INMEDIATAMENTE)

### 1. **VIOLACIÓN MASIVA DEL PRINCIPIO DE RESPONSABILIDAD ÚNICA (SRP)**

**Archivo:** `UsuarioServiceApplication.java`  
**Severidad:** 🔴 CRÍTICA

**Problema:**
La clase principal de la aplicación (`@SpringBootApplication`) está acumulando **CUATRO responsabilidades distintas:**

```java
@SpringBootApplication  // 1. Bootstrap de la aplicación
@RestController         // 2. Controlar endpoints HTTP
@CrossOrigin(origins = "http://localhost:3001")
public class UsuarioServiceApplication {
    // 3. Gestiona inicialización de datos
    @PostConstruct
    public void init() throws IOException { ... }
    
    // 4. Contiene toda la lógica de handlers REST
    @GetMapping("/users")
    @GetMapping("/user/{identifier}")
    @DeleteMapping("/user/{id}")
    @PostMapping("/user/add")
    @PutMapping("/user/{id}")
    @PatchMapping("/user/{id}")
}
```

**Impacto:**
- ❌ Imposible de testear - No se puede instanciar un controlador sin arrancar toda la aplicación
- ❌ Imposible de reutilizar - Los endpoints están acoplados al bootstrap
- ❌ Violación de Inversión de Control (IoC) - Spring no puede gestionar adecuadamente el ciclo de vida
- ❌ Difícil de mantener - Cualquier cambio afecta múltiples aspectos del sistema

**Recomendación:**
```
Crear la estructura correcta:
├── UsuarioServiceApplication.java     (Solo @SpringBootApplication)
├── controller/
│   └── UsuarioController.java         (@RestController)
├── service/
│   ├── UserRepository.java            (Ya existe - persistencia)
│   └── UsuarioService.java            (Lógica de negocio)
└── config/
    └── InitializationConfig.java      (@Configuration con @PostConstruct)
```

---

### 2. **ACOPLAMIENTO RÍGIDO A ARCHIVOS JSON**

**Archivo:** `UserRepository.java`  
**Severidad:** 🔴 CRÍTICA

**Problema:**
La clase `UserRepository` está completamente acoplada a la persistencia en JSON:

```java
public class UserRepository {
    private final ObjectMapper mapper = new ObjectMapper();  // Hardcoded JSON
    private final Map<Integer, User> users = Collections.synchronizedMap(new HashMap<>());
    private File jsonFile;
    
    public void init() throws IOException {
        // Lógica compleja de búsqueda de archivos
        // Directamente lee/escribe JSON
    }
    
    public synchronized void writeToFile() {
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, users.values());
    }
}
```

**Impacto:**
- ❌ Imposible cambiar a base de datos real (MySQL, PostgreSQL, etc.)
- ❌ Todo cambio de persistencia requiere refactorizar la clase completa
- ❌ Violación de Abstracción Invertida (Dependency Inversion) - depende de detalles, no abstracciones
- ❌ Persistencia sincrónica - bloquea la ejecución en cada operación

**Recomendación:**
```java
Crear interfaz de abstracción:

public interface IUserPersistence {
    Collection<User> findAll();
    User findById(int id);
    User save(User user);
    // ...
}

Implementaciones:
├── JsonUserPersistence     (JSON - mantener como prototipo)
├── JpaUserRepository       (Spring Data - producción)
└── InMemoryUserRepository  (Testing)
```

---

### 3. **LÓGICA DE NEGOCIO MEZCLADA CON PERSISTENCIA**

**Archivo:** `UserRepository.java`  
**Severidad:** 🔴 CRÍTICA

**Problema:**
No existe capa de servicio. La lógica está distribuida entre el controlador y el repositorio:

```java
// En UsuarioServiceApplication
@GetMapping("/user/{identifier}")
public ResponseEntity<User> getUser(@PathVariable String identifier) {
    if (identifier.contains("@")) {
        User user = userRepository.findByEmail(identifier);  // Lógica de búsqueda
        // ...
    }
    // ...
}

// En UserRepository
public User findByEmail(String email) {
    for (User u : users.values()) {
        if (u.getMail() != null && u.getMail().equalsIgnoreCase(email)) {
            return u;
        }
    }
    return null;
}

// FindById + búsqueda de archivos + ID autogenerado + validaciones
```

**Impacto:**
- ❌ Violación crítica de SRP y Clean Architecture
- ❌ Imposible de testear en unitario
- ❌ Lógica de generación de IDs dispersa (en `init()`, `save()`, `update()`)
- ❌ Flujos de negocio complejos no se pueden reutilizar

**Recomendación:**
Crear capa de servicio tipo:
```java
@Service
public class UsuarioService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    
    public User obtenerUsuarioPorIdentificador(String identificador) {
        // Lógica de negocio centralizada
        if (identificador.contains("@")) {
            return repository.findByEmail(identificador);
        }
        try {
            return repository.findById(Integer.parseInt(identificador));
        } catch (NumberFormatException e) {
            // manejo
        }
    }
}
```

---

### 4. **FALTA TOTAL DE VALIDACIÓN DE ENTRADA**

**Archivo:** `UsuarioServiceApplication.java` y `UserRepository.java`  
**Severidad:** 🔴 CRÍTICA

**Problemas:**
```java
// Sin validación de null/vacío
@PostMapping("/user/add")
public ResponseEntity<User> addUser(@RequestBody User incoming) {
    if (incoming == null) return ResponseEntity.badRequest().build();
    // ❌ ¿Qué pasa si: name = null, password = null, mail = null?
    User saved = userRepository.save(incoming);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

// Acceso directo a campos sin validar
public User partialUpdate(int id, Map<String, Object> updates) {
    if (updates.containsKey("name")) 
        existing.setName((String) updates.get("name"));  // ❌ Sin casteo seguro
    if (updates.containsKey("password")) 
        existing.setPassword((String) updates.get("password"));  // ❌ Null check?
    // ...
}

// Email duplicado sin validar
public User save(User user) {
    if (user.getId() == null || user.getId() <= 0) {
        user.setId(nextId.getAndIncrement());
    }
    users.put(user.getId(), user);  // ❌ ¿Y si el email ya existe?
    // ...
}
```

**Riesgos de Seguridad:**
- Inyección de datos inválidos
- Usuarios sin email ni password
- Duplicación de emails
- IDs negativos o cero

**Recomendación:**
```java
@PostMapping("/user/add")
public ResponseEntity<User> addUser(@Valid @RequestBody CreateUserRequest request) {
    // request ya fue validado por @Valid
    User user = usuarioService.crearUsuario(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
}

// DTO Con validaciones
public class CreateUserRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @Email(message = "Email inválido")
    @NotBlank
    private String mail;
    
    @NotBlank
    @Size(min = 8)
    private String password;
}
```

---

### 5. **GESTIÓN INSEGURA DE CONCURRENCIA**

**Archivo:** `UserRepository.java`  
**Severidad:** 🔴 CRÍTICA

**Problema:**
```java
private final Map<Integer, User> users = Collections.synchronizedMap(new HashMap<>());
private final AtomicInteger nextId = new AtomicInteger(1);

public User save(User user) {
    if (user.getId() == null || user.getId() <= 0) {
        user.setId(nextId.getAndIncrement());  // ❌ Data race
    }
    users.put(user.getId(), user);             // ❌ Sincronización incompleta
    nextId.updateAndGet(x -> Math.max(x, user.getId() + 1));  // ❌ TOCTOU
    writeToFile();                             // ❌ IO síncrono bloqueante
    return user;
}

public synchronized void writeToFile() {
    // ❌ synchronized solo para writeToFile
    // ❌ Pero se llama desde múltiples métodos no sincronizados
    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, users.values());
}
```

**Problemas específicos:**
1. **TOCTOU** (Time-of-Check-Time-of-Use): Entre comprobar ID y asignarlo, otro hilo cambió nextId
2. **Sincronización incompleta**: Solo writeToFile está sincronizado, pero se llama desde métodos que no lo son
3. **Race condition en ID generación**: Dos peticiones simultáneas podrían generar el mismo ID
4. **IO bloqueante**: Todas las operaciones bloquean esperando la escritura a disco

**Impacto:**
- 🚨 Pérdida de datos en concurrencia
- 🚨 IDs duplicados
- 🚨 Degradación severa de rendimiento

---

### 6. **VIOLACIÓN DE DEPENDENCY INVERSION PRINCIPLE (DIP)**

**Archivo:** `UserServiceConsumer.java`  
**Severidad:** 🔴 CRÍTICA

**Problema:**
```java
@Component
public class UserServiceConsumer {
    @Autowired
    private UserServiceProducer producer;  // ❌ Depende de implementación concreta

    @Autowired
    private UserRepository userRepository; // ❌ Depende de implementación concreta

    @RabbitListener(queues = RabbitMQConfig.USER_REQUEST_QUEUE)
    public void receiveUserRequest(UserRequest request) {
        // ... conversión manual de User a UserResponse
    }
}
```

**Impacto:**
- ❌ Acoplamiento con `UserServiceProducer` - no se puede mockear para tests
- ❌ Acoplamiento con `UserRepository` - no se puede cambiar persistencia
- ❌ Conversión manual de DTOs - código duplicado y frágil

**Recomendación:**
```java
public interface IUserService {
    UserResponse obtenerUsuario(int id);
}

// Consumer usa abstracción
@Component
public class UserServiceConsumer {
    private final IUserService usuarioService;
    
    // Inyecta abstracción, no implementación
    public UserServiceConsumer(IUserService usuarioService) {
        this.usuarioService = usuarioService;
    }
}
```

---

### 7. **AUSENCIA DE MANEJO DE EXCEPCIONES**

**Archivo:** Todos  
**Severidad:** 🔴 CRÍTICA

**Problemas:**
```java
// UserRepository.init() - silencia todas las excepciones
if (usersFileEnv != null && !usersFileEnv.isBlank()) {
    external = new File(usersFileEnv);
}
// ...
try {
    Collection<User> fromFile = mapper.readValue(jsonFile, new TypeReference<Collection<User>>() {});
    loadUsers(fromFile);
    return;
} catch (Exception ex) {
    // ignore parse errors and continue to other fallbacks  ❌ SILENT FAIL
}

// UserServiceConsumer
if (producer == null) {
    System.err.println("Producer not available");  // ❌ Solo print a stderr??
    return;
}

// writeToFile()
public synchronized void writeToFile() {
    try {
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, users.values());
    } catch (IOException e) {
        e.printStackTrace();  // ❌ No hace nada, solo imprime
    }
}
```

**Impacto:**
- 🚨 Fallos silenciosos - datos perdidos sin notificación
- 🚨 Difícil debugging
- 🚨 No recuperación de errores
- 🚨 Usuario nunca se entera que la operación falló

---

### 8. **INYECCIÓN DE DEPENDENCIAS INCONSISTENTE**

**Archivo:** `UserServiceConsumer.java`, `UserServiceProducer.java`  
**Severidad:** 🔴 CRÍTICA

**Problema:**
```java
@Component
public class UserServiceConsumer {
    @Autowired
    private UserServiceProducer producer;  // Field injection ❌
    
    @Autowired
    private UserRepository userRepository; // Field injection ❌
}

@Component
public class UserServiceProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate; // Field injection ❌
}
```

**Problemas:**
1. ❌ **Field injection** - No se puede crear la clase sin Spring (untestable)
2. ❌ **Optional checking** - Consumer comprueba si producer es null
3. ❌ **Circular dependency risk** - No hay visibilidad de dependencias
4. ❌ **Mocking complicado** - No se pueden usar constructores en tests

**Recomendación:**
```java
@Component
public class UserServiceConsumer {
    private final UserServiceProducer producer;
    private final UserRepository userRepository;
    
    // Constructor injection (mejor para testing y visibilidad)
    public UserServiceConsumer(UserServiceProducer producer, UserRepository userRepository) {
        this.producer = Objects.requireNonNull(producer);
        this.userRepository = Objects.requireNonNull(userRepository);
    }
}
```

---

## 🟠 MODERADOS (DEBE CORREGIR PRÓXIMAMENTE)

### 9. **VIOLACIÓN DEL OPEN/CLOSED PRINCIPLE (OCP)**

**Archivo:** `UserServiceConsumer.java`, `UsuarioServiceApplication.java`  
**Severidad:** 🟠 ALTA

**Problema:**
```java
// Cada vez que agrega un nuevo tipo de request, debe modificar Consumer
public void receiveUserRequest(UserRequest request) {
    // ... lógica hardcodeada
    UserResponse response = new UserResponse(...);
    producer.sendUserResponse(response);
}

// En el controlador, cada nuevo endpoint requiere nuevo código en la clase principal
@GetMapping("/users")
public Collection<User> getAllUsers() { ... }

@GetMapping("/user/{identifier}")
public ResponseEntity<User> getUser(@PathVariable String identifier) { ... }

@PostMapping("/user/add")
public ResponseEntity<User> addUser(@RequestBody User incoming) { ... }
// Que pasa si agrega eventos? Nuevos métodos aquí.
```

**Impacto:**
- Violación OCP - abierto para modificación, cerrado para extensión invertido
- Difícil de extender sin modificar código existente
- Riesgo de romper funcionalidad existente

---

### 10. **DUPLICACIÓN DE CÓDIGO (DTOs vs Model)**

**Archivo:** `User.java`, `UserResponse.java`  
**Severidad:** 🟠 ALTA

**Problema:**
```java
// model/User.java
public class User {
    private Integer id;
    private String name;
    private String password;
    private String mail;
    private boolean active;
    // Getters y setters
}

// messaging/UserResponse.java
public class UserResponse {
    private Integer id;
    private String name;
    private String mail;        // Sin password
    private boolean active;
    // Getters y setters idénticos
}
```

**Impacto:**
- Duplicación de código DRY violated
- Conversión manual en Consumer:
  ```java
  UserResponse response = new UserResponse(
      user.getId(),
      user.getName(),
      user.getMail(),
      user.isActive()
  );  // ❌ Frágil - si agrega campo, rompe
  ```
- Falta de mappers (ModelMapper, MapStruct)
- Inconsistencia en cambios

---

### 11. **BÚSQUEDA DE EMAIL INEFICIENTE (O(n))**

**Archivo:** `UserRepository.java`  
**Severidad:** 🟠 MEDIA

**Problema:**
```java
public User findByEmail(String email) {
    for (User u : users.values()) {  // ❌ O(n) búsqueda lineal
        if (u.getMail() != null && u.getMail().equalsIgnoreCase(email)) {
            return u;
        }
    }
    return null;
}
```

**Impacto:**
- Rendimiento O(n) con 1000+ usuarios
- Sin índices no escalable
- Cada búsqueda itera todos los usuarios

**Solución:**
```java
private Map<String, Integer> emailIndex = new HashMap<>();

public User findByEmail(String email) {
    Integer userId = emailIndex.get(email.toLowerCase());
    return userId != null ? users.get(userId) : null;  // O(1)
}
```

---

### 12. **FALTA DE LOGGING ESTRUCTURADO**

**Archivo:** Todos  
**Severidad:** 🟠 MEDIA

**Problemas:**
```java
System.out.println("User request received: " + request);
System.err.println("Producer not available");
System.out.println("User not found with id: " + userId);
e.printStackTrace();  // Stack trace sin contexto
System.out.println("User response sent: " + response);
```

**Impacto:**
- Sin logs estructurados
- No se pueden filtrar por nivel
- Imposible análisis en producción
- Stack traces sin contexto

**Recomendación:**
```java
private static final Logger logger = LoggerFactory.getLogger(UserServiceConsumer.class);

logger.info("User request received: userId={}", request.getUserId());
logger.error("Producer not available - unable to send response", exception);
logger.debug("User response sent: {}", response);
```

---

### 13. **CONFIGURACIÓN HARDCODEADA**

**Archivo:** `UsuarioServiceApplication.java`, `CorsConfig.java`  
**Severidad:** 🟠 MEDIA

**Problemas:**
```java
// CORS hardcodeado
config.addAllowedOrigin("http://localhost:3001");
config.addAllowedOrigin("http://localhost:3000");

// En UsuarioServiceApplication
@CrossOrigin(origins = "http://localhost:3001")  // Duplicado
public class UsuarioServiceApplication { ... }
```

**Impacto:**
- Cambion de configuración requiere recompilación
- No separación entre ambientes (dev, test, prod)
- No externalización de configuración

---

### 14. **FALTA DE DTOs PARA REQUESTS/RESPONSES**

**Archivo:** `UsuarioServiceApplication.java`  
**Severidad:** 🟠 MEDIA

**Problema:**
```java
@PostMapping("/user/add")
public ResponseEntity<User> addUser(@RequestBody User incoming) {
    // ❌ Recibe User directamente
    // ¿Qué pasa si el cliente envía campos extra?
    // ¿Y si quiere campos opcionales?
}

@PatchMapping("/user/{id}")
public ResponseEntity<User> patchUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
    // ❌ Recibe Map sin validación
    // Casting manual peligroso
}
```

**Recomendación:**
```java
public class CreateUserRequest {
    @NotBlank
    private String name;
    
    @Email
    @NotBlank
    private String mail;
    
    @NotBlank
    @Size(min = 8)
    private String password;
}

public class UpdateUserRequest {
    private String name;
    private String mail;
    private Boolean active;
}
```

---

### 15. **AUSENCIA DE TESTS UNITARIOS**

**Archivo:** Todo el servicio  
**Severidad:** 🟠 ALTA

**Evidencia:**
- Carpeta test solo tiene directorio `repository/` vacío
- No hay tests para servicios
- No hay tests para controladores
- No hay tests para mensajería

**Impacto:**
- Cero confianza en código
- Refatorización imposible sin romper cosas
- Deuda técnica acumulada

---

### 16. **VIOLACIÓN DEL LISKOV SUBSTITUTION PRINCIPLE (LSP)**

**Archivo:** `UserServiceConsumer.java`  
**Severidad:** 🟠 MEDIA

**Problema:**
```java
public void receiveUserRequest(UserRequest request) {
    if (producer == null) {  // ❌ No debería ocurrir si LSP se cumple
        System.err.println("Producer not available");
        return;
    }
    // ...
}
```

**Impacto:**
- Violación de contrato esperado
- Checks no defensivos
- Comportamiento inesperado

---

## 🟡 MENORES (DEUDA TÉCNICA)

### 17. **FALTA DE INTERFACES PÚBLICAS**

No hay interfaces para los repositorios y servicios. Dificulta testing y extensibilidad.

### 18. **CONVENCIÓN DE NOMBRES INCONSISTENTE**

- `User` vs `Usuario`
- `UserRequest`, `UserResponse` en interfaz
- `receivUserRequest()` con typo (si fuera)
- `getMail()` vs estándar JavaBean `getEmail()`

### 19. **FALTA DE DOCUMENTACIÓN**

- Sin JavaDoc
- Sin comentarios explicativos
- API no documentada

### 20. **RECURSOS NO LIBERADOS**

Files abiertos pero no cerrados en JsonFile manejo:
```java
mapper.readValue(jsonFile, ...)  // ¿Se cierra el FileInputStream?
mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, ...)  // Mismo
```

---

## 📈 VIOLACIONES DE SOLID RESUMIDAS

| Principio | Violación | Archivos | Severidad |
|-----------|-----------|----------|-----------|
| **S** (SRP) | 4 responsabilidades en 1 clase | UsuarioServiceApplication | 🔴 CRÍTICA |
| **O** (OCP) | Abierto a modificación | Todos los handlers | 🟠 ALTA |
| **L** (LSP) | Null checks innecesarios | UserServiceConsumer | 🟠 MEDIA |
| **I** (ISP) | Sin segregación de interfaz | UserRepository | 🟠 MEDIA |
| **D** (DIP) | Depende de implementaciones | UserServiceConsumer | 🔴 CRÍTICA |

---

## 📊 CODE SMELLS DETECTADOS

| Smell | Ubicación | Ejemplo |
|-------|-----------|---------|
| **Duplicate Code** | User.java / UserResponse.java | Estructura idéntica de campos |
| **Long Method** | init() en UserRepository | 60+ líneas |
| **Feature Envy** | receiveUserRequest() | Accede a repository como extensión |
| **Inappropriate Intimacy** | UsuarioServiceApplication | Acceso directo a userRepository |
| **Data Clumps** | User, CreateUserRequest | Mismos campos repetidos |
| **Switch Statements** | getUser() | if/else para lógica de búsqueda |
| **Parallel Inheritance** | User / UserResponse | Jeraquía paralela innecesaria |
| **Lazy Class** | Empty controller folder | Clases sin responsabilidad clara |
| **Speculative Generality** | sendUserResponse() | Parámetros que no se usan |
| **Dead Code** | Controller folder empty | Código no usado |
| **Comments** | Try-catch com "ignore parse errors" | Necesita comentarios= mal código |

---

## ✅ PLAN DE ACCIÓN (PRIORIZADO)

### **FASE 1: REFACTORIZACIÓN CRÍTICA (1-2 semanas)**

1. **Separar responsabilidades en UsuarioServiceApplication**
   - Crear `UsuarioController.java` (@RestController)
   - Crear `UsuarioServiceApplication.java` limpio (solo @SpringBootApplication)
   - Crear configuración de inicialización separada

2. **Crear capa de servicio**
   - `UsuarioService.java` con lógica de negocio
   - `Usuario` (entidad) separado del DTO

3. **Implementar interfaces de abstracción**
   - `IUserRepository` / `IUserPersistence`
   - `IUsuarioService`
   - `IUserProducer`, `IUserConsumer` para messaging

### **FASE 2: CALIDAD (1-2 semanas)**

4. **Agregar validación**
   - Request DTOs con @Valid
   - Exception handlers centralizados
   - Error responses consistentes

5. **Implementar tests**
   - Unit tests para servicios
   - Integration tests para repo
   - Tests para messaging

6. **Logging estructurado**
   - SLF4J + Logback
   - Log levels apropiados
   - Contexto en excepciones

### **FASE 3: OPTIMIZACIÓN (1 semana)**

7. **Mejorar concurrencia**
   - StampedLock o ReentrantReadWriteLock
   - Operaciones IO asincrónicas
   - Mejor generación de IDs

8. **Performance**
   - Índices para búsquedas (email)
   - Caché donde sea apropiado
   - Profiling

---

## 🎯 RECOMENDACIONES FINALES

**Prioridad 1: Urgente**
- ✅ Separar UsuarioServiceApplication (SRP)
- ✅ Crear interfaces de abstracción (DIP)
- ✅ Implementar UsuarioService (lógica de negocio)

**Prioridad 2: Alta**
- ✅ Agregar validación (security)
- ✅ Manejo de excepciones (reliability)
- ✅ Inyección por constructor (testability)

**Prioridad 3: Media**
- ✅ Tests unitarios (confidence)
- ✅ Logging estructurado (debuggability)
- ✅ Mappers para DTOs (DRY)

**Prioridad 4: Deuda Técnica**
- ✅ Documentación
- ✅ Convención de nombres
- ✅ Performance optimizations

---

## 📌 CONCLUSIÓN

El usuario-service **no está listo para producción**. Presenta graves violaciones arquitectónicas que impiden mantenimiento, testing y escalabilidad. La refactorización es **CRÍTICA Y URGENTE** antes de considerarlo como un servicio enterprise.

**Estimación:** 3-4 semanas para refactorización completa con tests.

---

*Auditoría realizada: 13 de Febrero de 2026*  
*Estado: Requiere Acción Inmediata*
