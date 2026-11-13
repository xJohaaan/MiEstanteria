package com.example.miestanteria

import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.AuthGateway
import com.example.miestanteria.data.session.Session
import com.example.miestanteria.ui.auth.LoginViewModel
import com.example.miestanteria.ui.auth.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Servidor falso: responde lo que el test le indique y cuenta las llamadas. */
private class FakeAuth(var result: NetworkResult<Session>) : AuthGateway {
    var calls = 0
    override suspend fun login(email: String, password: String): NetworkResult<Session> { calls++; return result }
    override suspend fun register(nombre: String, email: String, password: String): NetworkResult<Session> { calls++; return result }
}

private val SESSION = Session("tok", Long.MAX_VALUE, 1, "Sofia", "sofia@correo.com")

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelsTest {

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    // ---------- Login (RF02) ----------

    @Test
    fun loginConCamposVaciosNoLlamaAlServidor() {
        val fake = FakeAuth(NetworkResult.Success(SESSION))
        val vm = LoginViewModel(fake)
        vm.login()
        assertEquals(0, fake.calls)
        assertNotNull(vm.state.value.emailError)
        assertNotNull(vm.state.value.passwordError)
    }

    @Test
    fun loginCorrectoMarcaSesionIniciada() {
        val vm = LoginViewModel(FakeAuth(NetworkResult.Success(SESSION)))
        vm.onEmailChange("sofia@correo.com")
        vm.onPasswordChange("secreta123")
        vm.login()
        assertTrue(vm.state.value.loggedIn)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun loginIncorrectoMuestraMensajeDelServidor() {
        val vm = LoginViewModel(FakeAuth(NetworkResult.Error("Correo o contrasena incorrectos", 401)))
        vm.onEmailChange("sofia@correo.com")
        vm.onPasswordChange("mala")
        vm.login()
        assertFalse(vm.state.value.loggedIn)
        assertEquals("Correo o contrasena incorrectos", vm.state.value.errorMessage)
    }

    @Test
    fun escribirBorraElErrorDelCampo() {
        val vm = LoginViewModel(FakeAuth(NetworkResult.Success(SESSION)))
        vm.login()
        vm.onEmailChange("s")
        assertNull(vm.state.value.emailError)
    }

    // ---------- Registro (RF01) ----------

    @Test
    fun registroValidaEnTiempoReal() {
        val vm = RegisterViewModel(FakeAuth(NetworkResult.Success(SESSION)))
        vm.onEmailChange("sofia@")
        assertEquals("El correo no es valido", vm.state.value.emailError)
        vm.onPasswordChange("corta")
        assertNotNull(vm.state.value.passwordError)
        vm.onConfirmacionChange("otra")
        assertEquals("Las contrasenas no coinciden", vm.state.value.confirmacionError)
    }

    @Test
    fun registroConContrasenasDistintasNoLlamaAlServidor() {
        val fake = FakeAuth(NetworkResult.Success(SESSION))
        val vm = RegisterViewModel(fake)
        vm.onNombreChange("Sofia")
        vm.onEmailChange("sofia@correo.com")
        vm.onPasswordChange("secreta123")
        vm.onConfirmacionChange("secreta124")
        vm.register()
        assertEquals(0, fake.calls)
        assertFalse(vm.state.value.registered)
    }

    @Test
    fun registroCorrecto() {
        val fake = FakeAuth(NetworkResult.Success(SESSION))
        val vm = RegisterViewModel(fake)
        vm.onNombreChange("Sofia")
        vm.onEmailChange("sofia@correo.com")
        vm.onPasswordChange("secreta123")
        vm.onConfirmacionChange("secreta123")
        vm.register()
        assertEquals(1, fake.calls)
        assertTrue(vm.state.value.registered)
    }

    @Test
    fun registroConCorreoExistenteMuestraError() {
        val vm = RegisterViewModel(FakeAuth(NetworkResult.Error("Ya existe una cuenta con ese correo", 409)))
        vm.onNombreChange("Sofia")
        vm.onEmailChange("sofia@correo.com")
        vm.onPasswordChange("secreta123")
        vm.onConfirmacionChange("secreta123")
        vm.register()
        assertEquals("Ya existe una cuenta con ese correo", vm.state.value.errorMessage)
    }
}
