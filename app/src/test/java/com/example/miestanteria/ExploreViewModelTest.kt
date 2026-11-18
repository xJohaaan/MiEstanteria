package com.example.miestanteria

import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.CatalogoGateway
import com.example.miestanteria.data.repository.TipoBusqueda
import com.example.miestanteria.model.Libro
import com.example.miestanteria.ui.explore.ExploreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeCatalogo : CatalogoGateway {
    val llamadas = mutableListOf<Triple<String, TipoBusqueda, Int>>()
    var resultado: (Int) -> NetworkResult<List<Libro>> = { pagina ->
        NetworkResult.Success(List(20) { Libro("id-$pagina-$it", "Libro $it", "Autor") })
    }
    override suspend fun buscar(texto: String, tipo: TipoBusqueda, pagina: Int): NetworkResult<List<Libro>> {
        llamadas += Triple(texto, tipo, pagina)
        return resultado(pagina)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun noBuscaConMenosDeTresLetras() = runTest(dispatcher) {
        val fake = FakeCatalogo()
        val vm = ExploreViewModel(fake, flowOf(emptySet()))
        vm.onQueryChange("ha")
        advanceUntilIdle()
        assertTrue(fake.llamadas.isEmpty())
    }

    @Test
    fun autocompletadoEsperaQueElUsuarioDejeDeEscribir() = runTest(dispatcher) {
        val fake = FakeCatalogo()
        val vm = ExploreViewModel(fake, flowOf(emptySet()))
        vm.onQueryChange("har")
        advanceTimeBy(200)
        vm.onQueryChange("harr")
        advanceTimeBy(200)
        vm.onQueryChange("harry")
        advanceTimeBy(ExploreViewModel.DEBOUNCE_MS + 10)
        runCurrent()
        assertEquals(1, fake.llamadas.size)
        assertEquals("harry", fake.llamadas.single().first)
        assertEquals(20, vm.state.value.resultados.size)
    }

    @Test
    fun cambiarElTipoVuelveABuscarConEseFiltro() = runTest(dispatcher) {
        val fake = FakeCatalogo()
        val vm = ExploreViewModel(fake, flowOf(emptySet()))
        vm.buscarAhora("Garcia Marquez")
        advanceUntilIdle()
        vm.onTipoChange(TipoBusqueda.AUTOR)
        advanceUntilIdle()
        assertEquals(TipoBusqueda.AUTOR, fake.llamadas.last().second)
    }

    @Test
    fun cargarMasPideLaSiguientePaginaYAcumula() = runTest(dispatcher) {
        val fake = FakeCatalogo()
        val vm = ExploreViewModel(fake, flowOf(emptySet()))
        vm.buscarAhora("dune")
        advanceUntilIdle()
        vm.cargarMas()
        advanceUntilIdle()
        assertEquals(1, fake.llamadas.last().third)
        assertEquals(40, vm.state.value.resultados.size)
    }

    @Test
    fun siLaPaginaVieneIncompletaNoHayMas() = runTest(dispatcher) {
        val fake = FakeCatalogo().apply { resultado = { NetworkResult.Success(listOf(Libro("x", "X", "A"))) } }
        val vm = ExploreViewModel(fake, flowOf(emptySet()))
        vm.buscarAhora("raro")
        advanceUntilIdle()
        assertFalse(vm.state.value.hayMas)
        vm.cargarMas()
        advanceUntilIdle()
        assertEquals(1, fake.llamadas.size)
    }

    @Test
    fun errorDeRedSeMuestra() = runTest(dispatcher) {
        val fake = FakeCatalogo().apply { resultado = { NetworkResult.Error("Sin conexion") } }
        val vm = ExploreViewModel(fake, flowOf(emptySet()))
        vm.buscarAhora("dune")
        advanceUntilIdle()
        assertEquals("Sin conexion", vm.state.value.error)
        assertFalse(vm.state.value.cargando)
    }

    @Test
    fun limpiarBorraResultados() = runTest(dispatcher) {
        val vm = ExploreViewModel(FakeCatalogo(), flowOf(emptySet()))
        vm.buscarAhora("dune")
        advanceUntilIdle()
        vm.limpiar()
        assertTrue(vm.state.value.resultados.isEmpty())
        assertFalse(vm.state.value.buscado)
    }
}
