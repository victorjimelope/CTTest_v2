# Guía de arranque - CTTest_v2

## Requisitos

- Java 17
- Node.js 18+
- Maven 3.8+
- MySQL corriendo en `localhost:8033` con base de datos `demo`
- Variables de entorno necesarias (ver más abajo)

## Configuración de Java

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

## Arranque con 3 terminales (modo desarrollo)

### Terminal 1 - Legacy JSF/RichFaces (Tomcat embebido via Cargo) - Puerto 8080

```bash
cd /home/victor/git/CTTest_v2/legacy
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn cargo:run
```

Acceso: http://localhost:8080/CTTest/  
Usuario/Contraseña: `admin` / `admin`

Si el puerto está ocupado:
```bash
lsof -ti:8080 | xargs kill -9
```

### Terminal 2 - Backend Spring Boot - Puerto 8081

```bash
cd /home/victor/git/CTTest_v2
fuser -k 8081/tcp 2>/dev/null

JWT_SECRET=YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE= \
DB_URL=jdbc:mysql://localhost:8033/demo \
DB_USERNAME=root \
DB_PASSWORD=msandbox \
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
mvn spring-boot:run
```

### Terminal 3 - Frontend React + Vite - Puerto 5173

```bash
cd /home/victor/git/CTTest_v2/frontend
npm run dev
```

Acceso: http://localhost:5173

## Despliegue en Tomcat externo (Legacy)

Para generar el WAR del proyecto legacy:

```bash
cd /home/victor/git/CTTest_v2/legacy
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn clean package
```

El WAR se generará en `legacy/target/CTTest.war` (o similar). Copiarlo al directorio `webapps` de Tomcat:

```bash
cp legacy/target/*.war $CATALINA_BASE/webapps/CTTest.war
```

Tomcat desplegará automáticamente la aplicación. Acceso: http://<host>:<port>/CTTest/

## Despliegue del Backend Spring Boot

### Como JAR

```bash
cd /home/victor/git/CTTest_v2
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn clean package -DskipTests
java -jar target/cttest-*.jar
```

Configurar las variables de entorno necesarias (`JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

### Con Docker

```bash
docker compose up -d
```

## Notas

- El backend nuevo requiere obligatoriamente `JWT_SECRET` codificado en Base64 con al menos 32 bytes.
- La base de datos debe estar levantada antes de arrancar los servicios.
- El proyecto legacy usa Cargo con Tomcat 9 embebido para desarrollo.
