package com.example.miestanteria.ui.community

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.miestanteria.model.FechaRelativa
import com.example.miestanteria.model.Recomendacion
import com.example.miestanteria.ui.components.Avatar
import com.example.miestanteria.ui.components.BookCover
import com.example.miestanteria.ui.components.BottomNavBar
import com.example.miestanteria.ui.components.LoadingView
import com.example.miestanteria.ui.components.MessageView
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.components.ResenaCard
import com.example.miestanteria.ui.theme.SageContainer
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/** Muro de la comunidad: recomendaciones recibidas (RF07) y resenas recientes con me gusta (RF06). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    onOpenBook: (String) -> Unit,
    onTabSelected: (NavTab) -> Unit,
    onOpenLector: (Long) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    // Cada vez que se entra a la pestana se actualiza el muro
    LaunchedEffect(Unit) { viewModel.refrescar() }
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(selectedTab = NavTab.COMUNIDAD, onTabSelected = onTabSelected) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                Text("Comunidad", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Lo que leen y recomiendan otros lectores", fontSize = 13.sp, color = TextSecondary)
            }

            val noVistas = state.feed?.noVistas ?: 0
            PrimaryTabRow(selectedTabIndex = pestana, containerColor = MaterialTheme.colorScheme.background, contentColor = SagePrimary) {
                Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Para ti")
                        if (noVistas > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge { Text("$noVistas") }
                        }
                    }
                })
                Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Reseñas recientes") })
            }

            PullToRefreshBox(
                isRefreshing = state.cargando && state.feed != null,
                onRefresh = viewModel::refrescar,
                modifier = Modifier.fillMaxSize()
            ) {
                val feed = state.feed
                when {
                    feed == null && state.cargando -> LoadingView()
                    feed == null -> LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            MessageView(
                                icon = Icons.Default.CloudOff,
                                title = "Sin conexión con la comunidad",
                                message = state.error ?: "Intenta de nuevo",
                                actionLabel = "Reintentar",
                                onAction = viewModel::refrescar,
                                modifier = Modifier.padding(top = 48.dp)
                            )
                        }
                    }
                    pestana == 0 -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (feed.recomendaciones.isEmpty()) {
                            item {
                                MessageView(
                                    icon = Icons.Outlined.Inbox,
                                    title = "Aún no te han recomendado libros",
                                    message = "Cuando otro lector te recomiende un libro aparecerá aquí.",
                                    modifier = Modifier.padding(top = 32.dp)
                                )
                            }
                        }
                        items(feed.recomendaciones, key = { it.id }) { rec ->
                            RecomendacionCard(rec, onClick = {
                                viewModel.abrirRecomendacion(rec)
                                onOpenBook(rec.libro.apiId)
                            })
                        }
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (feed.resenasRecientes.isEmpty()) {
                            item {
                                MessageView(
                                    icon = Icons.Default.RateReview,
                                    title = "Todavía no hay reseñas",
                                    message = "Reseña un libro de tu estantería y sé el primero en aparecer aquí.",
                                    modifier = Modifier.padding(top = 32.dp)
                                )
                            }
                        }
                        items(feed.resenasRecientes, key = { it.id }) { r ->
                            ResenaCard(
                                resena = r,
                                onMeGusta = { viewModel.alternarMeGusta(r) },
                                mostrarLibro = true,
                                onAbrirLibro = { onOpenBook(r.libro.apiId) },
                                onAbrirAutor = { onOpenLector(r.autor.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecomendacionCard(rec: Recomendacion, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rec.visto) MaterialTheme.colorScheme.surface else SageContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(rec.emisor, 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${rec.emisor.nombre} te recomendó",
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(FechaRelativa.desde(rec.fecha), fontSize = 11.sp, color = TextSecondary)
                }
                if (!rec.visto) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(SagePrimary))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BookCover(libro = rec.libro, modifier = Modifier.width(48.dp), cornerRadius = 5.dp, compacta = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rec.libro.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(rec.libro.autores, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (!rec.mensaje.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("\"${rec.mensaje}\"", fontSize = 13.sp, fontStyle = FontStyle.Italic, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
