# Mi Estantería

Aplicación Android para gestionar tu vida lectora (biblioteca personal, reseñas, recomendaciones y estadísticas).
Proyecto de aula — Aplicaciones Móviles, Unidades Tecnológicas de Santander (UTS).

| Parte | Tecnología | Carpeta |
|---|---|---|
| App móvil | Android Studio · Kotlin · Jetpack Compose (Material 3) · MVVM · Retrofit · DataStore | `app/` |
| Servidor (API REST) | Java 21 · Spring Boot 4 · Spring Security + JWT · JPA · Flyway | `backend/` |
| Base de datos | MySQL 8.0 | creada por `backend/src/main/resources/db/migration` |
| Libros | Google Books API (consultada por el servidor) | — |

## 1. Requisitos

- Android Studio (trae el JDK que usa Gradle).
- MySQL Server 8.0 corriendo en `localhost:3306`.
- Emulador Android 7.0 (API 24) o superior.

## 2. Levantar el servidor

1. Copia `backend/local.properties.example` como `backend/local.properties` y escribe tu contraseña de MySQL.
   (Ese archivo no se sube a git.)
2. Desde la carpeta `backend/`:

   ```bash
   ./gradlew bootRun
   ```

   La primera vez Flyway crea la base `mi_estanteria` y todas las tablas.
3. Comprueba que responde: <http://localhost:8080/api/v1/salud> → `{"estado":"ok"}`.

Variables opcionales (para la nube o para cambiar valores):

| Variable | Para qué |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a MySQL |
| `JWT_SECRET` | Clave de firma de los tokens (mínimo 32 caracteres) |
| `JWT_EXPIRATION_MINUTES` | Duración de la sesión (por defecto 1440 = 24 h) |
| `GOOGLE_BOOKS_API_KEY` | Clave de Google Books (sin ella Google limita las búsquedas) |

## 3. Ejecutar la app

Abre la carpeta raíz del proyecto en Android Studio y ejecuta la configuración **app** en el emulador.
En modo debug la app llama a `http://10.0.2.2:8080/api/v1/` (10.0.2.2 es tu PC visto desde el emulador).

> **Android 17 (API 37) o superior:** la primera vez la app pide el permiso de "dispositivos cercanos"
> (`ACCESS_LOCAL_NETWORK`). Hay que aceptarlo: sin él Android bloquea la conexión al servidor del PC.
> Ese permiso solo existe en la versión debug; la versión final usa el servidor en internet.
>
> Si Windows muestra el aviso del Firewall al arrancar el servidor, permite el acceso a Java.

La contraseña de MySQL se puede poner en `backend/local.properties` o en el `local.properties` de la raíz.

Para un celular físico o el servidor en la nube, agrega en `local.properties` (raíz):

```properties
api.url.debug=http://IP_DE_TU_PC:8080/api/v1/
api.url.release=https://TU-SERVIDOR/api/v1/
```

## 4. Pruebas automáticas

```bash
./gradlew :app:testDebugUnitTest
```

```bash
cd backend && ./gradlew test
```

## 5. Endpoints disponibles

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Crear cuenta |
| POST | `/api/v1/auth/login` | Iniciar sesión (devuelve JWT) |
| GET | `/api/v1/usuarios/me` | Datos del usuario de la sesión |
| GET | `/api/v1/libros/buscar?q=&tipo=titulo\|autor\|categoria&pagina=0` | Buscar libros (Google Books; si falla, Open Library) |
| GET | `/api/v1/libros/{apiId}` | Detalle de un libro |
| GET | `/api/v1/biblioteca?estado=LEYENDO` | Ver mi biblioteca (filtro opcional por estado) |
| POST | `/api/v1/biblioteca` | Agregar un libro con estado y página |
| PUT | `/api/v1/biblioteca/{id}` | Cambiar estado o página actual |
| DELETE | `/api/v1/biblioteca/{id}` | Quitar un libro de mi biblioteca |
| GET | `/api/v1/libros/{apiId}/resenas` | Reseñas de un libro con su promedio |
| POST | `/api/v1/resenas` | Publicar reseña (1-5 estrellas, comentario ≤ 500) |
| PUT | `/api/v1/resenas/{id}` | Editar mi reseña |
| DELETE | `/api/v1/resenas/{id}` | Eliminar mi reseña |
| POST | `/api/v1/resenas/{id}/me-gusta` | Dar Me gusta (no a reseñas propias) |
| DELETE | `/api/v1/resenas/{id}/me-gusta` | Quitar Me gusta |
| GET | `/api/v1/usuarios/{id}/resenas` | Reseñas escritas por un usuario |
| POST | `/api/v1/recomendaciones` | Recomendar un libro a otro lector (mensaje ≤ 300) |
| GET | `/api/v1/recomendaciones/recibidas` | Recomendaciones que me enviaron |
| PUT | `/api/v1/recomendaciones/{id}/visto` | Marcar recomendación como vista |
| GET | `/api/v1/feed` | Muro: recomendaciones recibidas y reseñas recientes |
| GET | `/api/v1/usuarios/buscar?q=` | Buscar lectores por nombre |
| GET | `/api/v1/salud` | Estado del servidor |

### Reglas de la comunidad

- Solo se puede reseñar un libro que está en la biblioteca propia, y una sola reseña por libro (se puede editar).
- No se puede dar Me gusta a la reseña propia; repetir el Me gusta no lo duplica.
- No se puede recomendar un libro a uno mismo.
- Las fechas viajan en UTC (`2026-11-18T03:57:50Z`) y la app las muestra en la hora local del celular.

### Búsqueda de libros

El servidor consulta primero **Google Books**. Si Google no responde o limita las consultas (error 429,
común sin API key), usa automáticamente **Open Library** (gratuita, sin clave), así la búsqueda nunca queda vacía.
Con una clave de Google (`GOOGLE_BOOKS_API_KEY`) se usa Google casi siempre.

### Funcionamiento sin conexión

La app guarda la biblioteca en una base local (Room/SQLite). Sin internet se puede abrir la app,
ver la estantería, el progreso y el detalle de los libros guardados; los cambios requieren conexión.

Todas las rutas excepto `auth/*` y `salud` requieren la cabecera `Authorization: Bearer <token>`.
