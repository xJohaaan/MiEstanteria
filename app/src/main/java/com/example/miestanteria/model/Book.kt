package com.example.miestanteria.model

/** Datos de ejemplo que aun usa la pantalla de Perfil (se reemplazan por estadisticas reales en el Dia 4). */
object SampleData {
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
