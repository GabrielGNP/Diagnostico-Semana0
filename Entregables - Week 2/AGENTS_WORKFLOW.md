# Agentes y flujo de trabajo TDD / Requirements / Tests

Resumen rápido

Este documento describe los agentes disponibles en `.github/agents/`, su propósito, reglas importantes extraídas de cada definición y el flujo recomendado que seguiremos en este proyecto para transformar una idea en requisitos, planes de prueba y código mediante una cadena de agentes: Prompt Engineering (CRAFT) → IRISH (Requisitos) → Test Engineering (TEST_PLAN) → TDD Engineer (RED→GREEN→REFACTOR).

Checklist (lo que voy a cubrir)
- [x] Describir cada agente (rol, entradas, salidas y reglas clave).
- [x] Explicar el flujo propuesto paso a paso y quién hace qué.
- [x] Señalar detalles relevantes/advertencias que encontré en los archivos de agente.
- [x] Incluir recomendaciones operativas y mensajes/commit sugeridos en cada etapa.

---

Agentes disponibles (archivos fuente)
- `.github/agents/prompt-enginnering.agent.md` — "Prompt Engineering CRAFT"
- `.github/agents/irish-requirements-liftup.agent.md` — "IRISH"
- `.github/agents/test-engineering.agent.md` — "Test Engineering"
- `.github/agents/TDD-engineer.agent.md` — "TDD Engineer (TEED)"

---

1) Agente: Prompt Engineering (CRAFT)

Propósito
- Tomar un prompt base y refinarlo con la metodología CRAFT (Context, Role, Action, Format, Target Audience) para producir un prompt optimizado y listo para usar.

Entradas esperadas
- Un prompt base (puede ser vago o estructurado).

Salidas
- Un prompt final, estructurado en secciones C,R,A,F,T.

Reglas y comportamiento importante
- Fase 1 (Diagnóstico): NO generar el prompt final hasta recopilar información faltante; debe identificar si faltan Context/Role/Action/Format/Target Audience y formular preguntas numéricas, claras y puntuales.
- Fase 2 (Construcción): sólo después de recibir respuestas, genera el prompt final formateado.
- No asumir información crítica: si falta, debe preguntar.
- Entregable: prompt listo para copiar/pegar.

Detalles útiles encontrados
- No usa herramientas externas según header.
- Está pensado para mejorar prompts antes de enviarlos al resto de agentes; su salida debe ser precisa y no conversacional.

Consejos operativos
- Proporciona contexto mínimo (qué quieres lograr, para qué modelo, restricciones de formato) para obtener un prompt CRAFT de alta calidad.

---

2) Agente: IRISH — Requirements Lift-up

Propósito
- Tomar un prompt CRAFT (o una necesidad vaga) y convertirlo en Epics y User Stories completas, con FR/NFR, aceptación (GIVEN/WHEN/THEN), dependencias, suposiciones y preguntas abiertas.

Entradas esperadas
- Prompt en formato CRAFT o un requerimiento de negocio (puede ser vago pero el agente pedirá clarificaciones).

Salidas
- Documento de requisitos estructurado (**DRAFT**), y tras confirmación, el artefacto final en Markdown listo para guardarse como `HU-XY.md` en `USER_STORIES`.

Reglas y comportamiento importante (crucial)
- Siempre pedir aclaraciones si faltan datos.
- NO generar la versión final de la historia hasta que el usuario confirme el borrador.
- No crear archivos HU-XY.md hasta haber resuelto todas las preguntas y haber recibido confirmación explícita.
- Responder en inglés por diseño del agente (nota importante: el agente defina en inglés; si trabajas en español indícalo explicítamente al invocarlo).

Detalles útiles extraídos
- Estructura de salida muy estricta: EPIC → USER STORIES → FR/NFR → Acceptance Criteria (GIVEN/WHEN/THEN) → Process Flow → Open Questions.
- Insiste en no inventar reglas de negocio; siempre declarar supuestos.

Consejos operativos
- Usa Prompt Engineering antes de IRISH para entregarle un CRAFT completo y minimizar iteraciones.
- Responde puntualmente a las preguntas de clarificación para no bloquear la generación del archivo final.

---

3) Agente: Test Engineering (Generador de TEST_PLAN.md)

Propósito
- Tomar una User Story técnica y producir un `TEST_PLAN.md` formal con escenarios Gherkin, aplicando técnicas de diseño de pruebas (Equivalence Partitioning, Boundary Value Analysis, Decision Tables) y estratificando por niveles de prueba (unit/integration/system).

Entradas esperadas
- User Story final (HU-XY.md) con Acceptance Criteria completo.

Salidas
- `TEST_PLAN.md` con:
  - Análisis de contexto y riesgos
  - Principios de pruebas aplicados
  - Test levels strategy
  - Diseño por técnica (EP, BVA, Decision Table)
  - Escenarios Gherkin agrupados
  - Estrategia TDD (qué test implementar primero, mocking, riesgos)

Reglas y comportamiento importante
- Obligatorio aplicar las técnicas: Equivalence Partitioning, BVA y Decision Table cuando corresponda.
- Todas las pruebas deben estar expresadas en Gherkin (Given/When/Then).
- La trazabilidad entre cada escenario y el Acceptance Criterion debe ser explícita.

Detalles interesantes extraídos
- Produce un plan que indica qué tests deben escribirse primero en RED, con detalles de mocking e aislamiento.
- Fuerza rigor teórico: no genera escenarios “genéricos”.

Consejos operativos
- Entregarle a este agente las historias finalizadas por IRISH para evitar revisiones largas.
- Usa este TEST_PLAN como contrato entre product/qa/dev para la fase TDD.

---

4) Agente: TDD Engineer (TEED)

Propósito
- Ejecutar el ciclo TDD estricto (RED → GREEN → REFACTOR) tomando la User Story final y el `TEST_PLAN.md`.

Entradas esperadas
- `HU-XY.md` finalizado (User Story)
- `TEST_PLAN.md` generado por Test Engineering

Salidas
- Tests (RED): archivos de pruebas que deben fallar inicialmente.
- Implementación mínima (GREEN) para pasar tests.
- Refactor (REFACTOR): mejoras de estructura que no cambien comportamiento.
- Mensajes y recordatorios de commit entre fases.

Reglas y disciplina (muy estrictas)
- No saltar fases. Red, luego Green, luego Refactor.
- No generar código de producción antes de tests que fallen.
- Requiere confirmaciones y commits entre fases. Exige mensajes de commit específicos.
- Evalúa aplicación de SOLID en REFACTOR (con checkpoints obligatorios).
- No fusionar fases ni optimizar prematuramente.

Detalles interesantes extraídos
- El agente exige prueba de commit: la historia del Git debe mostrar (test commit → impl commit → refactor commit).
- Durante GREEN pide la mínima implementación y ofrece comandos para ejecutar tests localmente.
- En REFACTOR hay un "SOLID CHECKPOINT" obligatorio antes de finalizar.

Consejos operativos
- Sigue cuidadosamente las instrucciones de commit y mensajes sugeridos para mantener trazabilidad y cumplir con la disciplina TDD.
- Mantén las pruebas y commits pequeños y frecuentes.

---

Flujo recomendado (alto nivel)

1. Prompt Engineering (CRAFT)
   - Pulir y estructurar el prompt/requirements inicial en CRAFT.
   - Output: prompt CRAFT (Context/Role/Action/Format/Target Audience).

2. IRISH (Requirements)
   - Entrada: prompt CRAFT.
   - IRISH realiza la clarificación (si aplica); produce Draft; pide confirmación.
   - Tras confirmación, IRISH genera `HU-XY.md` final en `USER_STORIES`.

3. Test Engineering
   - Entrada: `HU-XY.md` final.
   - Genera `TEST_PLAN.md` con escenarios Gherkin y diseño de pruebas (EP/BVA/DecisionTable).

4. TDD Engineer (TEED)
   - Entrada: `HU-XY.md` final + `TEST_PLAN.md`.
   - Ejecuta RED: genera tests que deben fallar.
   - Confirma commit RED.
   - Ejecuta GREEN: implementa mínimo para pasar tests.
   - Confirma commit GREEN.
   - Ejecuta REFACTOR: mejora diseño, aplica SOLID checkpoint.
   - Confirma commit REFACTOR.

Diagrama (texto)

CRAFT → IRISH → HU-XY.md → TEST_PLAN.md → TDD (RED → GREEN → REFACTOR)

---

Detalles y advertencias importantes (resumen)
- IRISH no crea archivos finales hasta que se hayan respondido todas las preguntas; no automatizar la creación de `HU-XY.md` si hay preguntas abiertas.
- Test Engineering exige Gherkin y técnicas de diseño; no aceptar planes superficiales.
- TEED obliga disciplina de commits y no permite omitir fases; esto puede requerir disciplina manual (commits/reviews) cuando se automatice.
- Prompt Engineering debe usarse para reducir iteraciones con IRISH: mientras más completo es el prompt CRAFT, menos clarificaciones pedirá IRISH.

---

Recomendaciones operativas finales
- Siempre empezar por Prompt Engineering (CRAFT) para maximizar eficiencia.
- Mantener interacciones cortas y confirmaciones claras entre agentes (ej. "IRISH DRAFT OK").
- Conservar mensajes de commit propuestos en los archivos generados para facilitar revisión del historial.
- Documentar en cada HU en `USER_STORIES/` el `TEST_PLAN.md` vinculado y el registro de commits TDD (RED / GREEN / REFACTOR) para trazabilidad.

---

Archivo generado: `.github/agents/AGENTS_WORKFLOW.md` — contiene la descripción completa del flujo y de los agentes.
