# Estrategia de Implementación — CTTest_v2

> Prompt para ejecutar todas las mejoras del análisis (ANALISIS.md)

---

Eres un ingeniero de software senior. Implementa todas las mejoras del análisis del proyecto CTTest_v2 (`ANALISIS.md`) siguiendo **estrictamente** esta estrategia. Lee ANALISIS.md completo antes de empezar.

## Reglas generales

1. **No cambies la API pública ni el frontend** a menos que el fix lo requiera explícitamente. Preserva compatibilidad.
2. **Commits atómicos** — un commit por fase (o sub-fase si es grande), con mensaje claro.
3. **Ejecuta `mvn compile -DskipTests`** después de cada fase backend y **`npm run build`** después de cada fase frontend para verificar que no rompes nada.
4. **No hagas refactors grandes** sin primero crear los tests que cubran el código existente.
5. **Si un cambio requiere migración de DB**, no la ejecutes — solo marca los cambios en entities y documenta el ALTER TABLE necesario.
6. **Usa Git** — crea rama `feature/analysis-fixes` y commitea por fase.

---

## Fase 0: Preparación (setup + rama)

```bash
git checkout -b feature/analysis-fixes
```

---

## Fase 1: Quick Wins (1-2h cada uno, bajo riesgo)

### 1.1 Fix typo `IN_PROGESS` → `IN_PROGRESS`
- **Backend**: `StudentTestStatus.java` — cambiar enum constante
- **Frontend**: `src/types/index.ts` — actualizar string literal
- **Buscar** cualquier referencia a `IN_PROGESS` en todo el código (grep)

### 1.2 `Random` → `ThreadLocalRandom`
- **`TestInstanceServiceImpl.java`** — cambiar `Random random = new Random()` a `ThreadLocalRandom.current()` dentro del método `generatePin()`, eliminar el field.

### 1.3 `ResponseEntity.ok()` → `ResponseEntity.created()` en creates
- **Todos los controllers**: `CenterController`, `TestController`, `UserController`, `StudentController`, `StudentGroupController`, `QuestionController`, `TestInstanceController`, `StudentTestController`
- Los creates deben devolver `ResponseEntity.created(URI.create("/api/xxx/" + saved.getId())).body(saved)`
- `AuthController.login` y `PublicController` se saltan (login no es create, public tiene su propia lógica)

### 1.4 Eliminar código muerto
- **`StudentTest.java:59-62`**: Eliminar el campo `studentAnswers` (tiene `@OneToMany` + `@Transient`)
- **`AnswerRepository.java`**: Eliminar el archivo completo (nunca se inyecta)
- **Eliminar imports no usados** en todos los archivos
- **`QuestionRepository.findByTestIdOrderByOrderAsc`**: Si no se usa desde ningún controller, eliminar
- **`StudentTestRepository.findWithStudentsByTestInstanceId`**: Si no se usa, eliminar
- **`StudentAnswerRepository.findByQuestionId`**: Si no se usa, eliminar
- **`UserService.existsByUsername`**: Si no se usa, eliminar
- **`StudentTestService.deleteById`**: Si no se usa, eliminar

### 1.5 Añadir `@Valid` a `@RequestBody`
- **`QuestionController.create(@RequestBody @Valid Question question)`**
- **`UserController.create(@RequestBody @Valid User user)`**
- **`TestController.create(@RequestBody @Valid Test test)`**
- Y en todos los `update` que falten

### 1.6 Añadir unique constraint en `test_instance.pin`
- **`TestInstance.java`**: `@Column(unique = true)` en campo `pin`
- Documentar: `ALTER TABLE test_instance ADD UNIQUE INDEX idx_pin (pin);`

### 1.7 Añadir `ResourceNotFoundException` → 404
- Crear `src/main/java/com/cttest/exception/ResourceNotFoundException.java` (extiende `RuntimeException`)
- En `GlobalExceptionHandler`: añadir `@ExceptionHandler(ResourceNotFoundException.class) → 404`
- En todos los `*ServiceImpl.findById()`: cambiar `throw new RuntimeException("... not found")` por `throw new ResourceNotFoundException("...")`
- En `GlobalExceptionHandler.handleRuntimeException`: cambiar a mensaje genérico en la respuesta, loggear el detalle

### 1.8 Eliminar `FORCE_LAZY_LOADING`
- **`JacksonConfig.java`**: Eliminar `module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, true)`
- Buscar todas las serializaciones de entidades y asegurar que funcionan (pueden necesitar `@Transactional` en controllers o `@EntityGraph` en repos)
- **Riesgo**: Puede romper serialización existente. Probar bien.

### 1.9 Extraer score calculation de `PublicController`
- **`StudentTestService`**: Añadir método `calculateScore(Integer studentTestId)` que devuelva `Map.of("totalQuestions", ..., "correctAnswers", ...)`
- **`PublicController.java:91-100` y `143-152`**: Reemplazar lógica duplicada con llamada al servicio

### 1.10 Añadir ErrorBoundary en frontend
- Crear `frontend/src/components/ErrorBoundary.tsx`
- Envolver en `App.tsx`

---

## Fase 2: Seguridad (crítico + alto)

### 2.1 DTOs para endpoints públicos (fix C1 + H1)
- Crear `src/main/java/com/cttest/model/dto/TestDataDTO.java` (record) con: `testTitle`, `questions` (lista de `QuestionDataDTO`), `totalQuestions`
- Crear `QuestionDataDTO` con: `id`, `text`, `orderNum`, `imageData` (sin answers → las answers las damos en otro endpoint o embebidas sin `correct`)
- Crear `AnswerDataDTO` con: `id`, `text`, `imageData` (sin `correct`)
- **`PublicController.getTestData()`**: Devolver `TestDataDTO` en lugar de la entidad
- Crear `StudentTestResultDTO` para el resultado: `totalQuestions`, `correctAnswers`, `score`, `answers` (sin correct flag)

### 2.2 Roles y autorización (fix C2)
- Añadir `@PreAuthorize("hasRole('ADMIN')")` en operaciones de escritura de:
  - `UserController.create/update/delete`
  - `TestController.create/update/delete`
  - `QuestionController.create/update/delete`
  - `CenterController.create/update/delete`
  - `StudentGroupController.create/update/delete`
  - `TestInstanceController.create/update/delete`
- Las operaciones de lectura (`findAll`, `findById`) deben ser accesibles para `USER` y `ADMIN`
- Asegurar que `SecurityConfig` tiene `@EnableMethodSecurity` habilitado

### 2.3 Unique constraint en StudentAnswer (fix C3)
- **`StudentAnswer.java`**: Añadir `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_test_id", "question_id"}))`
- **`PublicController.saveAnswer()`**: Cambiar a `INSERT ... ON DUPLICATE KEY UPDATE` o usar merge: buscar si existe `StudentAnswer` para ese `(studentTestId, questionId)`. Si existe, actualizar `answerId`; si no, crear.
- **`StudentAnswerService`**: Añadir método `saveOrUpdate(studentTestId, questionId, answerId)`
- Documentar: `ALTER TABLE student_answer ADD UNIQUE INDEX idx_student_test_question (student_test_id, question_id);`

### 2.4 Transacciones + locking en flujo de examen (fix C5)
- **`PublicController.java`**: Marcar métodos `saveAnswer` y `finishTest` como `@Transactional`
- **`StudentTest.java`**: Añadir campo `@Version private Long version;` para optimistic locking
- **`StudentTestService`**: Añadir `findByIdWithLock(Integer id)` que use `@Lock(PESSIMISTIC_WRITE)` o `LockModeType.OPTIMISTIC`
- En `finishTest`: Verificar que el test no esté ya `FINISHED` antes de calcular score

### 2.5 JWT: secret obligatorio + refresh token (fix C7 + L1)
- **`application.yml`**: Eliminar valor default de `app.jwt.secret`, dejar `${JWT_SECRET}` sin fallback → hard-fail si no se configura
- **Añadir refresh token**: Crear endpoint `POST /api/auth/refresh` que acepte refresh token y devuelva nuevo access token + refresh token
- **`AuthController.login`**: Devolver también `refreshToken`
- **`JwtTokenProvider`**: Añadir métodos `generateRefreshToken()`, `validateRefreshToken()`
- **Frontend `AuthContext`**: Manejar refresh automático cuando access token expire (interceptor de axios)

### 2.6 JWT: localStorage → httpOnly cookie (fix C6)
- **Frontend**: Eliminar `localStorage.setItem('token', ...)` y `localStorage.getItem('token')`
- **Backend**: Configurar `SecurityConfig` para que el login devuelva la JWT en `Set-Cookie` header (`ResponseCookie` con `httpOnly=true`, `secure=true`, `sameSite=Strict`)
- **Frontend `axios.ts`**: No enviar `Authorization` header manualmente (la cookie se envía automática)
- **`AuthContext`**: Cambiar lazy initializer: en lugar de leer de localStorage, hacer `GET /api/auth/me` para verificar sesión activa

---

## Fase 3: Refactor Backend

### 3.1 Mover lógica de negocio fuera de controllers
- **`UserController`**: Mover `passwordEncoder.encode()` a `UserService.create()`
- **`TestInstanceController`**: Mover PIN generation a `TestInstanceService.create()`
- **`QuestionController`**: Mover validación "all answers must have image" a `QuestionService.create()`

### 3.2 BaseService + BaseController genéricos
- Crear `BaseService<T, ID>` con métodos: `findAll(Pageable)`, `search(query, Pageable)`, `findById(ID)`, `save(T)`, `update(ID, T)`, `deleteById(ID)`
- Crear `BaseController<T, ID, S extends BaseService>` con CRUD estándar
- Refactorizar controllers existentes para extender `BaseController`
- **NO refactorizar** `PublicController`, `AuthController`, `QuestionController` (tienen lógica especial)

### 3.3 DTOs para todas las respuestas
- Crear records para cada entidad: `UserDTO`, `TestDTO`, `CenterDTO`, `StudentDTO`, `StudentGroupDTO`, `TestInstanceDTO`, `QuestionDTO`, `AnswerDTO`, `ImageDTO`
- Crear mapper class o usar MapStruct
- Reemplazar entidades en respuestas de controllers por DTOs

---

## Fase 4: Frontend

### 4.1 Loading states + Error handling
- Añadir estado `loading` en cada página CRUD
- Mostrar spinner durante carga
- Añadir manejo de error en cada llamada API con toast/alert
- Añadir `React.Suspense` para lazy loading de rutas

### 4.2 Eliminar `any` types
- Reemplazar `params: any` con tipos específicos en cada página
- Reemplazar `useState<any>` con tipos concretos
- Reemplazar `e.target.value as any` con tipos de eventos correctos

### 4.3 Cleanup de effects
- Añadir `AbortController` en cada `useEffect` con llamada API
- Cleanup: `() => controller.abort()`

### 4.4 Extraer Modal component
- Crear `frontend/src/components/Modal.tsx` con props: `show`, `onClose`, `title`, `children`, `footer`
- Refactorizar las 7+ páginas para usar el Modal compartido

### 4.5 Corregir hardcoded centerId
- **`Tests.tsx`**: Añadir selector de centro en el modal de creación

---

## Fase 5: Testing

### 5.1 Configuración de tests
- Crear `src/test/resources/application-test.yml` con H2 o Testcontainers MySQL
- Crear test base class con configuración compartida

### 5.2 Tests de seguridad
- Test de JWT filter (token válido, inválido, expirado)
- Test de autorización por roles
- Test de endpoints públicos sin token

### 5.3 Tests de flujo de examen
- Test de `saveAnswer` (creación, actualización, duplicado)
- Test de `finishTest` con score calculation
- Test de concurrencia

### 5.4 Tests de frontend (básico)
- Configurar Vitest + React Testing Library
- Test de `LoginPage` (render, submit, error)
- Test de `AuthContext`

---

## Fase 6: Bugs restantes

### 6.1 Validación correct-answer
- **`QuestionController.create`**: Validar que al menos una respuesta esté marcada como correcta

### 6.2 Chequeo de expiración en validatePin
- **`PublicController.validatePin`**: Verificar `TestInstance.status != CLOSED`

### 6.3 Route 404 en frontend
- **`App.tsx`**: Añadir `<Route path="*" element={<NotFound />} />`

---

## Commits sugeridos

```
Fase 1.1: Fix typo IN_PROGESS → IN_PROGRESS
Fase 1.2: Thread-safe Random → ThreadLocalRandom
Fase 1.3: ResponseEntity.created() en creates
Fase 1.4: Remove dead code and unused imports
Fase 1.5: Add @Valid to @RequestBody params
Fase 1.6: Add unique constraint on test_instance.pin
Fase 1.7: ResourceNotFoundException → 404
Fase 1.8: Remove FORCE_LAZY_LOADING
Fase 1.9: Extract score calculation to StudentTestService
Fase 1.10: Add ErrorBoundary to frontend
Fase 2.1: DTOs for public endpoints (fix correct-answer leak)
Fase 2.2: Role-based authorization with @PreAuthorize
Fase 2.3: Unique constraint + upsert in StudentAnswer
Fase 2.4: @Transactional + optimistic locking in test flow
Fase 2.5: JWT secret required + refresh token
Fase 2.6: JWT in httpOnly cookie instead of localStorage
Fase 3: Backend refactoring (business logic, base service, DTOs)
Fase 4: Frontend improvements (loading, types, modal, cleanup)
Fase 5: Testing (security, test flow, frontend)
Fase 6: Remaining bug fixes
```

---

Ejecuta cada fase en orden. Después de cada fase, corre `mvn compile -DskipTests` (backend) y `npm run build` (frontend) para verificar. Si una fase rompe algo, detente y corrige antes de continuar.
