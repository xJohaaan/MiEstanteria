package com.example.miestanteria.ui.shelf

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.ui.components.BookCover
import com.example.miestanteria.ui.components.BottomNavBar
import com.example.miestanteria.ui.components.LoadingView
import com.example.miestanteria.ui.components.MessageView
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.components.PaginaDialog
import com.example.miestanteria.ui.theme.ProgressTrack
import com.example.miestanteria.ui.theme.SageContainer
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/** "Mi Estanteria" (MyShelfFragment): lista personal con filtros por estado y deslizar para quitar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(
    viewModel: ShelfViewModel,
    onOpenBook: (String) -> Unit,
    onTabSelected: (NavTab) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refrescando by viewModel.refrescando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var editando by remember { mutableStateOf<ItemBiblioteca?>(null) }
    var menuOrden by remember { mutableStateOf(false) }

    LaunchedEffect(mensaje) {
        val m = mensaje ?: return@LaunchedEffect
        // Se limpia el mensaje DESPUES de mostrarlo; limpiarlo antes cancelaria este mismo efecto
        if (m == ShelfViewModel.QUITADO) {
            val r = snackbar.showSnackbar(m, actionLabel = "Deshacer", duration = SnackbarDuration.Short)
            viewModel.mensajeMostrado()
            if (r == SnackbarResult.ActionPerformed) viewModel.deshacerQuitar()
        } else {
            snackbar.showSnackbar(m)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(selectedTab = NavTab.ESTANTERIA, onTabSelected = onTabSelected) },
        floatingActionButton = {
            if (state.conteo[null] != 0) {
                FloatingActionButton(
                    onClick = { onTabSelected(NavTab.EXPLORAR) },
                    containerColor = SagePrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar libro")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mi Estantería", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    val total = state.conteo[null] ?: 0
                    Text(if (total == 1) "1 libro" else "$total libros", fontSize = 13.sp, color = TextSecondary)
                }
                Box {
                    IconButton(onClick = { menuOrden = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    }
                    DropdownMenu(expanded = menuOrden, onDismissRequest = { menuOrden = false }) {
                        OrdenEstanteria.entries.forEach { o ->
                            DropdownMenuItem(
                                text = {
                                    Text(o.etiqueta, fontWeight = if (o == state.orden) FontWeight.Bold else FontWeight.Normal)
                                },
                                onClick = {
                                    viewModel.onOrden(o)
                                    menuOrden = false
                                }
                            )
                        }
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filtros: List<EstadoLectura?> = listOf(null) + EstadoLectura.entries
                items(filtros) { f ->
                    FilterChip(
                        selected = state.filtro == f,
                        onClick = { viewModel.onFiltro(f) },
                        label = { Text("${f?.etiqueta ?: "Todos"} (${state.conteo[f] ?: 0})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SagePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = refrescando,
                onRefresh = viewModel::refrescar,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.cargandoInicial -> LoadingView()
                    state.items.isEmpty() -> LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            MessageView(
                                icon = Icons.Outlined.CollectionsBookmark,
                                title = if (state.filtro == null) "Tu estantería está vacía" else "Nada en \"${state.filtro!!.etiqueta}\"",
                                message = if (state.filtro == null) "Busca libros en Explorar y agrégalos aquí."
                                else "Cambia el filtro o agrega más libros.",
                                actionLabel = "Explorar libros",
                                onAction = { onTabSelected(NavTab.EXPLORAR) },
                                modifier = Modifier.padding(top = 48.dp)
                            )
                        }
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            DeslizableParaQuitar(onQuitar = { viewModel.quitar(item) }) {
                                ShelfItem(
                                    item = item,
                                    onClick = { onOpenBook(item.libro.apiId) },
                                    onEditarPagina = { editando = item }
                                )
                            }
                        }
                        item {
                            Text(
                                "Desliza un libro hacia la izquierda para quitarlo",
                                fontSize = 12.sp, color = TextSecondary,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    editando?.let { item ->
        PaginaDialog(
            paginaActual = item.paginaActual,
            totalPaginas = item.libro.paginas,
            onDismiss = { editando = null },
            onConfirm = {
                editando = null
                viewModel.actualizarPagina(item, it)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeslizableParaQuitar(onQuitar: () -> Unit, content: @Composable () -> Unit) {
    // confirmValueChange puede llamarse varias veces durante el gesto: se quita una sola vez
    var quitado by remember { mutableStateOf(false) }
    val estado = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                if (!quitado) {
                    quitado = true
                    onQuitar()
                }
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = estado,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (estado.targetValue == SwipeToDismissBoxValue.EndToStart) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.errorContainer,
                label = "fondo"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(14.dp))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = Color.White)
            }
        }
    ) { content() }
}

@Composable
private fun ShelfItem(item: ItemBiblioteca, onClick: () -> Unit, onEditarPagina: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(libro = item.libro, modifier = Modifier.width(56.dp), cornerRadius = 6.dp, compacta = true)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.libro.titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface
                )
                Text(item.libro.autores, fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                EstadoBadge(item.estado)
                if (item.estado == EstadoLectura.LEYENDO) {
                    Spacer(modifier = Modifier.height(6.dp))
                    item.progreso?.let { p ->
                        LinearProgressIndicator(
                            progress = { p },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = SagePrimary, trackColor = ProgressTrack, strokeCap = StrokeCap.Round
                        )
                    }
                    val total = item.libro.paginas?.let { "/$it" } ?: ""
                    Text("Pág. ${item.paginaActual}$total", fontSize = 12.sp, color = TextSecondary)
                }
            }
            if (item.estado != EstadoLectura.LEIDO) {
                IconButton(onClick = onEditarPagina) {
                    Icon(Icons.Default.Edit, contentDescription = "Actualizar página", tint = SagePrimary)
                }
            }
        }
    }
}

@Composable
fun EstadoBadge(estado: EstadoLectura) {
    val (fondo, texto) = when (estado) {
        EstadoLectura.POR_LEER -> MaterialTheme.colorScheme.surfaceVariant to TextSecondary
        EstadoLectura.LEYENDO -> SageContainer to SagePrimary
        EstadoLectura.LEIDO -> SagePrimary to Color.White
    }
    Text(
        estado.etiqueta, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = texto,
        modifier = Modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
