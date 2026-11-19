package com.example.miestanteria.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miestanteria.model.Conteo
import com.example.miestanteria.model.Perfil
import com.example.miestanteria.ui.components.Avatar
import com.example.miestanteria.ui.components.BottomNavBar
import com.example.miestanteria.ui.components.LoadingView
import com.example.miestanteria.ui.components.MessageView
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.components.ResenaCard
import com.example.miestanteria.ui.theme.ProgressTrack
import com.example.miestanteria.ui.theme.SageContainer
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/**
 * Perfil con estadisticas de lectura (RF08).
 * Como pestana (perfil propio) muestra la barra inferior y "cerrar sesion";
 * como perfil publico de otro lector muestra una barra superior con "volver".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onOpenBook: (String) -> Unit,
    onOpenLector: (Long) -> Unit = {},
    onLogout: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onTabSelected: ((NavTab) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Perfil del lector", fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        bottomBar = { onTabSelected?.let { BottomNavBar(selectedTab = NavTab.PERFIL, onTabSelected = it) } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PullToRefreshBox(
                isRefreshing = state.cargando && state.perfil != null,
                onRefresh = viewModel::cargar,
                modifier = Modifier.fillMaxSize()
            ) {
                val perfil = state.perfil
                when {
                    perfil == null && state.error == null -> LoadingView()
                    perfil == null -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center
                    ) {
                        MessageView(
                            icon = Icons.Default.CloudOff,
                            title = "No se pudo cargar el perfil",
                            message = state.error ?: "",
                            actionLabel = "Reintentar",
                            onAction = viewModel::cargar
                        )
                    }
                    else -> ContenidoPerfil(perfil, onLogout, onSettingsClick, onOpenBook, onOpenLector, viewModel)
                }
            }

            // Diálogo de recomendación (documento 9.1 - UserProfileFragment)
            if (state.mostrarDialogoRecomendar) {
                DialogoRecomendar(viewModel)
            }
        }
    }
}

@Composable
private fun ContenidoPerfil(
    perfil: Perfil,
    onLogout: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
    onOpenBook: (String) -> Unit,
    onOpenLector: (Long) -> Unit,
    viewModel: ProfileViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Encabezado
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Avatar(perfil.lector, 64.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(perfil.lector.nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                perfil.miembroDesde?.let { Text(it, fontSize = 13.sp, color = TextSecondary) }
            }
            if (!perfil.esMio) {
                // Botón "Recomendar libro" (documento 9.1 - UserProfileFragment)
                Button(
                    onClick = { viewModel.mostrarDialogoRecomendar() },
                    modifier = Modifier.padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SagePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recomendar", fontSize = 14.sp)
                }
            } else if (onLogout != null) {
                if (onSettingsClick != null) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = SagePrimary)
                    }
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión", tint = SagePrimary)
                }
            }
        }
        perfil.biografia?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Estadisticas 2x2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.AutoMirrored.Filled.MenuBook, "Libros leídos", perfil.leidos.toString(), Modifier.weight(1f))
                StatCard(Icons.Default.AutoStories, "Páginas leídas", "%,d".format(perfil.paginasLeidas), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.RateReview, "Reseñas", perfil.resenasEscritas.toString(), Modifier.weight(1f))
                StatCard(Icons.Default.Favorite, "Me gusta recibidos", perfil.meGustasRecibidos.toString(), Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Estado de la estanteria
        Titulo("Su estantería".takeUnless { perfil.esMio } ?: "Mi estantería")
        Tarjeta {
            if (perfil.totalLibros == 0) {
                Text("Todavía no hay libros en la estantería.", fontSize = 14.sp, color = TextSecondary)
            } else {
                BarraConteo("Leídos", perfil.leidos, perfil.totalLibros)
                Spacer(modifier = Modifier.height(10.dp))
                BarraConteo("Leyendo", perfil.leyendo, perfil.totalLibros)
                Spacer(modifier = Modifier.height(10.dp))
                BarraConteo("Por leer", perfil.porLeer, perfil.totalLibros)
                if (perfil.resenasEscritas > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = com.example.miestanteria.ui.components.StarYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calificación promedio de las reseñas: ${perfil.promedioCalificacion}", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Generos favoritos
        Spacer(modifier = Modifier.height(24.dp))
        Titulo("Géneros favoritos")
        Tarjeta {
            if (perfil.generosFavoritos.isEmpty()) {
                Text("Aún no hay suficientes libros con categoría para calcularlo.", fontSize = 14.sp, color = TextSecondary)
            } else {
                val max = perfil.generosFavoritos.maxOf { it.cantidad }
                perfil.generosFavoritos.forEachIndexed { i, g ->
                    if (i > 0) Spacer(modifier = Modifier.height(10.dp))
                    BarraConteo(g.etiqueta, g.cantidad, max)
                }
            }
        }

        // Leidos por anio
        Spacer(modifier = Modifier.height(24.dp))
        Titulo("Libros leídos por año")
        Tarjeta {
            if (perfil.leidosPorAnio.isEmpty()) {
                Text("Marca libros como \"Leído\" para ver tu avance por año.", fontSize = 14.sp, color = TextSecondary)
            } else {
                GraficoBarras(perfil.leidosPorAnio)
            }
        }

        // Resenas
        Spacer(modifier = Modifier.height(24.dp))
        Titulo(if (perfil.esMio) "Mis reseñas" else "Sus reseñas")
        if (perfil.resenas.isEmpty()) {
            Text("Sin reseñas todavía.", fontSize = 14.sp, color = TextSecondary)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                perfil.resenas.forEach { r ->
                    ResenaCard(
                        resena = r,
                        onMeGusta = { viewModel.alternarMeGusta(r) },
                        mostrarLibro = true,
                        onAbrirLibro = { onOpenBook(r.libro.apiId) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun Tarjeta(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SageContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = SagePrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** Barra horizontal: etiqueta, cantidad y proporcion respecto a [maximo]. */
@Composable
private fun BarraConteo(etiqueta: String, cantidad: Int, maximo: Int) {
    Column {
        Row {
            Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(cantidad.toString(), fontSize = 13.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (maximo == 0) 0f else cantidad.toFloat() / maximo },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = SagePrimary, trackColor = ProgressTrack, strokeCap = StrokeCap.Round
        )
    }
}

/** Grafico de barras verticales (libros leidos por anio). */
@Composable
private fun GraficoBarras(datos: List<Conteo>) {
    val max = datos.maxOf { it.cantidad }.coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        datos.takeLast(6).forEach { d ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(d.cantidad.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SagePrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(fraction = (d.cantidad.toFloat() / max * 0.75f).coerceAtLeast(0.05f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(SagePrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(d.etiqueta, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

/**
 * Dialogo para recomendar un libro a otro lector (documento 9.1 - UserProfileFragment).
 * Muestra la biblioteca del usuario actual para seleccionar un libro y un campo de mensaje opcional.
 */
@Composable
private fun DialogoRecomendar(viewModel: ProfileViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val miBiblioteca by viewModel.miBiblioteca.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = { viewModel.ocultarDialogoRecomendar() },
        title = { Text("Recomendar un libro") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Selecciona un libro de tu biblioteca:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Lista de libros de la biblioteca del usuario
                if (miBiblioteca.isEmpty()) {
                    Text(
                        "No tienes libros en tu biblioteca aún.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(miBiblioteca) { item ->
                            val isSelected = state.libroSeleccionado?.apiId == item.libro.apiId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.seleccionarLibro(item.libro) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.libro.titulo,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            item.libro.autores,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Seleccionado",
                                            tint = SagePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Campo de mensaje opcional
                OutlinedTextField(
                    value = state.mensajeRecomendacion,
                    onValueChange = { 
                        if (it.length <= 300) viewModel.actualizarMensaje(it)
                    },
                    label = { Text("Mensaje (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = {
                        Text("${state.mensajeRecomendacion.length}/300")
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.enviarRecomendacion() },
                enabled = state.libroSeleccionado != null
            ) {
                Text("Enviar recomendación")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.ocultarDialogoRecomendar() }) {
                Text("Cancelar")
            }
        }
    )
}
