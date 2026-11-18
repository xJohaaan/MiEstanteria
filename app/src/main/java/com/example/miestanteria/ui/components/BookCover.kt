package com.example.miestanteria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.miestanteria.model.Libro
import kotlin.math.abs

/** Paletas para las portadas sin imagen; cada libro recibe siempre la misma segun su titulo. */
private val PALETAS = listOf(
    listOf(Color(0xFF2E6F40), Color(0xFF1B4326)),
    listOf(Color(0xFFD97724), Color(0xFF8C3E00)),
    listOf(Color(0xFF1D3557), Color(0xFF457B9D)),
    listOf(Color(0xFF1E5B94), Color(0xFF113253)),
    listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2)),
    listOf(Color(0xFF1A237E), Color(0xFF0D47A1)),
    listOf(Color(0xFFF9A825), Color(0xFFE65100)),
    listOf(Color(0xFF4C7B6B), Color(0xFF2B4A40))
)

/**
 * Portada de un libro. Carga la imagen con Coil (carga perezosa y cache en disco, RNF02).
 * Mientras carga, o si el libro no tiene portada, muestra un diseno con el titulo y el autor.
 */
@Composable
fun BookCover(
    titulo: String,
    autores: String,
    portadaUrl: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    compacta: Boolean = false
) {
    val paleta = PALETAS[abs(titulo.hashCode()) % PALETAS.size]
    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.verticalGradient(paleta)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compacta) 4.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .size(if (compacta) 16.dp else 24.dp)
                    .padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = titulo.uppercase(),
                color = Color.White,
                fontSize = if (compacta) 8.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (compacta) 10.sp else 13.sp
            )
            if (!compacta) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = autores,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        if (!portadaUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(portadaUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Portada de $titulo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun BookCover(libro: Libro, modifier: Modifier = Modifier, cornerRadius: Dp = 8.dp, compacta: Boolean = false) =
    BookCover(libro.titulo, libro.autores, libro.portadaUrl, modifier, cornerRadius, compacta)
