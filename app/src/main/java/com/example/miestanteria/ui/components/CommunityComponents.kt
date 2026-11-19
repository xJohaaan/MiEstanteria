package com.example.miestanteria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.model.FechaRelativa
import com.example.miestanteria.model.Lector
import com.example.miestanteria.model.Libro
import com.example.miestanteria.model.ReglasComunidad
import com.example.miestanteria.model.Resena
import com.example.miestanteria.ui.theme.SageContainer
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary
import kotlinx.coroutines.delay

val StarYellow = Color(0xFFF4B400)

/** Muestra una calificacion (admite medias estrellas para los promedios). */
@Composable
fun Estrellas(valor: Double, tamano: Dp = 16.dp) {
    Row {
        for (i in 1..5) {
            val icono = when {
                valor >= i -> Icons.Default.Star
                valor >= i - 0.5 -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(icono, contentDescription = null, tint = StarYellow, modifier = Modifier.size(tamano))
        }
    }
}

/** Selector de 1 a 5 estrellas tocando cada una. */
@Composable
fun SelectorEstrellas(valor: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        for (i in 1..5) {
            IconButton(onClick = { onChange(i) }, modifier = Modifier.testTag("estrella_$i")) {
                Icon(
                    if (i <= valor) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$i estrellas",
                    tint = StarYellow,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/** Circulo con la foto del lector o, si no tiene, sus iniciales. */
@Composable
fun Avatar(lector: Lector, tamano: Dp = 36.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(SageContainer)
            .border(1.dp, SagePrimary.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (lector.fotoUrl.isNullOrBlank()) {
            Iniciales(lector, tamano)
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(lector.fotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de ${lector.nombre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                loading = { Iniciales(lector, tamano) },
                error = { Iniciales(lector, tamano) }
            )
        }
    }
}

/** Iniciales del lector, usadas como respaldo cuando no hay foto. */
@Composable
private fun Iniciales(lector: Lector, tamano: Dp) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            lector.iniciales,
            fontSize = (tamano.value * 0.38f).sp,
            fontWeight = FontWeight.Bold,
            color = SagePrimary
        )
    }
}

/**
 * Tarjeta de resena. Si es propia muestra editar/eliminar; si es de otro, el boton "Me gusta" (RF06).
 * Con [mostrarLibro] incluye la portada y el titulo (para el muro de la comunidad).
 */
@Composable
fun ResenaCard(
    resena: Resena,
    onMeGusta: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarLibro: Boolean = false,
    onAbrirLibro: (() -> Unit)? = null,
    onEditar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null,
    onAbrirAutor: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = onAbrirAutor != null && !resena.esMia) { onAbrirAutor?.invoke() }
                ) {
                Avatar(resena.autor)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (resena.esMia) "${resena.autor.nombre} (tú)" else resena.autor.nombre,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Estrellas(resena.calificacion.toDouble(), 14.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(FechaRelativa.desde(resena.fecha), fontSize = 11.sp, color = TextSecondary)
                    }
                }
                }
                if (resena.esMia) {
                    onEditar?.let { IconButton(onClick = it) { Icon(Icons.Default.Edit, "Editar reseña", tint = SagePrimary) } }
                    onEliminar?.let {
                        IconButton(onClick = it) { Icon(Icons.Default.Delete, "Eliminar reseña", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }

            if (mostrarLibro) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = onAbrirLibro != null) { onAbrirLibro?.invoke() }
                        .padding(8.dp)
                ) {
                    BookCover(libro = resena.libro, modifier = Modifier.width(36.dp), cornerRadius = 4.dp, compacta = true)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(resena.libro.titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(resena.libro.autores, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            if (!resena.comentario.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(resena.comentario, fontSize = 14.sp, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (resena.esMia) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(textoMeGusta(resena.meGustas), fontSize = 12.sp, color = TextSecondary)
                } else {
                    IconButton(onClick = onMeGusta, modifier = Modifier.size(36.dp).testTag("me_gusta_${resena.id}")) {
                        Icon(
                            if (resena.meGustaMio) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (resena.meGustaMio) "Quitar me gusta" else "Me gusta",
                            tint = if (resena.meGustaMio) Color(0xFFE0245E) else TextSecondary
                        )
                    }
                    Text(textoMeGusta(resena.meGustas), fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

private fun textoMeGusta(n: Long) = when (n) {
    0L -> "Sin me gusta"
    1L -> "1 me gusta"
    else -> "$n me gusta"
}

/** Dialogo para escribir o editar una resena (1 a 5 estrellas + comentario opcional de 500 caracteres). */
@Composable
fun ResenaDialog(
    titulo: String,
    estrellasIniciales: Int = 0,
    comentarioInicial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var estrellas by rememberSaveable { mutableStateOf(estrellasIniciales) }
    var comentario by rememberSaveable { mutableStateOf(comentarioInicial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column {
                Text("Tu calificación", fontSize = 14.sp, color = TextSecondary)
                SelectorEstrellas(estrellas) { estrellas = it }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { if (it.length <= ReglasComunidad.MAX_COMENTARIO) comentario = it },
                    label = { Text("Comentario (opcional)") },
                    supportingText = { Text("${comentario.length}/${ReglasComunidad.MAX_COMENTARIO}") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("resena_comentario")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(estrellas, comentario) },
                enabled = ReglasComunidad.calificacionValida(estrellas),
                modifier = Modifier.testTag("resena_publicar")
            ) { Text("Publicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Dialogo para recomendar un libro a otro lector (RF07):
 * se busca al lector por nombre, se elige y se escribe un mensaje opcional.
 */
@Composable
fun RecomendarDialog(
    libro: Libro,
    buscar: suspend (String) -> NetworkResult<List<Lector>>,
    onDismiss: () -> Unit,
    onEnviar: (Lector, String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var mensaje by rememberSaveable { mutableStateOf("") }
    var elegido by remember { mutableStateOf<Lector?>(null) }
    var resultados by remember { mutableStateOf<List<Lector>>(emptyList()) }
    var buscando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            resultados = emptyList(); error = null; return@LaunchedEffect
        }
        delay(350) // espera a que el usuario deje de escribir
        buscando = true
        when (val r = buscar(query)) {
            is NetworkResult.Success -> { resultados = r.data; error = null }
            is NetworkResult.Error -> error = r.message
        }
        buscando = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recomendar libro") },
        text = {
            Column {
                Text("\"${libro.titulo}\"", fontSize = 14.sp, color = SagePrimary, fontWeight = FontWeight.Medium, maxLines = 2)
                Spacer(modifier = Modifier.height(12.dp))
                val sel = elegido
                if (sel == null) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("¿A quién? Busca por nombre") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("recomendar_buscar")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    when {
                        buscando -> CircularProgressIndicator(color = SagePrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        error != null -> Text(error!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        query.trim().length >= 2 && resultados.isEmpty() ->
                            Text("No encontramos lectores con ese nombre", fontSize = 12.sp, color = TextSecondary)
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(resultados, key = { it.id }) { l ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { elegido = l }
                                    .padding(vertical = 8.dp)
                            ) {
                                Avatar(l, 32.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(l.nombre, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SageContainer)
                            .padding(8.dp)
                    ) {
                        Avatar(sel, 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(sel.nombre, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SagePrimary)
                        TextButton(onClick = { elegido = null }) { Text("Cambiar") }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = { if (it.length <= ReglasComunidad.MAX_MENSAJE) mensaje = it },
                        label = { Text("Mensaje (opcional)") },
                        supportingText = { Text("${mensaje.length}/${ReglasComunidad.MAX_MENSAJE}") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("recomendar_mensaje")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { elegido?.let { onEnviar(it, mensaje) } },
                enabled = elegido != null,
                modifier = Modifier.testTag("recomendar_enviar")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enviar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
