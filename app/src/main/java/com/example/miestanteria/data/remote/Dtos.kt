package com.example.miestanteria.data.remote

/*
 * Objetos JSON que se intercambian con el servidor (API REST /api/v1).
 * Los campos que el servidor puede omitir son nullable para que Gson no deje nulos en tipos no-null.
 */

data class RegisterRequest(
    val nombreUsuario: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val expiraEnSegundos: Long,
    val usuario: UsuarioDto
)

data class UsuarioDto(
    val id: Long,
    val nombreUsuario: String,
    val email: String?,
    val fotoPerfilUrl: String?,
    val biografia: String?,
    val fechaRegistro: String?
)

data class LibroDto(
    val id: Long?,
    val apiId: String,
    val titulo: String,
    val autores: String?,
    val editorial: String?,
    val fechaPublicacion: String?,
    val portadaUrl: String?,
    val descripcion: String?,
    val categoria: String?,
    val paginas: Int?
)

/** Libro que se envia al agregarlo a la biblioteca. */
data class LibroEntrada(
    val apiId: String,
    val titulo: String,
    val autores: String?,
    val editorial: String?,
    val fechaPublicacion: String?,
    val portadaUrl: String?,
    val descripcion: String?,
    val categoria: String?,
    val paginas: Int?
)

data class AgregarBibliotecaRequest(
    val libro: LibroEntrada,
    val estadoLectura: String,
    val paginaActual: Int?
)

/** Los campos null no se envian (Gson los omite), asi se puede cambiar solo uno. */
data class ActualizarBibliotecaRequest(
    val estadoLectura: String? = null,
    val paginaActual: Int? = null
)

data class BibliotecaItemDto(
    val id: Long,
    val libro: LibroDto,
    val estadoLectura: String?,
    val paginaActual: Int?,
    val fechaAgregado: String?
)

/** Cuerpo de error que devuelve el servidor: {"status": 409, "error": "mensaje"} */
data class ApiError(
    val status: Int?,
    val error: String?
)
