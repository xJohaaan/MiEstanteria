package com.example.miestanteria

import com.example.miestanteria.ui.auth.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test fun emailValido() = assertNull(Validators.email("sofia.diaz@gmail.com"))
    @Test fun emailConEspaciosAlrededorEsValido() = assertNull(Validators.email("  sofia@correo.co  "))
    @Test fun emailVacio() = assertEquals("Escribe tu correo", Validators.email(""))
    @Test fun emailSinArroba() = assertEquals("El correo no es valido", Validators.email("sofiagmail.com"))
    @Test fun emailSinDominio() = assertNotNull(Validators.email("sofia@"))

    @Test fun passwordRegistroCorta() = assertNotNull(Validators.passwordRegistro("1234567"))
    @Test fun passwordRegistroMinima() = assertNull(Validators.passwordRegistro("12345678"))
    @Test fun passwordLoginVacia() = assertNotNull(Validators.passwordLogin(""))

    @Test fun confirmacionDistinta() = assertEquals("Las contrasenas no coinciden", Validators.confirmacion("abcdefgh", "abcdefgx"))
    @Test fun confirmacionIgual() = assertNull(Validators.confirmacion("abcdefgh", "abcdefgh"))

    @Test fun nombreCorto() = assertNotNull(Validators.nombre("ab"))
    @Test fun nombreSoloEspacios() = assertNotNull(Validators.nombre("    "))
    @Test fun nombreValido() = assertNull(Validators.nombre("Sofía"))
}
