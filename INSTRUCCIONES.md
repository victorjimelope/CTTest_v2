# CTTest_v2 - API REST + Frontend React (+ Legacy JSF)

## Requisitos

- Java 17 (`/usr/lib/jvm/java-17-openjdk-amd64`)
- Node.js 18+
- MySQL corriendo en `localhost:8033` con BD `demo`

> **Importante:** Maven debe ejecutarse con Java 17. Si el comando `java -version` muestra Java 11, anteponer `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` a cada comando `mvn`, o fijarlo permanentemente:
> ```bash
> echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
> source ~/.bashrc
> ```

## Arrancar los 3 proyectos (side-by-side)

Necesitas **3 terminales** para comparar el original vs el migrado:

### Terminal 1 — Proyecto original (JSF/RichFaces) en :8080

```bash
cd /home/victor/git/CTTest_v2/legacy
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn cargo:run
```

Abrir: http://localhost:8080/CTTest/ (login: admin / admin)

> Si da error de puerto ocupado: `lsof -ti:8080 | xargs kill -9`

### Terminal 2 — Backend nuevo (Spring Boot) en :8081

```bash
cd /home/victor/git/CTTest_v2
fuser -k 8081/tcp 2>/dev/null
# Nota: JWT_SECRET es obligatorio y debe ser Base64 de al menos 32 bytes (256 bits)
JWT_SECRET=YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE= \
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
mvn spring-boot:run
```

### Terminal 3 — Frontend React en :5173

```bash
cd /home/victor/git/CTTest_v2/frontend
npm run dev
```

Abrir: http://localhost:5173

---

## Probar la API nueva

### 1. Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### 2. Endpoints protegidos

```bash
TOKEN="el_token_del_login"

curl http://localhost:8081/api/centers -H "Authorization: Bearer $TOKEN"
curl http://localhost:8081/api/tests -H "Authorization: Bearer $TOKEN"
curl http://localhost:8081/api/users -H "Authorization: Bearer $TOKEN"
curl http://localhost:8081/api/students -H "Authorization: Bearer $TOKEN"
```

## Endpoints disponibles (API nueva)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/login | Login (público) |
| POST | /api/public/validate-pin | Validar PIN (público) |
| GET/POST | /api/centers | CRUD centros |
| GET/PUT/DELETE | /api/centers/{id} | CRUD centro |
| GET/POST | /api/tests | CRUD tests |
| GET/PUT/DELETE | /api/tests/{id} | CRUD test |
| GET | /api/tests/{id}/with-questions | Test con preguntas |
| POST | /api/tests/{id}/questions | Crear pregunta |
| GET/POST | /api/test-instances | CRUD instancias |
| GET/POST | /api/students | CRUD estudiantes |
| PUT/DELETE | /api/students/{id} | CRUD estudiante |
| GET/POST | /api/student-groups | CRUD grupos |
| PUT/DELETE | /api/student-groups/{id} | CRUD grupo |
| GET | /api/student-tests | Listar tests de estudiantes |
| GET/POST | /api/users | CRUD usuarios |
| PUT/DELETE | /api/users/{id} | CRUD usuario |

## Estructura del proyecto

```
CTTest_v2/
├── legacy/           ← Original JSF/RichFaces (main) — puerto 8080
├── pom.xml           ← Nuevo backend Spring Boot — puerto 8081
├── frontend/         ← React + Vite — puerto 5173
└── docker-compose.yml
```

## Conexión BD (variables de entorno)

```bash
DB_URL=jdbc:mysql://localhost:3306/demo \
DB_USERNAME=root \
DB_PASSWORD=msandbox \
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
  mvn spring-boot:run
```

## Docker

```bash
docker compose up -d
```
