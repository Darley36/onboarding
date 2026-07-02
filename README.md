# Taller Nequi — Proyecto base (Clean Architecture)

Checklist de contenido actualizado:

- Descripción y propósito del proyecto
- Arquitectura y módulos principales
- Requisitos y configuración
- Comandos para levantar dependencias (Docker)
- Cómo ejecutar la aplicación y tests
- Endpoints y pruebas rápidas

Descripción
-----------
Proyecto base que implementa los principios de Clean Architecture con módulos Gradle. Incluye ejemplos de adaptadores de persistencia (R2DBC/Postgres), cache (Redis), mensajería (SQS/LocalStack) y una API reactiva construida con Spring Boot WebFlux.
Se expone 4 endpoints (Creacion de usuario, consulta por id, listado y búsqueda por nombre) y se integra con un servicio externo para obtener datos de usuario.
Se envia un evento a SQS cuando se crea un usuario.
Se recibe un evento de SQS para procesar la creación de un usuario en la base de datos.

Arquitectura (resumen)
----------------------
El repositorio sigue una separación en capas:

- `domain`: modelos del dominio, excepciones y puertos (interfaces)
- `usecase`: lógica de aplicación (casos de uso)
- `infrastructure`:
  - `driven-adapters`: adaptadores a infraestructuras externas (r2dbc-postgresql, redis, dynamo-db, sqs-sender, rest-consumer, ...)
  - `entry-points`: puntos de entrada (API reactiva, listeners SQS)
- `applications/app-service`: ensamblado de Spring Boot y arranque de la aplicación

Estructura destacada
--------------------

- `applications/app-service/` — módulo principal que arranca la aplicación Spring
- `domain/model/` — entidades, excepciones y gateways (p. ej. `UserDBGateway`)
- `domain/usecase/` — interfaces y casos de uso (`UserUseCase`, `UserEventUseCase`)
- `infrastructure/driven-adapters/` — adaptadores de persistencia y servicios
- `infrastructure/entry-points/reactive-web/` — router, handlers y configuración web (seguridad, manejo de errores)

Requisitos
----------

- Java 17 o 21 (según toolchain configurado en `build.gradle`)
- Docker (recomendado para servicios dependientes: Postgres, Redis, LocalStack)
- Gradle Wrapper incluido (`gradlew`, `gradlew.bat`)

Configuración
-------------

Revisa y ajusta la configuración en:

- `applications/app-service/src/main/resources/application.yaml` (o `application.properties`)

Verifica las propiedades de conexión a Postgres, Redis y LocalStack/AWS antes de ejecutar en local.

Comandos para ejecutar este servicio (Docker)
-------------------------------------------
Se muestran a continuación ejemplos de comandos Docker para levantar dependencias locales. Sustituye contraseñas/usuarios según tu política de seguridad.

Postgres (Docker):

```powershell
docker run --name my_postgres -e POSTGRES_PASSWORD=admin -e POSTGRES_USER=admin -p 5432:5432 -v pgdata:/var/lib/postgresql postgres
```

pgAdmin (Docker):

```powershell
docker run --name pgadmin-container -e PGADMIN_DEFAULT_EMAIL=admin@local.com -e PGADMIN_DEFAULT_PASSWORD=admin123 -p 5050:80 -d dpage/pgadmin4
```

Redis (Docker):

```powershell
docker run --name mi-redis -p 6379:6379 -d redis
```

LocalStack (ejemplo de descarga del CLI en Linux — comando proporcionado):

```bash
curl --output localstack-cli-2026.4.0-linux-amd64-onefile.tar.gz \
	--location https://github.com/localstack/localstack-cli/releases/download/v2026.4.0/localstack-cli-2026.4.0-linux-amd64-onefile.tar.gz

sudo tar xvzf localstack-cli-2026.4.0-linux-*-onefile.tar.gz -C /usr/local/bin
```

Nota: los comandos para LocalStack arriba son para sistemas Linux. En Windows se recomienda usar Docker o instalar LocalStack via pip siguiendo la guía oficial.

Create table (Postgres):
```powershell

```

Create SQS :
```powershell

```

Create dynamo table :
```powershell

```

---

Ejecutar la aplicación (Windows PowerShell)
-----------------------------------------

```powershell
# Ejecutar la aplicación
.\gradlew.bat :applications:app-service:bootRun

# Compilar y ejecutar tests
.\gradlew.bat clean build

# Ejecutar solo tests
.\gradlew.bat test
```

Endpoints y uso rápido
----------------------

Los endpoints dependen de la configuración del `reactive-web` router. Ejemplos habituales:

- POST /users?id={id} — crear usuario obteniendo datos de servicio externo
- GET /users/{id} — obtener usuario por id
- GET /users — listar usuarios
- GET /users/search?name={name} — buscar usuarios por nombre

Tests y cobertura
------------------

Ejemplo para ejecutar tests y generar reportes JaCoCo en varios módulos:

```powershell
.\gradlew.bat :applications:app-service:test :infrastructure:driven-adapters:r2dbc-postgresql:test :infrastructure:entry-points:reactive-web:test jacocoTestReport
```

Los reportes se generan en `build/reports/jacocoHtml/index.html` de cada módulo.

Buenas prácticas
---------------

- Revisa `application.yaml` antes de arrancar servicios conectados (Postgres, Redis, LocalStack)
- Usa contenedores Docker para mantener un entorno reproducible
- Añade tests para cualquier cambio en `domain` o `usecase` para preservar la lógica de negocio

Contribuciones
--------------

1. Crear una rama: `feature/<descripcion>`
2. Añadir tests y documentación de los cambios
3. Enviar Pull Request con descripción y evidencia de pruebas


Contacto
-------
Darley Agudelo Giraldo 