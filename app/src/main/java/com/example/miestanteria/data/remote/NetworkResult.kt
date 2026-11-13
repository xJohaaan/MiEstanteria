package com.example.miestanteria.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Resultado de una operacion de red: exito con datos, o error con un mensaje para mostrar al usuario. */
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>
}

/** Ejecuta una llamada al servidor y convierte cualquier fallo en un mensaje legible en espanol. */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (e: HttpException) {
    NetworkResult.Error(mensajeDeError(e), e.code())
} catch (e: IOException) {
    NetworkResult.Error("No se pudo conectar con el servidor. Revisa tu conexion a internet.")
} catch (e: Exception) {
    NetworkResult.Error("Ocurrio un error inesperado.")
}

private val gson = Gson()

internal fun mensajeDeError(e: HttpException): String {
    val cuerpo = try {
        e.response()?.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    val mensaje = try {
        cuerpo?.let { gson.fromJson(it, ApiError::class.java)?.error }
    } catch (_: Exception) {
        null
    }
    return mensaje ?: when (e.code()) {
        401 -> "Tu sesion vencio. Inicia sesion de nuevo."
        403 -> "No tienes permiso para hacer esto."
        404 -> "No se encontro lo que buscabas."
        in 500..599 -> "El servidor tuvo un problema. Intenta mas tarde."
        else -> "Error ${e.code()}"
    }
}
