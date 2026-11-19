package com.example.miestanteria.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miestanteria.data.repository.TipoBusqueda
import com.example.miestanteria.model.Libro
import com.example.miestanteria.ui.components.BookCover
import com.example.miestanteria.ui.components.BottomNavBar
import com.example.miestanteria.ui.components.LoadingView
import com.example.miestanteria.ui.components.MessageView
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.theme.OutlineBorder
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

private val SUGERENCIAS = listOf("Gabriel García Márquez", "Cien años de soledad", "Harry Potter", "Ciencia ficción", "Isabel Allende")

/** Rangos de ano que se ofrecen como filtro rapido (RF03, seccion 9.1 del documento). */
private data class RangoAnio(val etiqueta: String, val desde: Int?, val hasta: Int?)

private val RANGOS_ANIO = listOf(
    RangoAnio("Todos los años", null, null),
    RangoAnio("2020 o después", 2020, null),
    RangoAnio("2010–2019", 2010, 2019),
    RangoAnio("2000–2009", 2000, 2009),
    RangoAnio("Antes de 2000", null, 1999)
)

/** Pantalla Explorar (SearchFragment del documento): busqueda en Google Books con autocompletado. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onOpenBook: (String) -> Unit,
    onTabSelected: (NavTab) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val enBiblioteca by viewModel.idsEnBiblioteca.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Paginacion: al acercarse al final de la lista se piden mas resultados
    val cercaDelFinal by remember {
        derivedStateOf {
            val ultimo = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            ultimo >= listState.layoutInfo.totalItemsCount - 4
        }
    }
    LaunchedEffect(cercaDelFinal, state.resultados.size) {
        if (cercaDelFinal && state.resultados.isNotEmpty()) viewModel.cargarMas()
    }

    Scaffold(
        bottomBar = { BottomNavBar(selectedTab = NavTab.EXPLORAR, onTabSelected = onTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Explorar", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Busca entre millones de libros", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search"),
                    placeholder = { Text("Título, autor o categoría…", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = viewModel::limpiar) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = SagePrimary,
                        unfocusedBorderColor = OutlineBorder
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focus.clearFocus()
                        viewModel.buscarAhora()
                    })
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TipoBusqueda.entries) { tipo ->
                    FilterChip(
                        selected = state.tipo == tipo,
                        onClick = { viewModel.onTipoChange(tipo) },
                        label = { Text(tipo.etiqueta) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SagePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Filtro por ano de publicacion (seccion 9.1 del documento)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(RANGOS_ANIO) { rango ->
                    FilterChip(
                        selected = state.anioDesde == rango.desde && state.anioHasta == rango.hasta,
                        onClick = { viewModel.onAnioChange(rango.desde, rango.hasta) },
                        label = { Text(rango.etiqueta, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SagePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.cargando && state.resultados.isEmpty() -> LoadingView()

                    state.error != null && state.resultados.isEmpty() -> MessageView(
                        icon = Icons.Default.CloudOff,
                        title = "No pudimos buscar",
                        message = state.error!!,
                        actionLabel = "Reintentar",
                        onAction = { viewModel.buscarAhora() },
                        modifier = Modifier.align(Alignment.Center)
                    )

                    !state.buscado -> Column(modifier = Modifier.padding(16.dp)) {
                        Text("Prueba con", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SUGERENCIAS.forEach { s ->
                            AssistChip(
                                onClick = {
                                    focus.clearFocus()
                                    viewModel.buscarAhora(s)
                                },
                                label = { Text(s) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        }
                    }

                    state.resultados.isEmpty() -> MessageView(
                        icon = Icons.Default.SearchOff,
                        title = "Sin resultados",
                        message = "No encontramos libros para \"${state.query}\". Prueba con otras palabras o cambia el filtro.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.testTag("explore_results")
                    ) {
                        items(state.resultados, key = { it.apiId }) { libro ->
                            ResultadoItem(
                                libro = libro,
                                enBiblioteca = libro.apiId in enBiblioteca,
                                onClick = { onOpenBook(libro.apiId) }
                            )
                        }
                        if (state.cargandoMas) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = SagePrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultadoItem(libro: Libro, enBiblioteca: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(libro = libro, modifier = Modifier.width(56.dp), cornerRadius = 6.dp, compacta = true)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    libro.titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(libro.autores, fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val extra = listOfNotNull(libro.anio, libro.categoria, libro.paginas?.let { "$it págs." }).joinToString(" · ")
                if (extra.isNotEmpty()) {
                    Text(extra, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (enBiblioteca) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SagePrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("En tu biblioteca", fontSize = 11.sp, color = SagePrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
        }
    }
}
