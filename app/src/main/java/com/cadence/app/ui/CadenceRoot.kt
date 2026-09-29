package com.cadence.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.modes.ModesScreen
import com.cadence.app.ui.notes.NotesScreen
import com.cadence.app.ui.plan.PlanScreen
import com.cadence.app.ui.stats.StatsScreen
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Ink
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
            // Floating forest pill bar: paper circle behind the active tab.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Forest)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selected == index
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Paper else Color.Transparent)
                                .clickable { selected = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) Ink else Paper.copy(alpha = 0.65f),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
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
