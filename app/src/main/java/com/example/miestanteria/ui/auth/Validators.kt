package com.example.miestanteria.ui.auth

/**
 * Reglas de validacion de los formularios (RF01, RF02).
 * Devuelven null si el dato es valido, o el mensaje de error a mostrar.
 * Son funciones puras, por eso se prueban con JUnit sin emulador.
 */
object Validators {

    const val MIN_PASSWORD = 8
    const val MIN_NOMBRE = 3

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun email(value: String): String? = when {
        value.isBlank() -> "Escribe tu correo"
        !EMAIL_REGEX.matches(value.trim()) -> "El correo no es valido"
        else -> null
    }

    fun passwordLogin(value: String): String? =
        if (value.isEmpty()) "Escribe tu contrasena" else null

    fun passwordRegistro(value: String): String? = when {
        value.isEmpty() -> "Escribe una contrasena"
        value.length < MIN_PASSWORD -> "Debe tener al menos $MIN_PASSWORD caracteres"
        else -> null
    }

    fun confirmacion(password: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Confirma tu contrasena"
        password != confirmacion -> "Las contrasenas no coinciden"
        else -> null
    }

    fun nombre(value: String): String? = when {
        value.isBlank() -> "Escribe tu nombre de usuario"
        value.trim().length < MIN_NOMBRE -> "Debe tener al menos $MIN_NOMBRE caracteres"
        value.trim().length > 100 -> "Maximo 100 caracteres"
        else -> null
    }
}
