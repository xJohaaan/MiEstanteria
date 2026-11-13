package com.example.miestanteria.model

import androidx.compose.ui.graphics.Color

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val category: String, // "Ficción", "No ficción", "Favoritos"
    val progress: Float = 0f, // 0.0 to 1.0
    val coverGradient: List<Color>,
    val coverIconResName: String = "book"
)

object SampleData {
    val currentlyReading = listOf(
        Book(
            id = "1",
            title = "El bosque infinito",
            author = "Alma t. de la cruz",
            category = "Ficción",
            progress = 0.64f,
            coverGradient = listOf(Color(0xFF2E6F40), Color(0xFF1B4326))
        ),
        Book(
            id = "2",
            title = "Hábitos Atómicos",
            author = "James Clear",
            category = "No ficción",
            progress = 0.35f,
            coverGradient = listOf(Color(0xFFD97724), Color(0xFF8C3E00))
        )
    )

    val libraryBooks = listOf(
        Book(
            id = "3",
            title = "El Alquimista",
            author = "Paulo Coelho",
            category = "Ficción",
            coverGradient = listOf(Color(0xFF1D3557), Color(0xFF457B9D))
        ),
        Book(
            id = "4",
            title = "Pensar Rápido",
            author = "Daniel Kahneman",
            category = "No ficción",
            coverGradient = listOf(Color(0xFF1E5B94), Color(0xFF113253))
        ),
        Book(
            id = "5",
            title = "Cien Años de Soledad",
            author = "G. García Márquez",
            category = "Ficción",
            coverGradient = listOf(Color(0xFF386641), Color(0xFF153B18))
        ),
        Book(
            id = "6",
            title = "La hipótesis de...",
            author = "Ali Hazelwood",
            category = "Favoritos",
            coverGradient = listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2))
        ),
        Book(
            id = "7",
            title = "Breves Respuestas...",
            author = "Stephen Hawking",
            category = "No ficción",
            coverGradient = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
        ),
        Book(
            id = "8",
            title = "El Principito",
            author = "A. de Saint-Exupéry",
            category = "Favoritos",
            coverGradient = listOf(Color(0xFFFBC02D), Color(0xFFF57F17))
        )
    )

    val weeklyActivity = listOf(
        DayActivity("Lun", 0.45f),
        DayActivity("Mar", 0.55f),
        DayActivity("Mié", 0.35f),
        DayActivity("Jue", 0.80f),
        DayActivity("Vie", 0.50f),
        DayActivity("Sáb", 0.95f),
        DayActivity("Dom", 0.70f)
    )
}

data class DayActivity(
    val dayLabel: String,
    val level: Float // 0.0 to 1.0
)
