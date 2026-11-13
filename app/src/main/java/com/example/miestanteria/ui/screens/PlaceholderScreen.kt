package com.example.miestanteria.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miestanteria.ui.components.BottomNavBar
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextSecondary

/** Pestana que todavia no esta implementada (se reemplaza en los siguientes dias del plan). */
@Composable
fun PlaceholderScreen(tab: NavTab, message: String, onTabSelected: (NavTab) -> Unit) {
    Scaffold(
        bottomBar = { BottomNavBar(selectedTab = tab, onTabSelected = onTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(tab.selectedIcon, contentDescription = null, tint = SagePrimary, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(tab.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}
