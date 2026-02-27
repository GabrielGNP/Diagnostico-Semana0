# 📋 TEST PLAN - usuario-service

**Proyecto:** usuario-service  
**Fecha Análisis:** 26 de febrero de 2026  
**Cobertura Actual:** 49%  
**Meta:** >80%  
**Gap:** +31 puntos porcentuales

---

## 🎯 RESUMEN EJECUTIVO

### Estado Actual (JaCoCo Report)

```
Cobertura Total:           49% (1.464 de 2.907 instrucciones)
Cobertura de Branches:     25% (59 de 229 branches)
Métodos sin cubrir:        74 de 167 métodos
Líneas sin cubrir:         374 de 743 líneas
Clases sin cubrir:         5 de 30 clases
```

### Objetivo del Plan
Incrementar la cobertura del **49% al 80%+** mediante tests estratégicos enfocados en las áreas con mayor gap de cobertura.

---

## 🔴 PRIORIZACIÓN POR IMPACTO

### Nivel CRÍTICO (4-12% cobertura)

**1. com.example.usuarioservice.messaging - 4%**
- Instrucciones perdidas: 132
- Branches perdidos: 4 (0% cobertura)
- Métodos sin cubrir: 18 de 20
- **Impacto estimado:** +4-5% cobertura total

**2. com.example.usuarioservice.validation - 12%**
- Instrucciones perdidas: 425 (Mayor gap del proyecto)
- Branches perdidos: 60 (0% cobertura)
- Métodos sin cubrir: 17 de 25
- **Impacto estimado:** +14-16% cobertura total

### Nivel ALTO (33-38% cobertura)

**3. com.example.usuarioservice.mapper - 33%**
- Instrucciones perdidas: 85
- Branches perdidos: 16 (11% cobertura)
- Métodos sin cubrir: 3 de 7
- **Impacto estimado:** +3% cobertura total

**4. com.example.usuarioservice.persistence - 38%**
- Instrucciones perdidas: 680 (Segunda mayor brecha)
- Branches perdidos: 65 (18% cobertura)
- Métodos sin cubrir: 28 de 50
- **Impacto estimado:** +23% cobertura total

### Nivel MEDIO (73% cobertura)

**5. com.example.usuarioservice.config - 73%**
- Instrucciones perdidas: 91
- Branches perdidos: 13 (31% cobertura)
- Métodos sin cubrir: 4 de 24
- **Impacto estimado:** +3% cobertura total

---

## 📅 PLAN DE ACCIÓN - 5 FASES

### FASE 1: Validation Tests (CRÍTICO) - Semana 1

**Objetivo:** Llevar validation de 12% → 85%

**Clases a testear:**
- `ValidationContext.java`
- `LenientValidationStrategy.java`
- `StrictValidationStrategy.java`
- `ValidationException.java`

**Tests a crear (~25 tests):**

1. **ValidationContextTest.java** (8 tests)
   - Strategy selection (LENIENT, STRICT)
   - validateForCreation() delegation
   - validateForUpdate() delegation
   - Invalid strategy handling
   - Strategy not found exception
   - Multiple strategies registered
   - Strategy switching
   - Null request handling

2. **LenientValidationStrategyTest.java** (8 tests)
   - Valid CreateUsuarioRequest (nombre, email, contraseña valid)
   - Invalid nombre (null, empty, too short/long)
   - Invalid email (null, invalid format)
   - Invalid contraseña (null, too short)
   - Valid UpdateUsuarioRequest (partial fields)
   - Empty UpdateUsuarioRequest allowed
   - Null fields in update allowed
   - Edge cases (special characters, unicode)

3. **StrictValidationStrategyTest.java** (8 tests)
   - Similar a Lenient pero con reglas más estrictas
   - Strong password validation (mayúscula, minúscula, número, especial)
   - Email domain whitelist
   - Nombre no permite números ni caracteres especiales
   - All fields required en create
   - UpdateUsuarioRequest requires at least one field
   - Length validation más restrictiva
   - Character set validation

4. **ValidationExceptionTest.java** (1 test)
   - Exception message and cause handling

**Estimación:** 6-8 horas  
**Ganancia esperada:** +14-16% cobertura

---

### FASE 2: Messaging Tests (CRÍTICO) - Semana 1

**Objetivo:** Llevar messaging de 4% → 80%

**Clases a testear:**
- `UserServiceConsumer.java`
- `RabbitMQConfig.java`
- Message DTOs (UserRequest, UserResponse)

**Tests a crear (~15 tests):**

1. **UserServiceConsumerTest.java** (10 tests)
   - handleUserRequest() con UserRequest válido
   - Usuario encontrado por ID retorna UserResponse
   - Usuario encontrado por email retorna UserResponse
   - Usuario no encontrado retorna error response
   - Request null handling
   - Request con identificador null
   - Request con identificador inválido
   - Exception en UserRepository propagada
   - Logging verification
   - Response format validation

2. **RabbitMQConfigTest.java** (3 tests)
   - userExchange bean creation
   - userRequestQueue bean creation
   - userResponseQueue bean creation

3. **UserRequestResponseTest.java** (2 tests)
   - UserRequest serialization/deserialization
   - UserResponse serialization/deserialization

**Estimación:** 4-5 horas  
**Ganancia esperada:** +4-5% cobertura

---

### FASE 3: Persistence Tests - Semana 2

**Objetivo:** Llevar persistence de 38% → 80%

**Clases a testear:**
- `UserRepository.java` (JSON persistence)
- `CachedUserPersistenceDecorator.java`
- Integration tests con UserJpaPersistence

**Tests a crear (~20 tests):**

1. **UserRepositoryTest.java** (adicionales - 10 tests)
   - init() carga desde archivo externo (USERS_FILE env)
   - init() carga desde resources
   - init() carga desde target
   - init() crea archivo si no existe
   - writeToFile() persiste cambios
   - save() asigna ID auto-incremental
   - save() con ID existente
   - update() con usuario inexistente
   - partialUpdate() con map vacío
   - Error handling en writeToFile()

2. **CachedUserPersistenceDecoratorTest.java** (10 tests)
   - Cache hit en findById()
   - Cache miss en findById() llama delegate
   - Cache eviction en save()
   - Cache eviction en update()
   - Cache eviction en deleteById()
   - findAll() no usa cache
   - findAllActive() no usa cache
   - findByEmail() cache behavior
   - Cache size limits
   - Cache expiration (si aplica)

**Estimación:** 6-8 horas  
**Ganancia esperada:** +23% cobertura

---

### FASE 4: Mapper Tests - Semana 2

**Objetivo:** Llevar mapper de 33% → 90%

**Clases a testear:**
- `UserEntityMapper.java`

**Tests a crear (~10 tests):**

1. **UserEntityMapperTest.java** (10 tests)
   - toDomain() convierte UserEntity → User correctamente
   - toDomain() con UserEntity null
   - toDomain() con campos null en entity
   - toDomain() preserva todos los campos
   - toEntity() convierte User → UserEntity correctamente
   - toEntity() con User null
   - toEntity() con campos null en user
   - toEntity() no copia ID (para nuevos)
   - Conversión bidireccional (entity → domain → entity)
   - MapStruct mapping verification

**Estimación:** 3-4 horas  
**Ganancia esperada:** +3% cobertura

---

### FASE 5: Config Tests - Semana 3

**Objetivo:** Llevar config de 73% → 85%

**Clases a testear:**
- `UserPersistenceFactory.java`
- `UsuariosInitializationConfig.java`
- Otros beans de configuración

**Tests a crear (~8 tests):**

1. **UserPersistenceFactoryTest.java** (4 tests)
   - createUserPersistence() con JSON mode
   - createUserPersistence() con JPA mode
   - createUserPersistence() con Cache decorator
   - Mode selection por property

2. **UsuariosInitializationConfigTest.java** (4 tests)
   - initializeUsers() se ejecuta al startup
   - initialize() llama a persistence.initialize()
   - Error handling en initialization
   - Logging verification

**Estimación:** 3-4 horas  
**Ganancia esperada:** +3-4% cobertura

---

## 📊 PROYECCIÓN DE COBERTURA

### Ganancia Estimada por Fase

| Fase | Package | Tests | Horas | Ganancia | Cobertura Acum. |
|------|---------|-------|-------|----------|-----------------|
| Inicial | - | - | - | - | 49% |
| FASE 1 | validation | 25 | 6-8h | +14-16% | 63-65% |
| FASE 2 | messaging | 15 | 4-5h | +4-5% | 67-70% |
| FASE 3 | persistence | 20 | 6-8h | +23% | 90-93% |
| FASE 4 | mapper | 10 | 3-4h | +3% | 93-96% |
| FASE 5 | config | 8 | 3-4h | +3-4% | **96-100%** |

**Total tests a crear:** ~78 tests  
**Tiempo total estimado:** 22-29 horas (~3 semanas)  
**Cobertura final esperada:** 96-100% ✅

---

## 🛠️ ESTRATEGIA DE TESTING

### Principios a Aplicar

1. **Prioridad por ROI**
   - Empezar por packages con mayor gap (validation, persistence)
   - Cada test debe maximizar ganancia de cobertura

2. **Testing Pyramid**
   ```
   E2E Tests (10%)         ← Ya cubierto en INFORME_COMPLETO
   Integration Tests (20%) ← FASE 3 (persistence)
   Unit Tests (70%)        ← FASE 1, 2, 4, 5
   ```

3. **Cobertura de Branches**
   - Testear todas las ramas condicionales
   - If-else, switch, try-catch, loops
   - Validaciones (null, empty, invalid)

4. **Edge Cases**
   - Valores límite (min, max, 0, negative)
   - Null safety
   - Empty collections
   - Caracteres especiales

### Herramientas

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 📝 TEMPLATE DE TEST

### Estructura Estándar

```java
package com.example.usuarioservice.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClassName - Method/Feature description")
class ClassNameTest {

    @Mock
    private Dependency dependency;

    @InjectMocks
    private ClassUnderTest classUnderTest;

    @Test
    @DisplayName("methodName should do X when Y")
    void methodName_shouldDoX_whenY() {
        // Given: Setup test data
        InputObject input = new InputObject(...);
        when(dependency.method()).thenReturn(...);

        // When: Execute method under test
        ResultObject result = classUnderTest.method(input);

        // Then: Verify results
        assertNotNull(result);
        assertEquals(expected, result.getValue());
        verify(dependency).method();
    }

    @Test
    @DisplayName("methodName should throw exception when invalid input")
    void methodName_shouldThrowException_whenInvalidInput() {
        // Given
        InputObject invalidInput = null;

        // When & Then
        assertThrows(ValidationException.class, 
            () -> classUnderTest.method(invalidInput));
    }
}
```

---

## ✅ CRITERIOS DE ACEPTACIÓN

### Por Fase

**FASE 1 (Validation):** ✅
- [ ] 25 tests creados y pasando
- [ ] Cobertura validation ≥ 85%
- [ ] Cobertura total ≥ 63%
- [ ] 0 errores de compilación
- [ ] 0 tests fallando

**FASE 2 (Messaging):** ✅
- [ ] 15 tests creados y pasando
- [ ] Cobertura messaging ≥ 80%
- [ ] Cobertura total ≥ 67%
- [ ] Integración RabbitMQ testeada

**FASE 3 (Persistence):** ✅
- [ ] 20 tests creados y pasando
- [ ] Cobertura persistence ≥ 80%
- [ ] Cobertura total ≥ 90%
- [ ] Cache decorator testeado

**FASE 4 (Mapper):** ✅
- [ ] 10 tests creados y pasando
- [ ] Cobertura mapper ≥ 90%
- [ ] Cobertura total ≥ 93%
- [ ] Conversiones bidireccionales OK

**FASE 5 (Config):** ✅
- [ ] 8 tests creados y pasando
- [ ] Cobertura config ≥ 85%
- [ ] **Cobertura total ≥ 80% (META ALCANZADA)**

---

## 🚀 EJECUCIÓN Y VERIFICACIÓN

### Comandos

**Ejecutar tests:**
```bash
mvn clean test
```

**Generar reporte JaCoCo:**
```bash
mvn jacoco:report
```

**Ver reporte HTML:**
```bash
open target/site/jacoco/index.html
```

**Ejecutar tests de una fase específica:**
```bash
# FASE 1
mvn test -Dtest="*Validation*Test"

# FASE 2
mvn test -Dtest="*Messaging*Test,*RabbitMQ*Test"

# FASE 3
mvn test -Dtest="*Persistence*Test,*Repository*Test"

# FASE 4
mvn test -Dtest="*Mapper*Test"

# FASE 5
mvn test -Dtest="*Config*Test,*Factory*Test"
```

---

## 📈 TRACKING DE PROGRESO

### Dashboard de Cobertura

```bash
# Crear script para tracking
cat > track-coverage.sh << 'EOF'
#!/bin/bash
echo "=== JaCoCo Coverage Report ==="
mvn clean test jacoco:report -q
echo ""
echo "Cobertura por Package:"
grep -A 50 "coveragetable" target/site/jacoco/index.html | \
  grep -oP 'com\.example\.usuarioservice\.\w+|<td class="ctr2" id="c\d+">\d+ %' | \
  paste -d " " - - | \
  awk '{print $1 ": " $2}'
EOF
chmod +x track-coverage.sh
```

**Ejecutar tracking:**
```bash
./track-coverage.sh
```

---

## 🎯 MÉTRICAS DE ÉXITO

### KPIs del Proyecto

| Métrica | Actual | Meta | Estado |
|---------|--------|------|--------|
| Cobertura de Instrucciones | 49% | 80% | ❌ |
| Cobertura de Branches | 25% | 70% | ❌ |
| Métodos cubiertos | 93/167 (56%) | 147/167 (88%) | ❌ |
| Clases cubiertas | 25/30 (83%) | 30/30 (100%) | ⚠️ |
| Tests totales | 146 | ~224 | 65% |

### Definición de "Done"

✅ **Proyecto completado cuando:**
1. Cobertura total ≥ 80%
2. Cobertura de branches ≥ 70%
3. Todos los packages críticos ≥ 80%
4. 0 tests fallando
5. Pipeline CI/CD verde

---

## 📚 REFERENCIAS

### Documentos Relacionados
- `INFORME_COMPLETO_REFACTORIZACION.md` - Trabajo previo completado
- `REFACTORING_PLAN_COVERAGE.md` - Plan maestro original
- JaCoCo Report: `target/site/jacoco/index.html`

### Best Practices
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

## 🤝 ROLES Y RESPONSABILIDADES

### Equipo Sugerido

| Rol | Responsabilidad | Fases |
|-----|-----------------|-------|
| **QA Lead** | Coordinar plan, revisar tests | Todas |
| **Dev 1** | FASE 1 + FASE 2 | Validation + Messaging |
| **Dev 2** | FASE 3 | Persistence |
| **Dev 3** | FASE 4 + FASE 5 | Mapper + Config |
| **Tech Lead** | Code review, arquitectura | Consulta |

### Estimación por Persona

- **1 persona:** 3-4 semanas (22-29 horas)
- **2 personas:** 1.5-2 semanas (11-15 horas cada uno)
- **3 personas:** 1 semana (7-10 horas cada uno)

---

## 🔄 PROCESO DE REVISIÓN

### Checklist por Pull Request

```markdown
## Test Coverage PR Checklist

- [ ] Tests compilan sin errores
- [ ] Todos los tests pasan (mvn test verde)
- [ ] Cobertura incrementada según objetivo de la fase
- [ ] Reporte JaCoCo actualizado
- [ ] Nombres de tests descriptivos (Given/When/Then)
- [ ] Edge cases cubiertos
- [ ] Mocks utilizados correctamente
- [ ] Sin código duplicado en tests
- [ ] Javadoc en tests complejos
- [ ] Sin @Disabled o @Ignore sin justificación

## Fase Completada
- [ ] FASE X: [Nombre]
- [ ] Cobertura objetivo alcanzada: X%
- [ ] Tests adicionales: X tests
```

---

## 📞 CONTACTO Y SOPORTE

### Para Consultas Técnicas

- **JaCoCo Issues:** Verificar configuración en pom.xml
- **Mockito Problems:** Revisar anotaciones @Mock, @InjectMocks
- **Spring Test Issues:** Verificar @SpringBootTest, @WebMvcTest, etc.

### Escalación

- **Cobertura no incrementa:** Revisar qué código ejecutan los tests
- **Tests fallan intermitentemente:** Problemas de concurrencia o dependencias externas
- **Performance lento:** Reducir uso de @SpringBootTest, preferir unit tests

---

## 📝 NOTAS FINALES

### Consideraciones Importantes

1. **No buscar 100% a toda costa**
   - Algunos constructores autogenerados (Lombok) no necesitan tests
   - Código de configuración simple puede tener baja cobertura aceptable
   - Focus en lógica de negocio y paths críticos

2. **Mantener balance**
   - Tests deben ser mantenibles
   - Evitar over-mocking (tests frágiles)
   - Preferir integration tests donde tenga sentido

3. **Continuous Improvement**
   - Este plan es iterativo
   - Ajustar prioridades según hallazgos
   - Celebrar incrementos progresivos

---

**Documento creado:** 26 de febrero de 2026  
**Basado en:** JaCoCo Report (49% coverage)  
**Meta:** 80%+ coverage  
**Estimación:** 3 semanas / 22-29 horas  
**Tests a crear:** ~78 tests  
**Estado:** ✅ PLAN APROBADO - Listo para ejecución

