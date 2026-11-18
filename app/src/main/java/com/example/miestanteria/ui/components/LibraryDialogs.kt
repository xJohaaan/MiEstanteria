package com.example.miestanteria.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.ui.theme.TextSecondary

/** Reglas del campo "pagina actual"; devuelve el mensaje de error o null si es valida. */
object PaginaValidator {
    fun validar(texto: String, total: Int?): String? {
        if (texto.isBlank()) return null
        val n = texto.toIntOrNull() ?: return "Escribe solo números"
        return when {
            n < 0 -> "La página no puede ser negativa"
            total != null && total > 0 && n > total -> "El libro tiene $total páginas"
            else -> null
        }
    }
}

/** Selector de estado de lectura en tres botones segmentados. */
@Composable
fun EstadoSelector(estado: EstadoLectura, onChange: (EstadoLectura) -> Unit, enabled: Boolean = true) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        EstadoLectura.entries.forEachIndexed { i, e ->
            SegmentedButton(
                selected = estado == e,
                onClick = { onChange(e) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = i, count = EstadoLectura.entries.size),
                label = { Text(e.etiqueta, fontSize = 13.sp) }
            )
        }
    }
}

/** Dialogo para agregar un libro a la biblioteca eligiendo estado y, si lo esta leyendo, la pagina. */
@Composable
fun AgregarLibroDialog(
    totalPaginas: Int?,
    onDismiss: () -> Unit,
    onConfirm: (EstadoLectura, Int?) -> Unit
) {
    var estado by rememberSaveable { mutableStateOf(EstadoLectura.POR_LEER) }
    var pagina by rememberSaveable { mutableStateOf("") }
    val error = if (estado == EstadoLectura.LEYENDO) PaginaValidator.validar(pagina, totalPaginas) else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar a mi biblioteca") },
        text = {
            Column {
                Text("¿En qué estado está este libro?", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                EstadoSelector(estado = estado, onChange = { estado = it })
                if (estado == EstadoLectura.LEYENDO) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PaginaField(pagina, { pagina = it }, totalPaginas, error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(estado, if (estado == EstadoLectura.LEYENDO) pagina.toIntOrNull() ?: 0 else null) },
                enabled = error == null,
                modifier = Modifier.testTag("dialog_agregar_confirmar")
            ) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Dialogo para actualizar la pagina en la que va el lector. */
@Composable
fun PaginaDialog(
    paginaActual: Int,
    totalPaginas: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var pagina by rememberSaveable { mutableStateOf(paginaActual.toString()) }
    val error = PaginaValidator.validar(pagina, totalPaginas)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿En qué página vas?") },
        text = { PaginaField(pagina, { pagina = it }, totalPaginas, error) },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pagina.toIntOrNull() ?: 0) },
                enabled = error == null && pagina.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PaginaField(valor: String, onChange: (String) -> Unit, total: Int?, error: String?) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo -> if (nuevo.length <= 5) onChange(nuevo.filter { it.isDigit() }) },
        label = { Text("Página actual") },
        supportingText = {
            Text(error ?: total?.let { "De $it páginas" } ?: "", color = if (error != null) MaterialTheme.colorScheme.error else TextSecondary)
        },
        isError = error != null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Dialogo de confirmacion generico. */
@Composable
fun ConfirmDialog(title: String, message: String, confirmLabel: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
