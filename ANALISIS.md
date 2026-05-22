# Análisis Completo de CTTest_v2

> Generado: 2026-05-22

---

## CRÍTICO

### C1. Las respuestas correctas se exponen al estudiante
**`PublicController.java:64-82`** — `GET /api/public/student-tests/{id}/test-data` devuelve el árbol completo de `Test` → `Question` → `Answer` **incluyendo** el campo `correct`. Un estudiante puede inspeccionar la respuesta de red y ver qué respuestas son correctas *durante el examen*.

**Fix:** Usar un DTO/proyección que excluya `Answer.correct` en endpoints públicos.

---

### C2. Sin control de roles en endpoints admin
**`SecurityConfig.java:38-44`** — Solo `/api/admin/**` está restringido por rol, pero **ningún controlador usa ese prefijo**. Todos los CRUD (`UserController`, `TestController`, etc.) están en `/api/users`, `/api/tests`, etc. y solo requieren `USER` o `ADMIN` autenticado. Un usuario normal puede crear/editar/borrar tests, usuarios, grupos, centros.

**Fix:** Añadir `@PreAuthorize("hasRole('ADMIN')")` en operaciones de escritura, o reestructurar rutas bajo `/api/admin/**`.

---

### C3. Duplicados en StudentAnswer por falta de unique constraint
**`PublicController.java:110-130`** — `saveAnswer` no verifica si ya existe una respuesta para el mismo `(studentTestId, questionId)`. Crea filas duplicadas. `findAnswersMapByStudentTestId` lanza `IllegalStateException: Duplicate key` por `Collectors.toMap`.

**Fix:** Añadir unique constraint `(student_test_id, question_id)` en tabla `student_answer` y usar `merge`/`INSERT ... ON DUPLICATE KEY UPDATE`.

---

### C4. FORCE_LAZY_LOADING deshabilita lazy loading
**`JacksonConfig.java:13`** — `Hibernate6Module` con `FORCE_LAZY_LOADING=true` serializa **todas** las asociaciones lazy, causando N+1 queries en cada serialización y exponiendo datos no intencionados.

**Fix:** Eliminar `FORCE_LAZY_LOADING`, añadir `@EntityGraph`/`JOIN FETCH` donde sea necesario, y usar DTOs en lugar de entidades.

---

### C5. Condiciones de carrera en flujo de examen
**`PublicController.java:111-169`** — `saveAnswer` y `finishTest` no tienen `@Transactional` ni locking. Solicitudes concurrentes pueden:
- Transicionar `ASSIGNED → IN_PROGRESS` múltiples veces
- Corromper el estado del `StudentTest`
- Calcular score mientras se guarda otra respuesta

**Fix:** Añadir `@Transactional` en servicio, usar `@Version` para optimistic locking, o `SELECT ... FOR UPDATE`.

---

### C6. JWT en localStorage → vulnerable a XSS
**`frontend/src/auth/AuthContext.tsx:23`** — El token JWT se guarda en `localStorage`. Cualquier XSS en la app lo roba permanentemente.

**Fix:** Usar `httpOnly` cookie. O al menos almacenar en memoria (closure) y perder el token al recargar.

---

### C7. Secret JWT por defecto hardcodeado
**`application.yml:31`** — `app.jwt.secret: ${JWT_SECRET:4a6f536e...}` con fallback a una clave fija hexadecimal. Cualquiera con acceso al source puede forjar JWTs.

**Fix:** Eliminar el valor default, hacer que falle si no se configura `JWT_SECRET`.

---

## ALTO

### H1. Endpoints públicos sin autenticación
**`PublicController.java:45-169`** — Los endpoints `/api/public/student-tests/{id}/test-data`, `saveAnswer`, `finishTest` no requieren autenticación. El único control de acceso es un `studentTestId` secuencial (auto-increment). Enumerar IDs es trivial.

**Fix:** Usar un UUID como identificador de sesión, o requerir PIN + studentId en cada request.

---

### H2. Entidades expuestas directamente (sin DTOs)
**Todos los controladores** devuelven entidades JPA directamente. Esto fuerza el uso de `@JsonIgnore`, `@JsonIgnoreProperties`, `@Transient` como parches. Ej: `User` expone `addDate`, `addUser`, `modDate`, `modUser`.

**Fix:** Crear DTOs/records para cada recurso. Usar MapStruct o manual mapping.

---

### H3. CRUD duplicado en 6 controladores y 5 servicios
Cada controller sigue el patrón `findAll/search/findById/save/update/deleteById`. Cada servicio es un pass-through anémico que solo llama al repositorio.

**Fix:** Crear `BaseController<T, ID>` y `BaseService<T, ID>` genéricos, o usar Spring Data REST.

---

### H4. Sin rate limiting en login
**`AuthController.java:27`** — `/api/auth/login` no tiene rate limiting, bloqueo de cuenta, ni CAPTCHA. Ataques de fuerza bruta directos.

**Fix:** Añadir Spring Boot + bucket4j o similar. O bloqueo tras N intentos fallidos.

---

### H5. Sin loading states en frontend
**Todas las páginas CRUD** — `Centers.tsx`, `Tests.tsx`, `Students.tsx`, etc. No muestran indicadores de carga durante llamadas API. El usuario ve tablas vacías/obsoletas hasta que llegan los datos.

**Fix:** Añadir estado `loading` y spinners/skeletons.

---

### H6. Sin Error Boundary en frontend
No hay un componente ErrorBoundary ni manejo global de errores. Errores no capturados muestran pantalla en blanco.

**Fix:** Crear `ErrorBoundary.tsx` y envolver la app.

---

### H7. Side-effects sin cleanup en frontend
**Todas las páginas** — `useEffect` que llama `api.get()` no tiene cleanup ni `AbortController`. Navegación rápida causa `setState` en componente desmontado.

**Fix:** Añadir `AbortController` y cleanup en cada `useEffect`.

---

### H8. `@Transient` anula `@OneToMany` en StudentTest
**`StudentTest.java:59-62`** — `studentAnswers` tiene `@OneToMany(mappedBy = "studentTestId")` Y `@Transient`. `@Transient` gana, el campo nunca se puebla. Todo el mapping es código muerto.

**Fix:** Eliminar `@Transient` o eliminar el campo por completo.

---

### H9. Sin unique constraint en `test_instance.pin`
**`TestInstance.java:46`** — `pin` no tiene `@Column(unique = true)`. `generatePin()` hace check + insert no atómico → colisión posible.

**Fix:** Añadir `unique = true` y unique index en DB.

---

### H10. Plain Integer FKs en vez de `@ManyToOne`
**`Test.java:27`, `TestInstance.java:27`, `StudentTest.java:37`, `StudentAnswer.java:28-33`** — Usan ints sueltos como FK. `StudentAnswer` no tiene **ninguna** relación JPA.

**Fix:** Convertir a `@ManyToOne` con `@JoinColumn`.

---

### H11. 404 vs 500
**`GlobalExceptionHandler.java:17-20`** — `RuntimeException` con `ex.getMessage()` se devuelve como 500. Casos de "not found" deberían ser 404.

**Fix:** Crear `ResourceNotFoundException` → 404. No devolver mensajes de excepción al cliente.

---

## MEDIO

### M1. Score calculation duplicado en controller
**`PublicController.java:91-100, 143-152`** — El cálculo de aciertos está inline, duplicado. Debe ir en `StudentTestService`.

### M2. PasswordEncoder inyectado en controller
**`UserController.java:26,45-47`** — El controller conoce `PasswordEncoder`. Debe ir en `UserService`.

### M3. `IN_PROGESS` typo
**`StudentTestStatus.java:11`** — `IN_PROGESS` (falta la R). También en `frontend/src/types/index.ts:84`.

### M4. `Random` no thread-safe
**`TestInstanceServiceImpl.java:21`** — `Random random = new Random()` es campo de instancia. Usar `ThreadLocalRandom.current()`.

### M5. Métodos/imports no utilizados
- `AnswerRepository` nunca se inyecta
- `QuestionRepository.findByTestIdOrderByOrderAsc` nunca se llama desde controller
- `StudentTestRepository.findWithStudentsByTestInstanceId` nunca se llama
- `StudentAnswerRepository.findByQuestionId` nunca se llama
- Múltiples imports `Page` no utilizados en controllers

### M6. POST crea con 200 en vez de 201
**Todos los controladores** — `ResponseEntity.ok()` en creates. Debe ser `ResponseEntity.created()`.

### M7. Sin `@Valid` en `@RequestBody`
**Varios controllers** — `QuestionController.create`, `UserController.create`, `TestController.create` no validan el body.

### M8. Sin paginación en StudentTestController
**`StudentTestController.java:26`** — Devuelve `List<StudentTest>` sin `Pageable`.

### M9. Navegación entre páginas sin lazy loading
**`App.tsx:6-14`** — Todos los componentes importados estáticamente. Usar `React.lazy()` para code splitting.

### M10. Patrón modal duplicado 7+ veces
**Todas las páginas CRUD** — El mismo modal header/body/footer/close/save repetido inline. Extraer a `<Modal>` componente.

### M11. `any` type en frontend
**Múltiples páginas** — `params: any`, `e.target.value as any`, `useState<any>`. Derrota TypeScript strict mode.

### M12. Hardcoded `centerId: 1` en Tests create
**`Tests.tsx:42`** — Al crear test, el centro está hardcodeado a ID 1.

### M13. Sin tests de integración
**0 tests** con `@DataJpaTest`, `@WebMvcTest`, o Testcontainers. Tests existentes usan `@SpringBootTest` con `@MockBean` y no tienen `application-test.yml` — intentan conectar a MySQL real.

### M14. `ResponseEntity<?>` oculta tipos
**Varios controllers** — Usan `ResponseEntity<?>` en lugar del tipo concreto, perdiendo type safety en la API.

---

## BAJO

### L1. Sin refresh token
**`application.yml:32`** — JWT expira en 24h sin mecanismo de refresh. Si roban el token, vale 24h sin revocación.

### L2. CORS demasiado permisivo
**`SecurityConfig.java:54,57`** — `allowedOriginPatterns("*")` con `allowCredentials(true)`.

### L3. Enumeración de usuarios en login
**`CustomUserDetailsService.java:20`** — `"User not found: " + username` permite probar qué usuarios existen.

### L4. Sin validación server-side de tipo MIME
**`Image.java:39-47`** — Solo se valida cliente-side (`accept="image/jpeg,image/png"`). Servidor acepta cualquier tipo.

### L5. `User.isAccountNonLocked()` siempre true
**`CustomUserDetails.java:34-51`** — Sin bloqueo por intentos fallidos.

### L6. Cloneable violado en entities
**`Question.java:116-130`, `Answer.java:56-63`, `Image.java:63-68`** — `clone()` manual sin `super.clone()`.

### L7. `CascadeType.ALL` en `@ManyToOne` hacia Image
**`Question.java:43`, `Answer.java:35`** — Borrar una Question/Answer cascades a borrar la imagen. Si varias preguntas comparten imagen (por clone), se corrompe.

### L8. Sin chequeo de expiración en validatePin
**`PublicController.java:47-61`** — `validatePin` no verifica si el `TestInstance` está `CLOSED`.

### L9. Sin límite de tiempo en examen
**`PublicController.java`** — Un estudiante puede estar `IN_PROGRESS` indefinidamente.

### L10. Sin route 404 en frontend
**`App.tsx:17-40`** — Cualquier ruta desconocida muestra pantalla en blanco.

---

## Resumen por categoría

| Categoría | Issues |
|-----------|--------|
| **Seguridad** | C1, C2, C6, C7, H1, H4, L1, L2, L3, L4, L5 |
| **Backend (JPA/ORM)** | C4, C5, H8, H9, H10, M3, M4, L6, L7 |
| **Backend (Arquitectura)** | H2, H3, H11, M1, M2, M5, M6, M7, M8, M14 |
| **Frontend** | H5, H6, H7, M9, M10, M11, M12, L10 |
| **Testing** | M13 |
| **API REST** | M6, M8, M14 |

---

## Quick wins (1-2 horas)

1. Fix `IN_PROGESS` → `IN_PROGRESS` (backend + frontend)
2. `Random` → `ThreadLocalRandom.current()`
3. `ResponseEntity.ok()` → `ResponseEntity.created()` en creates
4. Eliminar código muerto: `@Transient studentAnswers`, `AnswerRepository`, imports no usados, repos no usados
5. Añadir `@Valid` a `@RequestBody` en controllers
6. Añadir unique constraint en `test_instance.pin`
7. Añadir `ResourceNotFoundException` → 404
8. Eliminar `FORCE_LAZY_LOADING` y empezar a usar DTOs/`@EntityGraph`
9. Extraer score calculation de `PublicController` a `StudentTestService`
10. Añadir ErrorBoundary en frontend

---

¿Quieres que empiece a implementar alguna de estas mejoras?
