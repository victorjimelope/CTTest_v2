# Análisis de permisos, visibilidad, API y tests de CTTest_v2

> Fecha de auditoría: 2026-10-26  
> Alcance: código fuente, configuración, pantallas React, aplicación legacy JSF y tests.  
> Método: revisión estática + ejecución local de la suite + comprobaciones puntuales sobre H2 con datos sintéticos.  
> Restricción respetada: solo lectura del repositorio; el único archivo creado es este informe.

---

## 1. Resumen ejecutivo

La aplicación tiene dos capas de acceso muy distintas:

- **Backend moderno (Spring Boot):** autenticación JWT correcta y escrituras protegidas con `@PreAuthorize("hasRole('ADMIN')")`, pero **las lecturas no tienen autorización por rol, propietario ni centro**. Cualquier usuario autenticado puede listar y consultar usuarios, centros, alumnos, grupos, tests, instancias y resultados de cualquier centro.
- **Flujo público de examen:** los endpoints públicos identifican la sesión del alumno con un **`studentTestId` numérico y secuencial**, sin token de sesión, sin PIN en cada petición y sin comprobar que el alumno sea el propietario. Esto permite leer exámenes, respuestas y resultados ajenos, y modificar o finalizar exámenes de otros.
- **Aplicación legacy (JSF):** el filtro de autenticación solo comprueba que exista sesión. No hay comprobaciones de rol ni de centro en servidor. Los elementos de administración se ocultan en el menú, pero las páginas y acciones siguen siendo accesibles por URL directa o invocación de beans.
- **Frontend React:** `PrivateRoute` solo comprueba autenticación; no existe un guard de administrador. `/users` y `/centers` se ocultan del menú, pero sus rutas y botones siguen disponibles para cualquier usuario autenticado.
- **Tests:** existen tests de seguridad y flujo, pero la suite por defecto **no es ejecutable de forma fiable** (20 errores por la tabla H2 `user`). Con el workaround de H2 se ejecutan 33 tests y fallan 6, todos relacionados con el flujo público y la validación de instancias cerradas. No hay tests negativos de autorización por rol, centro ni IDOR.

**Recuento de hallazgos:** 2 críticos, 8 altos, 7 medios y 5 bajos (**22 en total**).

**Riesgo principal:** un atacante no autenticado puede acceder a datos de exámenes y resultados ajenos mediante IDs secuenciales; un usuario autenticado no administrador puede leer toda la información de la organización, aunque no pueda ejecutar escrituras.

---

## 2. Arquitectura y mecanismos de autorización identificados

### 2.1 Backend moderno

| Elemento | Implementación | Consecuencia para permisos |
|---|---|---|
| Framework | Spring Boot 3.3.5, Spring Security, JPA/Hibernate | Seguridad declarativa y method security disponibles. |
| Autenticación | `POST /api/auth/login`, JWT access + refresh | El token se acepta por `Authorization: Bearer` y por cookie `jwt`. |
| Autoridades | `CustomUserDetails` asigna `ROLE_ADMIN` o `ROLE_USER` | Solo existen dos roles globales; no hay rol docente ni alumno autenticado. |
| Centro | `CustomUserDetails.centerId` y `User.center` | El centro se usa en la UI para la pestaña “Mis tests”, pero **no se aplica como frontera de seguridad**. |
| Reglas HTTP | `SecurityConfig.securityFilterChain` | Públicos: `/api/auth/**`, `/api/public/**`; `/api/admin/**` exige ADMIN; el resto de `/api/**` solo exige autenticación. |
| Reglas por método | `@PreAuthorize("hasRole('ADMIN')")` en `BaseController` y controladores específicos | Protegen escrituras, no lecturas ni acceso a recursos concretos. |
| JWT | `JwtTokenProvider` genera tokens con `type=access`/`refresh`; el filtro valida firma y expiración | El filtro no comprueba el tipo de token ni el `username` nulo de un refresh. |
| Errores | `GlobalExceptionHandler` captura `RuntimeException` y devuelve 500 genérico | Puede convertir denegaciones de autorización en 500 y no registra la causa. |

Referencias: `src/main/java/com/cttest/config/SecurityConfig.java:33-58`, `src/main/java/com/cttest/security/CustomUserDetails.java`, `src/main/java/com/cttest/security/JwtAuthenticationFilter.java:30-44`, `src/main/java/com/cttest/controller/BaseController.java:34-60`, `src/main/java/com/cttest/exception/GlobalExceptionHandler.java:22-26`.

### 2.2 Frontend React

- `AuthContext` mantiene el usuario en memoria y llama a `/auth/me` al arrancar. No usa `localStorage` para el token.
- `PrivateRoute` solo valida `isAuthenticated`.
- `Layout` filtra `/centers` y `/users` del menú con `adminOnly`, pero no protege las rutas.
- Las páginas no consultan `isAdmin` para ocultar botones ni formularios; las llamadas de escritura fallan en backend, pero las de lectura no.
- El cliente envía `centerId` como parámetro de filtro; el servidor lo acepta sin comprobar que coincida con el centro del usuario.

Referencias: `frontend/src/auth/AuthContext.tsx`, `frontend/src/components/PrivateRoute.tsx:4-8`, `frontend/src/components/Layout.tsx:15-31`, `frontend/src/App.tsx:33-56`.

### 2.3 Aplicación legacy JSF

- `AuthFilter` se aplica a `*.xhtml` y solo comprueba que exista `LOGGED_USER` en sesión.
- No hay comprobación de rol ni de centro en el filtro.
- El sidebar oculta centros y usuarios a los no administradores, pero las páginas siguen publicadas.
- `BaseManagedBean.checkLoggedUserAuthorization` existe, pero no se invoca en ningún bean; la única llamada está comentada en `TestFormBean`.
- Los beans de búsqueda y los servicios BL tampoco aplican autorización.

Referencias: `legacy/src/main/java/cttest/AuthFilter.java:51-72`, `legacy/src/main/java/cttest/BaseManagedBean.java:77-82`, `legacy/src/main/java/cttest/TestFormBean.java:95-103`, `legacy/src/main/webapp/template/app_sidebar_lg.xhtml:35-47`.

### 2.4 Modelo de roles real

| Rol | Cómo se obtiene | Qué puede hacer realmente |
|---|---|---|
| Anónimo | Sin JWT | Login, flujo público con PIN, `/actuator/health`. |
| `ROLE_USER` | `User.admin=false` | Leer todos los recursos autenticados; escrituras denegadas (aunque devuelven 500). |
| `ROLE_ADMIN` | `User.admin=true` | Leer y escribir todos los recursos, sin límite de centro ni de propiedad. |
| Alumno legacy | Sesión pública | Ejecutar el test si conoce el PIN y el identificador; el enlace usa un ID cifrado con clave estática. |

No existe un rol “docente” separado ni un rol “alumno” autenticado. Cualquier decisión de negocio que asuma “profesor de un centro” o “administrador de centro” **no está implementada**.

---

## 3. Inventario de pantallas y rutas

### 3.1 Frontend React

| Pantalla / ruta | Acceso esperado | Control en frontend | Control real en backend | Observación |
|---|---|---|---|---|
| `/` (`HomePage`) | Público | Ninguno | `/api/public/validate-pin` público | Pide PIN y “student ID”; ver P-M07. |
| `/login` | Público | Ninguno | `/api/auth/login` público | Correcto. |
| `/take-test` (`PinEntry`) | Público | Ninguno | Reutiliza `HomePage` | Correcto como entrada pública. |
| `/take-test/:id` | Alumno con PIN válido | Ninguno; ID en URL | `/api/public/student-tests/{id}/test-data` público | **IDOR crítico** (P-C01). |
| `/take-test/:id/result` | Alumno de ese test | Ninguno; ID en URL | `/api/public/student-tests/{id}/result` público | **IDOR crítico** (P-C01). |
| `/dashboard` | Autenticado | `PrivateRoute` | Lee `/centers`, `/tests`, `/students`, `/users` | Un USER ve datos de todos los centros. |
| `/tests` | Autenticado | `PrivateRoute` | Lee todo; escrituras ADMIN | Botones de crear/editar/borrar visibles para USER. |
| `/tests/:id` | Autenticado / propietario | `PrivateRoute` | `/tests/{id}/with-questions` devuelve `correct` | USER puede ver respuestas correctas y botones de edición. |
| `/test-instances` | Autenticado | `PrivateRoute` | Lee todo; escrituras ADMIN | Botones de crear/cerrar/borrar visibles para USER. |
| `/test-instances/:id` | Autenticado / propietario | `PrivateRoute` | Lee instancia, `student-tests` y `with-questions` | USER ve resultados y respuestas correctas. |
| `/students` | Autenticado / centro | `PrivateRoute` | Lee todo; escrituras ADMIN | Sin filtro por centro. |
| `/student-groups` | Autenticado / centro | `PrivateRoute` | Lee todo; escrituras ADMIN | Sin filtro por centro. |
| `/centers` | ADMIN | Ruta sin guard; solo menú oculto | GET cualquier autenticado; escrituras ADMIN | **Ruta accesible por URL a USER** (P-H03). |
| `/users` | ADMIN | Ruta sin guard; solo menú oculto | GET cualquier autenticado; escrituras ADMIN | **Ruta accesible por URL a USER** (P-H03). |
| `*` (`NotFound`) | Público | Ninguno | No aplica | Correcto. |

### 3.2 Legacy JSF

| Pantalla / recurso | Acceso esperado | Control en servidor | Observación |
|---|---|---|---|
| `app/home/home.xhtml` | Público | Excluido del filtro | Correcto. |
| `app/login/login.xhtml` | Público | Excluido del filtro | Correcto. |
| `app/studentTest/studentTest.xhtml` | Público con PIN | Excluido por `contains` | Enlace con ID cifrado y clave estática (P-C02). |
| `app/welcome/welcomeWithTemplate.xhtml` | Autenticado | Solo sesión | Correcto en autenticación, no en rol. |
| `app/search/test/testSearchWithTemplate.xhtml` | Autenticado / docente | Solo sesión | Cualquier usuario puede ver todos los tests. |
| `app/view/test/testViewWithTemplate.xhtml` | Autenticado / propietario | Solo sesión | Sin comprobación de centro ni propiedad. |
| `app/view/testInstance/testInstanceViewWithTemplate.xhtml` | Autenticado / docente | Solo sesión | Cualquier usuario puede abrir resultados por `id`. |
| `app/search/student/studentSearchWithTemplate.xhtml` | Autenticado / centro | Solo sesión | Sin filtro de centro. |
| `app/search/studentGroup/studentGroupSearchWithTemplate.xhtml` | Autenticado / centro | Solo sesión | Sin filtro de centro. |
| `app/search/center/centerSearchWithTemplate.xhtml` | ADMIN | Solo sesión; menú oculto | **Accesible por URL directa a USER** (P-H06). |
| `app/search/user/userSearchWithTemplate.xhtml` | ADMIN | Solo sesión; menú oculto | **Accesible por URL directa a USER** (P-H06). |

Conclusión de esta sección: **ocultar en el menú no es proteger**. En React y en JSF, la visibilidad de la navegación y la autorización efectiva son cosas distintas; solo las escrituras modernas tienen una barrera real de rol.

---

## 4. Matriz de permisos y visibilidad

| Recurso / operación | Anónimo | USER | ADMIN | Comprobación en servidor |
|---|---|---|---|---|
| Login / refresh / logout | Permitido | Permitido | Permitido | Público. |
| `/api/public/**` | Permitido | Permitido | Permitido | Ninguna; IDOR en `student-tests/{id}`. |
| Lectura de centros | 403 | Permitido | Permitido | Solo autenticación. |
| Lectura de usuarios | 403 | **Permitido** | Permitido | Solo autenticación; expone `admin`, centro y grupos. |
| Lectura de alumnos / grupos | 403 | Permitido | Permitido | Solo autenticación; sin centro. |
| Lectura de tests / preguntas | 403 | Permitido | Permitido | Solo autenticación; incluye respuestas correctas. |
| Lectura de instancias / resultados | 403 | Permitido | Permitido | Solo autenticación; sin centro. |
| Escritura de centros/usuarios/alumnos/grupos/tests/instancias | 403 | Denegada | Permitido | `@PreAuthorize` ADMIN; el fallo de USER se convierte en 500 (P-H08). |
| Páginas `/users`, `/centers` | Login | Visible por URL | Visible | Solo `PrivateRoute`; no hay guard de rol. |
| Menú de administración | No visible | Oculto | Visible | Solo cosmético. |
| Legacy centros/usuarios | Login | Visible por URL | Visible | Solo sesión. |
| Legacy CRUD y resultados | Login | Permitido | Permitido | Ninguna comprobación de rol/centro. |

---

## 5. Inventario de métodos API y restricciones de acceso

> “Auth real” describe lo que impone el servidor, no lo que la interfaz sugiere.

| Método | Endpoint | Auth real | Regla de autorización | Datos / operación | Riesgo |
|---|---|---|---|---|---|
| POST | `/api/auth/login` | Público | Ninguna | Credenciales → access + refresh | Fuerza bruta (P-M02). |
| POST | `/api/auth/refresh` | Público | Refresh válido | Nuevo access/refresh | Rotación sin revocación (P-M01). |
| POST | `/api/auth/logout` | Público | Ninguna | Borra cookie | No revoca tokens (P-M01). |
| GET | `/api/auth/me` | Público | Ninguna | Devuelve `authenticated:false` si no hay sesión | Correcto. |
| POST | `/api/public/validate-pin` | Público | Ninguna | PIN + studentId → studentTestId | IDOR, PIN cerrado, fuerza bruta (P-C01, P-H04, P-M02). |
| GET | `/api/public/student-tests/{id}/test-data` | Público | Ninguna | Test, preguntas, respuestas y nombre del alumno | **IDOR crítico** (P-C01). |
| GET | `/api/public/student-tests/{id}/result` | Público | Ninguna | Resultado y entidad `StudentTest` | **IDOR crítico** (P-C01). |
| POST | `/api/public/student-tests/{id}/answer` | Público | Ninguna | Guarda respuesta | **IDOR de escritura**; sin validar pregunta/respuesta (P-C01, P-H05). |
| POST | `/api/public/student-tests/{id}/finish` | Público | Ninguna | Calcula y guarda nota | **IDOR de escritura**; falla con 500 (P-C01, P-M06). |
| GET | `/api/centers` | Autenticado | Ninguna | Lista paginada de centros | USER ve todos (P-H02). |
| GET | `/api/centers/{id}` | Autenticado | Ninguna | Centro por ID | USER ve cualquiera. |
| POST/PUT/DELETE | `/api/centers` y `/api/centers/{id}` | Autenticado | ADMIN en método | CRUD de centros | USER recibe 500, no 403 (P-H08). |
| GET | `/api/users` | Autenticado | Ninguna | Usuarios, `admin`, centro y grupos | **USER ve administración** (P-H03). |
| GET | `/api/users/{id}` | Autenticado | Ninguna | Usuario por ID | Igual. |
| POST/PUT/DELETE | `/api/users` y `/api/users/{id}` | Autenticado | ADMIN en método | CRUD de usuarios | USER recibe 500. |
| GET | `/api/students` | Autenticado | Ninguna | Alumnos, incluido `identifier` | Sin centro (P-H02). |
| GET | `/api/students/{id}` | Autenticado | Ninguna | Alumno por ID | Sin centro. |
| POST/PUT/DELETE | `/api/students` y `/api/students/{id}` | Autenticado | ADMIN en método | CRUD de alumnos | USER recibe 500. |
| GET | `/api/student-groups` | Autenticado | Ninguna | Grupos, opcional `centerId` | El cliente elige el centro (P-H02). |
| GET | `/api/student-groups/{id}` | Autenticado | Ninguna | Grupo por ID | Sin centro. |
| POST/PUT/DELETE | `/api/student-groups` y `/api/student-groups/{id}` | Autenticado | ADMIN en método | CRUD de grupos | USER recibe 500. |
| GET | `/api/tests` | Autenticado | Ninguna | Tests, opcional `centerId`/estado | Sin centro; el cliente elige filtro. |
| GET | `/api/tests/{id}` | Autenticado | Ninguna | Test por ID | Sin centro. |
| GET | `/api/tests/{id}/with-questions` | Autenticado | Ninguna | Test + preguntas + `correct` | **Respuestas correctas a USER** (P-H01). |
| POST/PUT/DELETE | `/api/tests` y `/api/tests/{id}` | Autenticado | ADMIN en método | CRUD de tests | USER recibe 500. |
| GET | `/api/questions/{id}` | Autenticado | Ninguna | Pregunta completa, incluye `correct` | **Respuestas correctas a USER** (P-H01). |
| POST | `/api/tests/{testId}/questions` | Autenticado | ADMIN en método | Crea pregunta | Sin comprobar centro/propiedad. |
| PUT/DELETE | `/api/questions/{id}`, `/api/questions/{id}/move` | Autenticado | ADMIN en método | Edita/borra/mueve pregunta | Sin comprobar centro/propiedad. |
| GET | `/api/test-instances` | Autenticado | Ninguna | Instancias, opcional `testId` | USER ve todas (P-H02). |
| GET | `/api/test-instances/{id}` | Autenticado | Ninguna | Instancia por ID | USER ve cualquiera. |
| POST/PUT/DELETE | `/api/test-instances` y `/api/test-instances/{id}` | Autenticado | ADMIN en método | CRUD/cierre de instancias | USER recibe 500. |
| GET | `/api/student-tests` | Autenticado | Ninguna | Todos los `StudentTest` o los de una instancia | USER ve resultados/PII (P-H02, P-M04). |
| GET | `/api/student-tests/{id}` | Autenticado | Ninguna | `StudentTest` completo | USER ve cualquiera. |
| POST/PUT | `/api/student-tests` y `/api/student-tests/{id}` | Autenticado | ADMIN en método | Crea/actualiza `StudentTest` | USER recibe 500. |
| GET | `/actuator/health` | Público | Ninguna | Estado de salud | Expuesto por `anyRequest().permitAll()` (P-L01). |

---

## 6. Matriz de trazabilidad entre pantallas, API y tests

| Funcionalidad | Pantalla / ruta | API | Permiso esperado | Protección implementada | Tests existentes | Carencia |
|---|---|---|---|---|---|---|
| Login de personal | `/login` | `POST /api/auth/login` | Público | Autenticación | `SecurityTest.login_*` | Sin logout/refresh/me negativos. |
| Tomar test público | `/take-test` | `POST /api/public/validate-pin` | PIN + alumno asignado + instancia abierta | Solo PIN/alumno; no estado ni rate limit | `TestFlowTest.validatePin_*` | Falla instancia cerrada; sin rate limit. |
| Cargar examen | `/take-test/:id` | `GET .../test-data` | Token de sesión del alumno | ID secuencial público | `getTestData_*` | IDOR; no se prueba acceso ajeno. |
| Responder | `/take-test/:id` | `POST .../answer` | Token de sesión + estado | ID público + `studentTest.closed` | `saveAnswer_*` | IDOR; no valida pregunta/respuesta; falla con 500. |
| Finalizar | `/take-test/:id` | `POST .../finish` | Token de sesión + estado | ID público + `studentTest.closed` | `finishTest_*` | IDOR; 500; no comprueba instancia. |
| Ver resultado | `/take-test/:id/result` | `GET .../result` | Token de sesión del alumno | ID público | `getResult_*` | IDOR; 500 en el flujo de test. |
| CRUD centros | `/centers` | `/api/centers` | ADMIN | Escritura ADMIN; lectura autenticada | `CenterControllerTest` | Ruta/lectura para USER; sin test negativo. |
| CRUD usuarios | `/users` | `/api/users` | ADMIN | Escritura ADMIN; lectura autenticada | Ninguno | USER lee todos; sin test negativo. |
| CRUD alumnos/grupos | `/students`, `/student-groups` | `/api/students`, `/api/student-groups` | ADMIN / centro | Escritura ADMIN; lectura autenticada | Ninguno | Sin centro; sin tests. |
| CRUD tests | `/tests`, `/tests/:id` | `/api/tests`, `/api/questions` | ADMIN / propietario | Escritura ADMIN; lectura autenticada | `SecurityTest.createQuestion_*` | USER lee respuestas correctas; sin ownership. |
| Ver resultados de instancia | `/test-instances/:id` | `/api/test-instances`, `/api/student-tests`, `/tests/{id}/with-questions` | Docente del centro / ADMIN | Lectura autenticada | Ninguno | USER ve todo; sin tests. |
| Legacy CRUD | `*SearchWithTemplate.xhtml` | Beans BL | Rol/centro | Solo sesión | Ninguno | Cualquier usuario autenticado. |
| Legacy examen | `studentTest.xhtml` | `HomeBean` / `StudentTestFormBean` | PIN + alumno | ID cifrado con clave estática | Ninguno | ID forjable; sin sesión. |

---

## 7. Inventario de tests existentes y evaluación de cobertura

### 7.1 Tests encontrados

| Archivo | Tests | Cobertura real | Problemas |
|---|---:|---|---|
| `src/test/java/com/cttest/CTTestApplicationTests.java` | 1 | Contexto Spring | No prueba permisos. |
| `src/test/java/com/cttest/controller/CenterControllerTest.java` | 3 | GET con `@WithMockUser`, POST con ADMIN | No prueba USER en POST/PUT/DELETE, ni lectura de `/users`, ni centro. |
| `src/test/java/com/cttest/service/impl/CenterServiceImplTest.java` | 5 | Servicio de centros | Sin autorización. |
| `src/test/java/com/cttest/service/impl/TestInstanceServiceImplTest.java` | 4 | PIN y generación | No prueba instancia cerrada ni autorización. |
| `src/test/java/com/cttest/SecurityTest.java` | 8 | Público, 403 sin auth, login, token inválido, pregunta sin respuesta correcta | Solo crea ADMIN; no prueba USER, refresh, logout ni rutas de administración. |
| `src/test/java/com/cttest/TestFlowTest.java` | 12 | Flujo público completo | No prueba IDOR, ni acceso a otro alumno, ni instancia cerrada con resultado esperado. |

`BaseTest`, `SecurityTest` y `TestFlowTest` están **sin trackear en git** (`git status` los muestra como `??`). La suite no forma parte del código versionado actual.

### 7.2 Comandos y resultados

Comando por defecto:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn -q test
```

Resultado: **33 tests, 0 fallos, 20 errores**. Los 20 errores son de `SecurityTest` (8) y `TestFlowTest` (12) y tienen la misma causa: H2 interpreta `user` como palabra reservada al ejecutar consultas generadas por Hibernate:

```text
Syntax error in SQL statement "... from [*]user u1_0"; expected "identifier"
```

Workaround sin modificar ficheros:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn -q test \
  -Dspring.datasource.url='jdbc:h2:mem:testdb;NON_KEYWORDS=USER'
```

Resultado: **33 tests, 6 fallos, 0 errores**.

| Test | Resultado | Evidencia |
|---|---|---|
| `CenterControllerTest` | 3/3 OK | — |
| `CTTestApplicationTests` | 1/1 OK | — |
| `SecurityTest` | 8/8 OK | Con `NON_KEYWORDS=USER`. |
| `CenterServiceImplTest` | 5/5 OK | — |
| `TestInstanceServiceImplTest` | 4/4 OK | — |
| `TestFlowTest` | 6/12 OK | 6 fallos. |

Fallos confirmados de `TestFlowTest`:

1. `validatePin_WhenClosedInstance_ShouldReturn400`: esperaba 400, recibió **200**.
2. `finishTest_WithAllCorrect_ShouldReturnFullScore`: esperaba 200, recibió **500**.
3. `finishTest_WithPartialCorrect_ShouldReturnPartialScore`: esperaba 200, recibió **500**.
4. `finishTest_WhenAlreadyFinished_ShouldReturn400`: esperaba 200 en el primer finish, recibió **500**.
5. `saveAnswer_WhenTestFinished_ShouldReturn400`: esperaba 200 en el finish previo, recibió **500**.
6. `getResult_ShouldReturnScore`: esperaba 200 en el finish previo, recibió **500**.

El `GlobalExceptionHandler` devuelve un 500 genérico y **no registra la excepción**, por lo que la causa raíz de los 500 no queda demostrada por la salida de los tests. La hipótesis más probable es un problema de JPA al reemplazar la colección `Test.questions` con `orphanRemoval` dentro de la transacción de `finish` (`TestServiceImpl.findWithQuestionsById`), pero debe confirmarse con trazas antes de tratarlo como hecho. Ver P-M03 y P-M06.

### 7.3 Carencias de cobertura

No existen tests para:

- Usuario `ROLE_USER` leyendo `/api/users`, `/api/centers`, `/api/students`, `/api/student-groups`, `/api/tests`, `/api/test-instances` o `/api/student-tests`.
- Usuario `ROLE_USER` intentando POST/PUT/DELETE y verificando **403** (hoy se observa 500).
- Aislamiento entre centros: usuario del centro A no debe ver datos del centro B.
- IDOR en `/api/public/student-tests/{id}/*`: el dueño del test A no debe acceder al test B.
- Acceso sin token o con token de otro alumno al flujo público.
- Instancia cerrada: `validate-pin`, `test-data`, `answer` y `finish`.
- Validación de que `questionId` pertenece al test y `answerId` pertenece a la pregunta.
- Exposición de `correct` en `/api/tests/{id}/with-questions` y `/api/questions/{id}` a un USER.
- Rutas React `/users` y `/centers` con usuario no administrador.
- Legacy: acceso directo a `centerSearchWithTemplate.xhtml` y `userSearchWithTemplate.xhtml` con sesión no admin, y ejecución de acciones CRUD.
- Legacy: forja de ID cifrado con la clave AES estática.
- `logout` no invalida tokens; `refresh` no rota/revoca.
- Rate limiting de login y de PIN.
- Concurrencia en `answer`/`finish` (transiciones de estado y doble envío).

---

## 8. Hallazgos clasificados por gravedad

> Convención: **confirmado** = verificado en código y, cuando se indica, en ejecución local; **hipótesis** = requiere validación adicional. Los identificadores `P-*` son propios de este informe y no sustituyen a los `C*`/`H*` de `ANALISIS.md`.

### 8.1 Críticos

#### P-C01 — IDOR público: cualquier persona puede leer y modificar exámenes ajenos por `studentTestId`

- **Gravedad:** Crítica.
- **Tipo:** Confirmado (código + ejecución local).
- **Descripción:** Los endpoints públicos reciben un `studentTestId` numérico y secuencial. No hay token de sesión, cookie, PIN ni comprobación de propiedad. `validate-pin` devuelve el ID, pero después cualquier petición puede usar otro ID sin volver a validar PIN ni alumno.
- **Ubicación:** `src/main/java/com/cttest/controller/PublicController.java:44-144`; `src/main/java/com/cttest/repository/StudentTestRepository.java:17`; `frontend/src/pages/HomePage.tsx:18-22`; `frontend/src/pages/StudentTestFlow.tsx:130-180`.
- **Pantallas / API / permisos / tests afectados:** `/take-test/:id`, `/take-test/:id/result`, `POST /api/public/validate-pin`, `GET .../test-data`, `POST .../answer`, `POST .../finish`, `GET .../result`; `TestFlowTest` (solo prueba el camino feliz).
- **Comportamiento actual:** un `GET /api/public/student-tests/1/test-data` sin autenticación devuelve el test, las preguntas, las respuestas y el nombre del alumno. Un `POST .../answer` o `.../finish` con otro ID modifica el estado de otro alumno. Los IDs son autoincrementales.
- **Comportamiento esperado:** el alumno debe recibir un token opaco, corto y de un solo uso tras `validate-pin`; todas las operaciones deben validar ese token, el PIN/alumno y el estado de la instancia. Nunca se debe confiar en un ID de recurso enviado por el cliente.
- **Evidencia:** lectura de código y comprobación local: `GET /api/public/student-tests/1/test-data` sin cabecera devolvió **200** con `studentName` y preguntas; `GET /api/public/student-tests/1/result` sin autenticación devolvió **200**.
- **Impacto:** lectura de exámenes y datos personales, suplantación de alumnos, modificación de respuestas, finalización fraudulenta y cálculo de notas ajenas. Es el riesgo más grave del sistema.
- **Recomendación:** sustituir el ID por un token firmado/HMAC o por un identificador aleatorio (UUID) ligado a `studentTestId`, alumno, PIN e instancia; expiración corta; una sola sesión activa; validar en cada endpoint; no devolver el ID interno. Añadir rate limiting y auditoría.
- **Test de verificación:** `GET .../test-data` sin token → 401/403; token del alumno A contra `studentTestId` de B → 403; token caducado → 401; token de una instancia cerrada → 409/400.

#### P-C02 — Legacy: ID de examen cifrado con clave AES estática y sin sesión vinculada

- **Gravedad:** Crítica.
- **Tipo:** Confirmado por código; no se ejecutó la aplicación legacy.
- **Descripción:** El enlace público legacy cifra el `studentTestId` con AES/ECB y una clave literal en el código (`ABCDEFGHIJKLMNOP`). Cualquiera que conozca el código puede cifrar cualquier ID. `StudentTestFormBean` descifra el ID y carga el test sin comprobar PIN, alumno, sesión ni propiedad.
- **Ubicación:** `legacy/src/main/java/util/AESUtil.java:17-20`; `legacy/src/main/java/cttest/HomeBean.java:59-68`; `legacy/src/main/java/cttest/StudentTestFormBean.java:70-106`; `legacy/src/main/java/cttest/AuthFilter.java:51-53`.
- **Pantallas / API / permisos / tests afectados:** `app/studentTest/studentTest.xhtml`, `HomeBean.navigateToTest`, `StudentTestFormBean.init`; flujo legacy completo; no hay tests.
- **Comportamiento actual:** el ID cifrado es la única “credencial”. La clave es fija, ECB no aporta aleatoriedad y el filtro permite la página pública con `contains`. Un usuario puede generar el enlace de otro alumno.
- **Comportamiento esperado:** token aleatorio de sesión vinculado al PIN/alumno, con expiración y validación en servidor; clave gestionada fuera del código; no usar cifrado determinista como control de acceso.
- **Evidencia:** clave y transformación visibles en `AESUtil`; `HomeBean` cifra el ID; `StudentTestFormBean` lo descifra y carga directamente; `AuthFilter` no valida rol ni sesión de alumno.
- **Impacto:** acceso y modificación de exámenes ajenos en legacy; el mismo patrón de IDOR que P-C01, agravado por una clave filtrada.
- **Recomendación:** retirar el flujo legacy o migrarlo al token moderno; rotar/eliminar la clave; usar `SecureRandom` y un identificador opaco; añadir comprobación de PIN/alumno/instancia en cada acción.
- **Test de verificación:** intentar abrir `studentTest.xhtml?id=<ID cifrado de otro alumno>` → 403; ID manipulado → 400; token caducado → 401.

### 8.2 Altos

#### P-H01 — Las respuestas correctas se exponen a cualquier usuario autenticado

- **Gravedad:** Alta (crítica si los alumnos tienen cuenta `ROLE_USER`).
- **Tipo:** Confirmado (código + ejecución local).
- **Descripción:** `AnswerDTO` incluye `correct` y `EntityDTOMapper` lo rellena. `GET /api/tests/{id}/with-questions` y `GET /api/questions/{id}` no tienen restricción de rol ni de propiedad. Un USER puede obtener las respuestas correctas de cualquier test.
- **Ubicación:** `src/main/java/com/cttest/model/dto/AnswerDTO.java:3-8`; `src/main/java/com/cttest/model/dto/EntityDTOMapper.java:39-51` y `:100-112`; `src/main/java/com/cttest/controller/TestController.java:39-42`; `src/main/java/com/cttest/controller/QuestionController.java:33-36`.
- **Pantallas / API / permisos / tests afectados:** `/tests/:id`, `/test-instances/:id`; `GET /api/tests/{id}/with-questions`, `GET /api/questions/{id}`; `SecurityTest` no lo cubre.
- **Comportamiento actual:** un USER con token obtiene `"correct":true/false` en la respuesta. Comprobado localmente con un test sintético: `GET /api/tests/1/with-questions` como USER devolvió **200** con `correct`.
- **Comportamiento esperado:** solo ADMIN (o docente propietario, si se define ese rol) puede ver respuestas correctas; el alumno recibe un DTO sin `correct`; el endpoint público ya usa `TestDataMapper`, que excluye el campo.
- **Evidencia:** DTO y mapper; ejecución local con USER.
- **Impacto:** filtración de soluciones y ruptura de la integridad del examen si un alumno tiene cuenta; también permite a un usuario no autorizado copiar tests.
- **Recomendación:** separar DTO de administración y DTO público; no incluir `correct` en el DTO de lectura general; añadir `@PreAuthorize` o filtro de propiedad para `with-questions`; test negativo con USER.
- **Test de verificación:** USER `GET /api/tests/{id}/with-questions` → 403 o respuesta sin `correct`; ADMIN → 200 con `correct`.

#### P-H02 — Sin aislamiento por centro ni por propietario en las lecturas

- **Gravedad:** Alta.
- **Tipo:** Confirmado por código; parcialmente comprobado en ejecución.
- **Descripción:** Todos los listados usan `findAll` o filtros opcionales enviados por el cliente. No se obtiene el centro del principal ni se comprueba que el recurso pertenezca a él. `centerId` es un parámetro de consulta que el cliente puede cambiar.
- **Ubicación:** `src/main/java/com/cttest/controller/BaseController.java:27-32`; `src/main/java/com/cttest/controller/UserController.java:26-35`; `src/main/java/com/cttest/controller/TestController.java:24-37`; `StudentController.java`, `StudentGroupController.java`, `TestInstanceController.java`, `StudentTestController.java`; `src/main/java/com/cttest/service/impl/*`.
- **Pantallas / API / permisos / tests afectados:** dashboard, tests, instancias, alumnos, grupos, centros y usuarios; todos los GET de `/api/**`; no hay tests de aislamiento.
- **Comportamiento actual:** un USER puede listar y consultar recursos de cualquier centro; `?centerId=` solo filtra si el cliente lo envía. En la comprobación local, un USER del centro A obtuvo `/api/users` y `/api/centers` completos.
- **Comportamiento esperado:** el servidor debe derivar el centro del usuario y aplicarlo siempre; un ADMIN global puede ver todo, un administrador de centro solo el suyo. Si se usa `centerId` como filtro, debe validarse contra el principal.
- **Evidencia:** código y ejecución local; `UserController` devuelve todos los usuarios con `admin` y `centerId`.
- **Impacto:** fuga de datos personales y académicos entre centros; enumeración de usuarios y resultados.
- **Recomendación:** introducir un `AuthorizationService`/`TenantContext`; aplicar scoping en repositorios y servicios, no en el controlador; tests con dos centros.
- **Test de verificación:** usuario centro A `GET /api/students` → solo A; `GET /api/students?centerId=B` → 403; ADMIN → todo.

#### P-H03 — Datos de usuarios/centros y rutas de administración accesibles a `ROLE_USER`

- **Gravedad:** Alta.
- **Tipo:** Confirmado (código + ejecución local).
- **Descripción:** `/api/users` y `/api/centers` son legibles por cualquier autenticado. En React, `/users` y `/centers` están bajo `PrivateRoute`, no bajo un guard de admin; el menú solo las oculta. Las páginas tampoco ocultan botones.
- **Ubicación:** `src/main/java/com/cttest/controller/UserController.java:26-35`; `src/main/java/com/cttest/config/SecurityConfig.java:42-43`; `frontend/src/App.tsx:46-54`; `frontend/src/components/PrivateRoute.tsx:4-8`; `frontend/src/components/Layout.tsx:20-31`.
- **Pantallas / API / permisos / tests afectados:** `/users`, `/centers`, `/dashboard`; `GET /api/users`, `GET /api/centers`, `GET /api/users/{id}`, `GET /api/centers/{id}`; no hay tests.
- **Comportamiento actual:** un USER puede abrir `/users` y `/centers` escribiendo la URL, y la API le devuelve usuarios con `admin`, centro y grupos. Comprobado localmente: `GET /api/users` como USER → **200** con el usuario admin.
- **Comportamiento esperado:** rutas y endpoints de administración solo para ADMIN; ocultar no es suficiente; el backend debe devolver 403. Si existe un administrador de centro, limitar a su centro.
- **Evidencia:** ejecución local y código.
- **Impacto:** enumeración de cuentas, conocimiento de administradores y estructura organizativa; facilita ataques de fuerza bruta y phishing.
- **Recomendación:** `AdminRoute` en React + `@PreAuthorize` en lecturas de usuarios/centros; separar DTO público del DTO administrativo; tests.
- **Test de verificación:** USER `GET /api/users` → 403; USER navega a `/users` → redirect/403; ADMIN → 200.

#### P-H04 — `validate-pin` acepta instancias cerradas

- **Gravedad:** Alta.
- **Tipo:** Confirmado (código + ejecución local + test fallido).
- **Descripción:** `validatePin` busca el PIN y el alumno, pero no comprueba `TestInstance.isClosed()` ni el estado de la instancia. `getIdByPin` tampoco filtra por estado.
- **Ubicación:** `src/main/java/com/cttest/controller/PublicController.java:44-59`; `src/main/java/com/cttest/service/impl/TestInstanceServiceImpl.java:64-68`; `src/main/java/com/cttest/repository/TestInstanceRepository.java` (`getIdByPin`).
- **Pantallas / API / permisos / tests afectados:** `/take-test`, `POST /api/public/validate-pin`; test `TestFlowTest.validatePin_WhenClosedInstance_ShouldReturn400`.
- **Comportamiento actual:** con una instancia `CLOSED`, `validate-pin` devolvió **200** y `studentTestId`; `test-data` siguió devolviendo el examen.
- **Comportamiento esperado:** 400/409 con un error claro; el alumno no debe poder iniciar ni continuar un examen cerrado.
- **Evidencia:** comprobación local; test existente falla con “expected 400 but was 200”.
- **Impacto:** realización de exámenes fuera de plazo y acceso a contenido cerrado.
- **Recomendación:** validar `instance.isClosed()` en `validate-pin` y en todos los endpoints públicos; incluir estado en la consulta; test de regresión.
- **Test de verificación:** instancia cerrada + PIN/alumno válidos → 400/409; instancia cerrada + `test-data` → 403/409.

#### P-H05 — `answer` y `finish` no validan la relación pregunta/respuesta/test ni la instancia

- **Gravedad:** Alta.
- **Tipo:** Confirmado por código; no se ejecutó el caso cruzado.
- **Descripción:** `saveOrUpdate(id, questionId, answerId)` guarda cualquier combinación. No comprueba que `questionId` pertenezca al test de la instancia, que `answerId` pertenezca a esa pregunta, que la instancia esté abierta ni que el estado permita responder. `finish` solo mira `studentTest.isClosed()`, no `TestInstance.isClosed()`.
- **Ubicación:** `src/main/java/com/cttest/controller/PublicController.java:101-136`; `src/main/java/com/cttest/service/impl/StudentAnswerServiceImpl.java:27-47`; `src/main/java/com/cttest/service/impl/StudentTestServiceImpl.java:88-96`.
- **Pantallas / API / permisos / tests afectados:** `/take-test/:id`; `POST .../answer`, `POST .../finish`; `TestFlowTest` solo usa IDs válidos.
- **Comportamiento actual:** un cliente puede enviar un `questionId` de otro test o un `answerId` de otra pregunta; se crea/actualiza la respuesta y el cálculo de nota puede quedar manipulado. Un test cerrado a nivel de instancia puede seguir recibiendo respuestas si su `StudentTest` no está cerrado.
- **Comportamiento esperado:** validar pertenencia y estado en servidor; responder solo preguntas del test de la instancia; responder solo respuestas de esa pregunta; rechazar instancia cerrada; idempotencia y control de concurrencia.
- **Evidencia:** lectura de código; endpoint local con IDs válidos devolvió 200; el caso cruzado no se ejecutó para no alterar datos fuera de H2.
- **Impacto:** corrupción de respuestas, notas incorrectas y bypass del cierre del examen.
- **Recomendación:** cargar la pregunta y la respuesta y comprobar `question.test.id == test.id` y `answer.question.id == question.id`; validar estado; unique constraint `(student_test_id, question_id)`; test negativo.
- **Test de verificación:** `questionId` de otro test → 400; `answerId` de otra pregunta → 400; instancia cerrada → 400/409; doble respuesta simultánea → una sola fila.

#### P-H06 — Legacy: no hay autorización de rol ni de centro en servidor

- **Gravedad:** Alta.
- **Tipo:** Confirmado por código; no se ejecutó la aplicación legacy.
- **Descripción:** `AuthFilter` solo comprueba sesión. Los beans de búsqueda y formularios no comprueban rol. `checkLoggedUserAuthorization` nunca se llama; la llamada de `TestFormBean` está comentada. Los servicios BL tampoco comprueban permisos.
- **Ubicación:** `legacy/src/main/java/cttest/AuthFilter.java:51-72`; `legacy/src/main/java/cttest/BaseManagedBean.java:77-82`; `legacy/src/main/java/cttest/TestFormBean.java:95-103`; `legacy/src/main/java/cttest/TestInstanceFormBean.java:99-129`; `UserSearchBean`, `CenterSearchBean`, `StudentSearchBean`, `StudentGroupSearchBean`; `legacy/src/main/java/bl/impl/*`; `legacy/src/main/webapp/template/app_sidebar_lg.xhtml:35-47`.
- **Pantallas / API / permisos / tests afectados:** todas las páginas no públicas; CRUD de usuarios, centros, tests, alumnos, grupos; resultados de instancias; no hay tests.
- **Comportamiento actual:** cualquier usuario autenticado puede abrir directamente `centerSearchWithTemplate.xhtml` o `userSearchWithTemplate.xhtml` y ejecutar acciones CRUD; también puede abrir resultados de cualquier instancia por ID. El menú solo oculta centros/usuarios.
- **Comportamiento esperado:** comprobación de rol y centro en el filtro o en un `PhaseListener`; autorización en cada bean/acción; denegación por defecto; el ocultado de menú es solo UX.
- **Evidencia:** `AuthFilter` no consulta `loggedUser.isAdmin()`; `grep` de `checkLoggedUserAuthorization` solo encuentra la definición y una llamada comentada; los BL no reciben el usuario para autorizar.
- **Impacto:** un usuario no administrador puede administrar la aplicación y acceder a datos de otros centros.
- **Recomendación:** añadir un interceptor/PhaseListener de autorización; roles y centro en servidor; retirar o aislar las páginas de administración; tests de acceso directo y de acción.
- **Test de verificación:** usuario no admin abre `userSearchWithTemplate.xhtml` → 403/redirect; invoca `save`/`delete` → denegado; usuario centro A abre instancia B → 403.

#### P-H07 — Frontend: ausencia de guards de administrador y de ocultación de acciones

- **Gravedad:** Alta.
- **Tipo:** Confirmado por código.
- **Descripción:** `PrivateRoute` solo comprueba autenticación. `/users` y `/centers` no tienen guard de rol. Las páginas `Users`, `Centers`, `Tests`, `TestDetail`, `TestInstances`, `TestInstanceDetail`, `Students` y `StudentGroups` no consultan `isAdmin` para ocultar botones de creación/edición/borrado.
- **Ubicación:** `frontend/src/components/PrivateRoute.tsx:4-8`; `frontend/src/App.tsx:39-55`; `frontend/src/components/Layout.tsx:20-31`; páginas citadas.
- **Pantallas / API / permisos / tests afectados:** todas las rutas privadas; no hay tests de UI.
- **Comportamiento actual:** un USER ve botones que al pulsarlos producen un 500 (no 403) y puede abrir rutas de administración. La seguridad real depende del backend, que no protege las lecturas.
- **Comportamiento esperado:** `AdminRoute`/`RoleGuard` en React; ocultar o deshabilitar acciones no permitidas; mensaje claro de 403; el backend sigue siendo la última barrera.
- **Evidencia:** `grep` de `isAdmin` solo aparece en `Layout` y en el formulario de usuarios; no hay guards en rutas.
- **Impacto:** confusión, exposición de datos y superficie de ataque; un usuario puede descubrir funcionalidades administrativas.
- **Recomendación:** crear `AdminRoute` y `Can`/`useAuthorization`; aplicar a rutas y botones; tests de renderizado/rol.
- **Test de verificación:** USER en `/users` → redirect o pantalla 403; USER no ve botón “Crear”; ADMIN sí.

#### P-H08 — Las denegaciones de autorización se convierten en 500

- **Gravedad:** Alta (corrección de seguridad y observabilidad).
- **Tipo:** Confirmado (ejecución local).
- **Descripción:** `GlobalExceptionHandler` captura `RuntimeException` y devuelve 500. Una denegación de `@PreAuthorize` (por ejemplo, USER haciendo POST en `/api/centers`) no llega como 403, sino como error genérico 500. Además, no se registra la excepción.
- **Ubicación:** `src/main/java/com/cttest/exception/GlobalExceptionHandler.java:22-26`; `src/main/java/com/cttest/controller/BaseController.java:39-60`.
- **Pantallas / API / permisos / tests afectados:** todas las escrituras administrativas; frontend que muestra errores; tests negativos.
- **Comportamiento actual:** `POST /api/centers` con token USER devolvió **500** con `{"status":500,"message":"Internal server error"}`. No se registró traza.
- **Comportamiento esperado:** 403 con mensaje claro; log de seguridad con usuario, recurso y acción; sin filtrar detalles internos al cliente.
- **Evidencia:** comprobación local; `SecurityTest` no prueba USER en escritura.
- **Impacto:** los clientes no distinguen “no autorizado” de “error del servidor”; se dificulta la auditoría y se pueden enmascarar vulnerabilidades.
- **Recomendación:** añadir `@ExceptionHandler(AccessDeniedException.class)` antes del genérico o no capturar `AccessDeniedException`; registrar `WARN` con contexto; tests de 403.
- **Test de verificación:** USER POST/PUT/DELETE en cada recurso → 403; ADMIN → 2xx; logs contienen el intento.

### 8.3 Medios

| ID | Gravedad | Hallazgo | Evidencia / ubicación | Comportamiento esperado | Impacto | Recomendación / test |
|---|---|---|---|---|---|---|
| P-M01 | Media | Cookies JWT sin `Secure`, sin `SameSite`, CSRF deshabilitado y logout sin revocación | `AuthController.java:44-49,83-90`; `SecurityConfig.java:36`; `LoginResponse.java:3-10` | Cookie `Secure`/`SameSite`, CSRF si se usa cookie, rotación y revocación | Robo de sesión, CSRF y tokens válidos tras logout | Configurar flags, rotación/blacklist, tests de logout/CSRF. |
| P-M02 | Media | Sin rate limiting en login ni en `validate-pin` | `AuthController.java:34`; `PublicController.java:44` | Límite por IP/usuario/PIN y backoff | Fuerza bruta de contraseñas y PIN de 6 dígitos | Añadir bucket4j/Resilience4j y tests 429. |
| P-M03 | Media | `GlobalExceptionHandler` no registra la causa de los 500 | `GlobalExceptionHandler.java:22-26` | Log con excepción y contexto, respuesta genérica | Diagnóstico imposible; los tests no explican los 500 | Añadir logger y `@ExceptionHandler` específicos; test de logs. |
| P-M04 | Media | Entidades y PII expuestas en respuestas públicas/autenticadas | `PublicController.java:71-77,93-97`; `StudentTestController.java:28-38` | DTOs mínimos sin `addUser`, `modUser`, versión, etc. | Fuga de datos personales y de auditoría | Crear DTOs públicos; tests de campos permitidos. |
| P-M05 | Media | Refresh token como `Bearer` produce 500; el filtro no comprueba `type` | `JwtAuthenticationFilter.java:30-34`; `JwtTokenProvider.java` | Rechazar refresh en API (401) y aceptar solo access | Ruido/errores 500 y superficie innecesaria | Validar `type=access`; test refresh como bearer → 401. |
| P-M06 | Media | Suite por defecto rota (20 errores) y tests sin trackear | `src/test/resources/application-test.yml`; `git status` | Suite reproducible en CI y tests versionados | No se puede confiar en la cobertura | Añadir `NON_KEYWORDS=USER` o renombrar tabla; trackear tests; CI. |
| P-M07 | Media | El formulario público envía `studentId` (PK) pero la UI/legacy usan el identificador del alumno | `HomePage.tsx:18-21`; `StudentTestRepository.java:17`; `HomeBean.java:51-60` | Un único campo y semántica documentada; buscar por `identifier` | Alumnos legítimos no pueden entrar o se ven obligados a usar IDs internos | Cambiar a `studentIdentifier` y consultar por `identifier`; test. |

### 8.4 Bajos

| ID | Gravedad | Hallazgo | Evidencia / ubicación | Impacto | Recomendación / test |
|---|---|---|---|---|---|
| P-L01 | Baja | `anyRequest().permitAll()` deja públicos recursos no previstos; `/actuator/health` es público | `SecurityConfig.java:44` | Superficie de exposición si se añaden endpoints | `denyAll` por defecto y permitir explícitamente; proteger actuator. |
| P-L02 | Baja | CORS con `localhost:*` y credenciales | `SecurityConfig.java:54-57` | En producción puede sobrar amplitud | Orígenes exactos por entorno; test de preflight. |
| P-L03 | Baja | Legacy usa `requestURI.contains(TEST_DEMO_INIT_URI)` para decidir página pública | `AuthFilter.java:52-53` | Posible confusión de rutas | Comparación exacta o patrón acotado; test de path traversal. |
| P-L04 | Baja | Documentación y tests desactualizados/no versionados; `README.md` vacío | `README.md`; `ANALISIS.md`; `git status` | Dificulta auditoría y onboarding | Actualizar README/ANALISIS y versionar tests. |
| P-L05 | Baja | Fallback `centerId: 1` al crear test | `frontend/src/pages/Tests.tsx:193` | Asigna tests al centro 1 si falta el dato | Hacer el campo obligatorio o derivarlo del usuario; test. |

---

## 9. Tests recomendados

Los siguientes tests deben ser de integración con MockMvc (backend) y de componente/E2E (frontend/legacy). Deben usar dos centros y dos usuarios (ADMIN, USER) como mínimo.

| ID | Escenario | Resultado esperado |
|---|---|---|
| T-01 | Anónimo `GET /api/users` | 403 |
| T-02 | USER `GET /api/users`, `/api/centers`, `/api/students`, `/api/student-groups`, `/api/tests`, `/api/test-instances`, `/api/student-tests` | 403 o datos filtrados a su centro; nunca datos de otro centro |
| T-03 | USER `POST/PUT/DELETE` en cada recurso administrativo | 403 (no 500) |
| T-04 | USER `GET /api/tests/{id}/with-questions` | Sin campo `correct` o 403 |
| T-05 | ADMIN `GET /api/tests/{id}/with-questions` | 200 con `correct` |
| T-06 | USER centro A `GET /api/students?centerId=B` | 403 |
| T-07 | Público `GET /api/public/student-tests/{id}/test-data` sin token | 401/403 |
| T-08 | Alumno A con token accede a `studentTestId` de B | 403 |
| T-09 | Alumno A con token accede a su propio test | 200 y datos correctos |
| T-10 | Token caducado o manipulado | 401 |
| T-11 | `validate-pin` con instancia `CLOSED` | 400/409 |
| T-12 | `test-data`/`answer`/`finish` con instancia `CLOSED` | 400/409 |
| T-13 | `answer` con `questionId` de otro test | 400 |
| T-14 | `answer` con `answerId` de otra pregunta | 400 |
| T-15 | Doble `answer` concurrente para misma pregunta | Una sola fila/actualización idempotente |
| T-16 | `finish` dos veces | Primera 200; segunda 400/409 |
| T-17 | `finish` de un test ya cerrado por instancia | 400/409 |
| T-18 | `logout` y uso posterior del access token | 401 (si se implanta revocación) |
| T-19 | `refresh` rotado y reutilización del anterior | 401 |
| T-20 | Login/PIN con N intentos fallidos | 429/backoff |
| T-21 | React: USER navega a `/users` y `/centers` | Redirect/403; no renderiza datos |
| T-22 | React: USER no ve botones de crear/editar/borrar | Botones ausentes o deshabilitados |
| T-23 | Legacy: USER abre `userSearchWithTemplate.xhtml` y `centerSearchWithTemplate.xhtml` | 403/redirect |
| T-24 | Legacy: USER invoca acciones `save`/`delete` de esos beans | Denegado |
| T-25 | Legacy: enlace `studentTest.xhtml?id=` de otro alumno | 403/400 |
| T-26 | Suite completa en CI con H2 | 0 fallos, 0 errores |

Además, deben añadirse tests de contrato para los DTOs públicos que verifiquen que no aparecen campos sensibles (`correct`, `password`, `addUser`, `modUser`, `version`).

---

## 10. Discrepancias entre `ANALISIS.md` y la implementación

`ANALISIS.md` (2026-05-22) está desactualizado. Estas son las discrepancias relevantes para permisos y tests:

| Elemento de `ANALISIS.md` | Estado actual | Evidencia |
|---|---|---|
| C1: respuestas correctas expuestas en `/public/.../test-data` | **Corregido** en el endpoint público: usa `TestDataMapper`, que no incluye `correct`. Pero sigue expuesto en `/api/tests/{id}/with-questions` y `/api/questions/{id}`. | `TestDataMapper.java`; `AnswerDTO.java`; P-H01. |
| C2: sin control de roles en endpoints admin | **Parcialmente corregido:** las escrituras tienen `@PreAuthorize` ADMIN. Las lecturas siguen abiertas y `/api/admin/**` no se usa. | `BaseController.java:39-60`; `SecurityConfig.java:41-43`. |
| C3: duplicados de `StudentAnswer` | **Corregido en código** con `saveOrUpdate`; no hay unique constraint en BD, por lo que queda una carrera residual. | `StudentAnswerServiceImpl.java:27-47`. |
| C4: `FORCE_LAZY_LOADING` | **Corregido:** no existe `JacksonConfig` con esa opción. | `grep` sin resultados. |
| C5: condiciones de carrera en examen | **Parcialmente corregido:** `@Transactional` y `@Version`, pero faltan bloqueo, validación de estado y token de sesión. | `PublicController.java:101-136`; P-H05. |
| C6: JWT en `localStorage` | **Corregido en frontend:** el token no se persiste en `localStorage`; se usa cookie HttpOnly y estado en memoria. La cookie sigue sin `Secure`/`SameSite` y el logout no revoca. | `AuthContext.tsx`; `AuthController.java:44-49`. |
| C7: secret JWT por defecto | **Corregido:** `app.jwt.secret: ${JWT_SECRET}` sin fallback; la aplicación falla si falta. | `application.yml:31`. |
| H1: endpoints públicos sin autenticación | **No corregido:** el IDOR sigue siendo el riesgo principal. | `PublicController.java`; P-C01. |
| H2: entidades expuestas | **Parcialmente corregido:** hay DTOs para varias listas, pero `StudentTestController` y `PublicController` siguen devolviendo entidades. | P-M04. |
| H4: sin rate limiting | **No corregido.** | P-M02. |
| H9: PIN sin unique constraint | **Corregido:** `@Column(unique = true)` y constraint en entidad; la generación check+insert sigue sin ser atómica, pero la BD protege. | `TestInstance.java`. |
| M13: sin tests de integración | **Parcialmente corregido:** existen tests, pero no están trackeados y la suite por defecto falla. | P-M06. |
| L1: sin refresh token | **Corregido:** hay refresh token, aunque sin rotación/revocación efectiva. | `AuthController.java:57-80`. |
| L8: sin chequeo de expiración en `validatePin` | **No corregido:** no se comprueba instancia cerrada. | P-H04. |
| M12: `centerId: 1` hardcodeado | **No corregido** en la creación de test. | `Tests.tsx:193`; P-L05. |

También hay una discrepancia de documentación general: `README.md` solo contiene el título y no explica arranque, roles, configuración ni tests.

---

## 11. Plan de corrección priorizado

### P0 — Seguridad de acceso y datos (inmediato)

| Orden | Acción | Esfuerzo estimado | Dependencias |
|---:|---|---:|---|
| 1 | Implantar token de sesión público opaco para el flujo del alumno y eliminar el uso de `studentTestId` como credencial | 2–4 días | Diseño del token, almacenamiento/expiración |
| 2 | Validar en cada endpoint público: token, PIN/alumno, estado de instancia y pertenencia pregunta/respuesta | 1–2 días | Punto 1 |
| 3 | Cerrar lecturas por rol y centro: usuarios, centros, alumnos, grupos, tests, instancias y resultados | 2–4 días | Decidir modelo de roles (ADMIN global vs admin de centro) |
| 4 | Corregir `GlobalExceptionHandler` para devolver 403 en `AccessDeniedException` y registrar el intento | 0,5 día | — |
| 5 | Añadir `AdminRoute`/`RoleGuard` y ocultar acciones en React | 1–2 días | Punto 3 |
| 6 | Corregir H2 (`NON_KEYWORDS=USER` o renombrar tabla) y versionar los tests | 0,5 día | CI |

### P1 — Legacy y cobertura

| Orden | Acción | Esfuerzo estimado | Dependencias |
|---:|---|---:|---|
| 7 | Añadir autorización de rol/centro en legacy (filtro/PhaseListener + beans) | 2–4 días | Modelo de roles |
| 8 | Sustituir AES estático por token opaco o retirar el flujo legacy | 1–3 días | Punto 1 |
| 9 | Implementar tests negativos T-01 a T-26 | 3–5 días | Puntos 1–8 |
| 10 | Añadir rate limiting en login y PIN | 1 día | Infraestructura de límites |
| 11 | DTOs públicos sin PII ni campos de auditoría | 1–2 días | — |

### P2 — Endurecimiento y mantenimiento

| Orden | Acción | Esfuerzo estimado | Dependencias |
|---:|---|---:|---|
| 12 | Cookie `Secure`/`SameSite`, CSRF si se usa cookie, rotación/revocación de tokens | 1–2 días | Decisión de despliegue HTTPS |
| 13 | Revisar CORS por entorno y `anyRequest()`/actuator | 0,5 día | — |
| 14 | Actualizar `README.md` y `ANALISIS.md`; documentar roles y matriz de permisos | 1 día | — |
| 15 | Añadir unique constraint de `student_answer` y auditoría de cambios | 1 día | Migración BD |

**Dependencia crítica:** antes de implementar el aislamiento por centro hay que confirmar si el modelo de negocio es “un ADMIN global” o “administradores por centro”. Sin esa decisión, cualquier scoping corre el riesgo de romper la operativa.

---

## 12. Limitaciones del análisis y aspectos pendientes de verificar

- **No se ejecutó la aplicación legacy** con su stack JSF/DB real; los hallazgos legacy son de revisión estática de código y vistas.
- **No se probó contra la base de datos de producción** ni con datos reales. Las comprobaciones dinámicas se hicieron sobre H2 local con datos sintéticos.
- **No se hizo pentesting ni escaneo dinámico**; no se han probado XSS, CSRF real, SSRF, path traversal ni concurrencia.
- **La causa raíz de los 500 en `finish`/`answer` no está confirmada** porque `GlobalExceptionHandler` no registra la excepción. La hipótesis de `orphanRemoval` al reemplazar `Test.questions` requiere una traza o un test aislado.
- **No se verificó la regla de negocio de `studentId` vs `identifier`**: el formulario público usa `studentId`, el repositorio interpreta la PK y el legacy usa el identificador. Debe confirmarse cuál es el dato que el alumno conoce.
- **No se verificó si los alumnos tienen cuenta `ROLE_USER`**. Si la tienen, P-H01 y P-H02 pasan a ser críticos; si no, el riesgo se limita al personal.
- **No se auditaron todas las vistas legacy línea a línea**; se revisaron filtros, beans principales, sidebar y formularios de administración. Puede haber acciones JSF adicionales sin control.
- **No se evaluó la configuración de despliegue** (HTTPS, proxy, cabeceras, secretos, backups); el informe se limita a código y configuración del repositorio.
- **No se incluyen secretos, tokens ni credenciales** en este documento; las pruebas locales usaron un entorno efímero.

---

### Resumen final

- **Críticos:** 2 (IDOR público moderno y legacy).
- **Altos:** 8 (respuestas correctas, falta de aislamiento, administración legible, instancia cerrada, validación de respuestas, legacy sin RBAC, frontend sin guards, 500 en denegaciones).
- **Medios:** 7 (JWT/cookies/CSRF, rate limiting, logging de errores, PII, refresh token, suite rota, identificador vs PK).
- **Bajos:** 5 (actuator/anyRequest, CORS, `contains` legacy, documentación/tests, `centerId` hardcodeado).
- **Tests ejecutados:** suite por defecto (33 tests, 20 errores) y workaround H2 (33 tests, 6 fallos). Los fallos confirman los problemas de instancia cerrada y los 500 del flujo público.
- **Archivo generado:** `/home/victor/git/CTTest_v2/ANALISIS_PERMISOS.md`.
