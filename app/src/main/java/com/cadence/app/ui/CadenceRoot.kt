package com.cadence.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.modes.ModesScreen
import com.cadence.app.ui.notes.NotesScreen
import com.cadence.app.ui.plan.PlanScreen
import com.cadence.app.ui.stats.StatsScreen
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.today.TodayScreen

// Root navigation: Today / Plan / Notes / Stats / Modes
private data class Tab(val label: String, val icon: ImageVector)

@Composable
fun CadenceRoot() {
    val tabs = listOf(
        Tab("Today", Icons.Outlined.CheckCircle),
        Tab("Plan", Icons.Outlined.DateRange),
        Tab("Notes", Icons.Outlined.Edit),
        Tab("Stats", Icons.Outlined.Star),
        Tab("Modes", Icons.Outlined.Lock),
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
        when (selected) {
            0 -> TodayScreen(modifier = Modifier.padding(padding))
            1 -> PlanScreen(modifier = Modifier.padding(padding))
            2 -> NotesScreen(modifier = Modifier.padding(padding))
            3 -> StatsScreen(modifier = Modifier.padding(padding))
            4 -> ModesScreen(modifier = Modifier.padding(padding))
        }
    }
}
