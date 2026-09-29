package com.cadence.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.plan.PlanScreen
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.today.TodayScreen

private data class Tab(val label: String, val icon: ImageVector, val placeholder: String?)

@Composable
fun CadenceRoot() {
    val tabs = listOf(
        Tab("Today", Icons.Outlined.CheckCircle, null),
        Tab("Plan", Icons.Outlined.DateRange, null),
        Tab("Notes", Icons.Outlined.Edit, "Notes arrive in T6"),
        Tab("Stats", Icons.Outlined.Star, "Insights arrive in T5"),
    )
    var selected by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            NavigationBar(containerColor = Paper, tonalElevation = 0.dp) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Leaf,
                            selectedTextColor = Ink,
                            unselectedIconColor = Faint,
                            unselectedTextColor = Faint,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        val tab = tabs[selected]
        when {
            selected == 0 -> TodayScreen(modifier = Modifier.padding(padding))
            selected == 1 -> PlanScreen(modifier = Modifier.padding(padding))
            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.placeholder ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Faint,
                )
            }
        }
    }
}
