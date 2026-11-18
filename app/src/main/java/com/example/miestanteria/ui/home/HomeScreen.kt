package com.example.miestanteria.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.ui.components.BookCover
import com.example.miestanteria.ui.components.BottomNavBar
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.theme.OutlineBorder
import com.example.miestanteria.ui.theme.ProgressTrack
import com.example.miestanteria.ui.theme.SageContainer
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/**
 * Panel principal (HomeFragment): saludo, libros que se estan leyendo con su avance
 * y la biblioteca en cuadricula con filtro por estado.
 */
@Composable
fun HomeScreen(
    userName: String,
    biblioteca: List<ItemBiblioteca>,
    onOpenBook: (String) -> Unit,
    onTabSelected: (NavTab) -> Unit
) {
    var filtro by rememberSaveable { mutableStateOf<EstadoLectura?>(null) }
    val leyendo = biblioteca.filter { it.estado == EstadoLectura.LEYENDO }
    val grilla = if (filtro == null) biblioteca else biblioteca.filter { it.estado == filtro }

    Scaffold(
        bottomBar = { BottomNavBar(selectedTab = NavTab.INICIO, onTabSelected = onTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 104.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Encabezado(userName, onPerfil = { onTabSelected(NavTab.PERFIL) })
                    Spacer(modifier = Modifier.height(16.dp))
                    BarraBusqueda(onClick = { onTabSelected(NavTab.EXPLORAR) })
                    Spacer(modifier = Modifier.height(22.dp))
                    Text("Leyendo ahora", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (leyendo.isEmpty()) {
                        SinLecturas(onClick = { onTabSelected(if (biblioteca.isEmpty()) NavTab.EXPLORAR else NavTab.ESTANTERIA) })
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(leyendo, key = { it.id }) { item ->
                                LeyendoCard(item, onClick = { onOpenBook(item.libro.apiId) })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                    Text("Mi Biblioteca", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filtros: List<EstadoLectura?> = listOf(null) + EstadoLectura.entries
                        items(filtros) { f ->
                            FilterChip(
                                selected = filtro == f,
                                onClick = { filtro = f },
                                label = { Text(f?.etiqueta ?: "Todos") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SagePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            if (grilla.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        if (biblioteca.isEmpty()) "Aún no tienes libros. Búscalos en Explorar y agrégalos a tu biblioteca."
                        else "No tienes libros en este estado.",
                        fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(grilla, key = { it.id }) { item ->
                    Column(modifier = Modifier.clickable { onOpenBook(item.libro.apiId) }) {
                        BookCover(libro = item.libro, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            item.libro.titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(item.libro.autores, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(userName: String, onPerfil: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val nombre = userName.substringBefore(' ').ifBlank { "lector" }
            Text("Hola, $nombre", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("¿Qué leeremos hoy?", fontSize = 13.sp, color = TextSecondary)
        }
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(SageContainer)
                .border(1.dp, SagePrimary.copy(alpha = 0.3f), CircleShape)
                .clickable(onClick = onPerfil),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = SagePrimary, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun BarraBusqueda(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, OutlineBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text("Buscar libros en Google Books…", fontSize = 14.sp, color = TextSecondary)
    }
}

@Composable
private fun SinLecturas(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SageContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoStories, contentDescription = null, tint = SagePrimary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("No estás leyendo nada", fontWeight = FontWeight.SemiBold, color = SagePrimary)
                Text("Marca un libro como \"Leyendo\" para seguir tu avance aquí.", fontSize = 12.sp, color = SagePrimary.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun LeyendoCard(item: ItemBiblioteca, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(libro = item.libro, modifier = Modifier.width(56.dp), cornerRadius = 6.dp, compacta = true)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.libro.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.libro.autores, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                val p = item.progreso
                Text(
                    if (p != null) "${(p * 100).toInt()}% leído" else "Página ${item.paginaActual}",
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SagePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { p ?: 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = SagePrimary, trackColor = ProgressTrack, strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
