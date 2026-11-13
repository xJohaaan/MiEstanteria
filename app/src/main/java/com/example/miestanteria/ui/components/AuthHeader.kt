package com.example.miestanteria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/** Logo + titulo de la app, comun a Splash, Login y Registro. */
@Composable
fun AuthHeader(subtitle: String, modifier: Modifier = Modifier, logoSize: Int = 80) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(logoSize.dp)
                .clip(RoundedCornerShape((logoSize / 4).dp))
                .border(2.dp, SagePrimary, RoundedCornerShape((logoSize / 4).dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = "Logo de Mi Estanteria",
                tint = SagePrimary,
                modifier = Modifier.size((logoSize * 0.52f).dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Mi Estantería",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtitle, fontSize = 14.sp, color = TextSecondary)
    }
}
