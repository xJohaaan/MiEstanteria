package com.example.miestanteria.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.OutlinedButton
import com.example.miestanteria.model.Resena
import com.example.miestanteria.ui.components.Estrellas
import com.example.miestanteria.ui.components.RecomendarDialog
import com.example.miestanteria.ui.components.ResenaCard
import com.example.miestanteria.ui.components.ResenaDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.model.Libro
import com.example.miestanteria.ui.components.AgregarLibroDialog
import com.example.miestanteria.ui.components.BookCover
import com.example.miestanteria.ui.components.ConfirmDialog
import com.example.miestanteria.ui.components.EstadoSelector
import com.example.miestanteria.ui.components.LoadingView
import com.example.miestanteria.ui.components.MessageView
import com.example.miestanteria.ui.components.PaginaDialog
import com.example.miestanteria.ui.theme.ProgressTrack
import com.example.miestanteria.ui.theme.SageContainer
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/** Ficha completa del libro (BookDetailFragment del documento). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(viewModel: BookDetailViewModel, onBack: () -> Unit, onOpenLector: (Long) -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val item by viewModel.item.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    var mostrarAgregar by rememberSaveable { mutableStateOf(false) }
    var mostrarPagina by rememberSaveable { mutableStateOf(false) }
    var confirmarQuitar by rememberSaveable { mutableStateOf(false) }
    var mostrarResena by rememberSaveable { mutableStateOf(false) }
    var confirmarBorrarResena by rememberSaveable { mutableStateOf(false) }
    var mostrarRecomendar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del libro", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    state.libro?.let { libro ->
                        IconButton(onClick = { mostrarRecomendar = true }, modifier = Modifier.testTag("detail_recomendar")) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Recomendar a un lector")
                        }
                        IconButton(onClick = { compartirLibro(context, libro) }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val libro = state.libro
        when {
            state.cargando && libro == null -> LoadingView(Modifier.padding(padding))
            libro == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                MessageView(
                    icon = Icons.Default.CloudOff,
                    title = "No se pudo cargar el libro",
                    message = state.error ?: "Intenta de nuevo",
                    actionLabel = "Reintentar",
                    onAction = viewModel::cargar
                )
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Encabezado(libro)
                Spacer(modifier = Modifier.height(20.dp))
                MiLectura(
                    item = item,
                    trabajando = state.trabajando,
                    onAgregar = { mostrarAgregar = true },
                    onEstado = viewModel::cambiarEstado,
                    onEditarPagina = { mostrarPagina = true },
                    onQuitar = { confirmarQuitar = true }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Sinopsis", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                when {
                    libro.descripcion != null -> Text(
                        BookDetailViewModel.textoPlano(libro.descripcion),
                        fontSize = 14.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onBackground
                    )
                    state.cargando -> CircularProgressIndicator(color = SagePrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else -> Text("Este libro no tiene sinopsis disponible.", fontSize = 14.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(24.dp))
                SeccionResenas(
                    state = state,
                    enBiblioteca = item != null,
                    onEscribir = { mostrarResena = true },
                    onEliminar = { confirmarBorrarResena = true },
                    onMeGusta = viewModel::alternarMeGusta,
                    onReintentar = viewModel::cargarResenas,
                    onOpenLector = onOpenLector
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    val libro = state.libro
    if (mostrarAgregar && libro != null) {
        AgregarLibroDialog(
            totalPaginas = libro.paginas,
            onDismiss = { mostrarAgregar = false },
            onConfirm = { estado, pagina ->
                mostrarAgregar = false
                viewModel.agregar(estado, pagina)
            }
        )
    }
    val actual = item
    if (mostrarPagina && actual != null) {
        PaginaDialog(
            paginaActual = actual.paginaActual,
            totalPaginas = actual.libro.paginas,
            onDismiss = { mostrarPagina = false },
            onConfirm = {
                mostrarPagina = false
                viewModel.actualizarPagina(it)
            }
        )
    }
    if (mostrarResena) {
        val mia = state.resenas?.mia
        ResenaDialog(
            titulo = if (mia == null) "Escribir reseña" else "Editar mi reseña",
            estrellasIniciales = mia?.calificacion ?: 0,
            comentarioInicial = mia?.comentario ?: "",
            onDismiss = { mostrarResena = false },
            onConfirm = { estrellas, comentario ->
                mostrarResena = false
                viewModel.publicarResena(estrellas, comentario)
            }
        )
    }
    if (confirmarBorrarResena) {
        ConfirmDialog(
            title = "Eliminar reseña",
            message = "Tu reseña y sus me gusta se borrarán.",
            confirmLabel = "Eliminar",
            onDismiss = { confirmarBorrarResena = false },
            onConfirm = {
                confirmarBorrarResena = false
                viewModel.eliminarResena()
            }
        )
    }
    if (mostrarRecomendar && libro != null) {
        RecomendarDialog(
            libro = libro,
            buscar = viewModel::buscarLectores,
            onDismiss = { mostrarRecomendar = false },
            onEnviar = { lector, mensaje ->
                mostrarRecomendar = false
                viewModel.recomendar(lector, mensaje)
            }
        )
    }
    if (confirmarQuitar) {
        ConfirmDialog(
            title = "Quitar de mi biblioteca",
            message = "Se borrará tu progreso de lectura de este libro.",
            confirmLabel = "Quitar",
            onDismiss = { confirmarQuitar = false },
            onConfirm = {
                confirmarQuitar = false
                viewModel.quitar(onDone = {})
            }
        )
    }
}

@Composable
private fun Encabezado(libro: Libro) {
    Row(verticalAlignment = Alignment.Top) {
        BookCover(libro = libro, modifier = Modifier.width(120.dp), cornerRadius = 10.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                libro.titulo, fontSize = 21.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onBackground, maxLines = 4, overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(libro.autores, fontSize = 14.sp, color = SagePrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))
            Dato("Año", libro.anio)
            Dato("Editorial", libro.editorial)
            Dato("Páginas", libro.paginas?.toString())
            libro.categoria?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    it, fontSize = 12.sp, color = SagePrimary, fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(SageContainer, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String?) {
    if (valor.isNullOrBlank()) return
    Row {
        Text("$etiqueta: ", fontSize = 13.sp, color = TextSecondary)
        Text(valor, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MiLectura(
    item: ItemBiblioteca?,
    trabajando: Boolean,
    onAgregar: () -> Unit,
    onEstado: (EstadoLectura) -> Unit,
    onEditarPagina: () -> Unit,
    onQuitar: () -> Unit
) {
    if (item == null) {
        Button(
            onClick = onAgregar,
            enabled = !trabajando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("detail_agregar"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = Color.White)
        ) {
            if (trabajando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar a mi biblioteca", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mi lectura", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (trabajando) CircularProgressIndicator(color = SagePrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            EstadoSelector(estado = item.estado, onChange = onEstado, enabled = !trabajando)

            if (item.estado != EstadoLectura.POR_LEER) {
                Spacer(modifier = Modifier.height(16.dp))
                val progreso = item.progreso
                if (progreso != null) {
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = SagePrimary,
                        trackColor = ProgressTrack,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val total = item.libro.paginas?.let { " de $it" } ?: ""
                    val pct = progreso?.let { " · ${(it * 100).toInt()}%" } ?: ""
                    Text("Página ${item.paginaActual}$total$pct", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    if (item.estado == EstadoLectura.LEYENDO) {
                        TextButton(onClick = onEditarPagina, enabled = !trabajando) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Actualizar")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onQuitar, enabled = !trabajando) {
                    Icon(Icons.Default.BookmarkRemove, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quitar de mi biblioteca", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** Seccion de resenas del libro: promedio, la resena propia y las de otros lectores. */
@Composable
private fun SeccionResenas(
    state: BookDetailUiState,
    enBiblioteca: Boolean,
    onEscribir: () -> Unit,
    onEliminar: () -> Unit,
    onMeGusta: (Resena) -> Unit,
    onReintentar: () -> Unit,
    onOpenLector: (Long) -> Unit
) {
    val resenas = state.resenas
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Reseñas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        if (resenas != null && resenas.total > 0) {
            Spacer(modifier = Modifier.width(10.dp))
            Estrellas(resenas.promedio, 16.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text("${resenas.promedio} (${resenas.total})", fontSize = 13.sp, color = TextSecondary)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))

    when {
        state.cargandoResenas && resenas == null ->
            CircularProgressIndicator(color = SagePrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        resenas == null -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.errorResenas ?: "No se pudieron cargar las reseñas", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
            TextButton(onClick = onReintentar) { Text("Reintentar") }
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val mia = resenas.mia
            if (mia != null) {
                ResenaCard(resena = mia, onMeGusta = {}, onEditar = onEscribir, onEliminar = onEliminar)
            } else if (enBiblioteca) {
                OutlinedButton(
                    onClick = onEscribir,
                    enabled = !state.trabajando,
                    modifier = Modifier.fillMaxWidth().testTag("detail_escribir_resena"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escribir reseña")
                }
            } else {
                Text(
                    "Agrega este libro a tu biblioteca para poder reseñarlo.",
                    fontSize = 13.sp, color = TextSecondary
                )
            }
            if (resenas.deOtros.isEmpty()) {
                Text(
                    if (mia == null) "Nadie ha reseñado este libro todavía. ¡Sé el primero!" else "Aún no hay reseñas de otros lectores.",
                    fontSize = 13.sp, color = TextSecondary
                )
            }
            resenas.deOtros.forEach { r -> ResenaCard(resena = r, onMeGusta = { onMeGusta(r) }, onAbrirAutor = { onOpenLector(r.autor.id) }) }
        }
    }
}

/** Compartir nativo de Android (WhatsApp, correo, etc.). */
private fun compartirLibro(context: android.content.Context, libro: Libro) {
    val texto = "Te recomiendo \"${libro.titulo}\" de ${libro.autores}. Lo encontré en Mi Estantería 📚"
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Compartir libro"))
}
