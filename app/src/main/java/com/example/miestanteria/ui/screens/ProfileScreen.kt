package com.example.miestanteria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miestanteria.model.DayActivity
import com.example.miestanteria.model.SampleData
import com.example.miestanteria.ui.components.BottomNavBar
import com.example.miestanteria.ui.components.NavTab
import com.example.miestanteria.ui.theme.AppBackground
import com.example.miestanteria.ui.theme.MiEstanteriaTheme
import com.example.miestanteria.ui.theme.ProgressTrack
import com.example.miestanteria.ui.theme.SageContainer
import com.example.miestanteria.ui.theme.SagePrimary
import com.example.miestanteria.ui.theme.TextPrimary
import com.example.miestanteria.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    userName: String = "Lector",
    email: String = "",
    onLogout: () -> Unit = {},
    onTabSelected: (NavTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = NavTab.PERFIL,
                onTabSelected = onTabSelected
            )
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Profile Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SageContainer)
                        .border(2.dp, SagePrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = SagePrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = email,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = SagePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Grid 2x2
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.MenuBook,
                        label = "Libros leídos",
                        value = "47",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.AutoGraph,
                        label = "Páginas leídas",
                        value = "12,450",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Timer,
                        label = "Tiempo",
                        value = "320h",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Racha actual",
                        value = "15 días",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Metas y Retos
            Text(
                text = "Metas y Retos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GoalProgressItem(
                        label = "Meta anual",
                        current = "32",
                        target = "50 libros",
                        progress = 32f / 50f
                    )
                    GoalProgressItem(
                        label = "Reto de páginas",
                        current = "8200",
                        target = "10000 págs",
                        progress = 8200f / 10000f
                    )
                    GoalProgressItem(
                        label = "Racha de lectura",
                        current = "15",
                        target = "30 días",
                        progress = 15f / 30f
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actividad de lectura (Semanal)
            Text(
                text = "Actividad de lectura (Semanal)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bar Chart
                    WeeklyBarChart(activities = SampleData.weeklyActivity)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SageContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = SagePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun GoalProgressItem(
    label: String,
    current: String,
    target: String,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$current/$target",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = SagePrimary,
            trackColor = ProgressTrack,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun WeeklyBarChart(
    activities: List<DayActivity>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        activities.forEach { activity ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Vertical Bar
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight(fraction = activity.level.coerceIn(0.15f, 1.0f))
                        .clip(RoundedCornerShape(6.dp))
                        .background(SagePrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day Label
                Text(
                    text = activity.dayLabel,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MiEstanteriaTheme {
        ProfileScreen()
    }
}
