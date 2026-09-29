package com.cadence.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.modes.ModesScreen
import com.cadence.app.ui.notes.NotesScreen
import com.cadence.app.ui.plan.PlanScreen
import com.cadence.app.ui.stats.StatsScreen
import com.cadence.app.ui.theme.Background
import com.cadence.app.ui.theme.CadenceIcons
import com.cadence.app.ui.theme.BarChart
import com.cadence.app.ui.theme.IconSize
import com.cadence.app.ui.theme.Outline
import com.cadence.app.ui.theme.Primary
import com.cadence.app.ui.theme.PrimaryContainer
import com.cadence.app.ui.theme.Sliders
import com.cadence.app.ui.theme.Surface
import com.cadence.app.ui.theme.TextSecondary
import com.cadence.app.ui.today.TodayScreen

// Root navigation: Today / Plan / Notes / Insights / Modes
private data class Tab(val label: String, val icon: ImageVector)

@Composable
fun CadenceRoot() {
    val tabs = listOf(
        Tab("Today", Icons.Outlined.CheckCircle),
        Tab("Plan", Icons.Outlined.DateRange),
        Tab("Notes", Icons.Outlined.Edit),
        Tab("Insights", CadenceIcons.BarChart),
        Tab("Modes", CadenceIcons.Sliders),
    )
    var selected by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            CadenceNavBar(
                tabs = tabs,
                selected = selected,
                onSelect = { selected = it },
            )
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

// Light navigation surface per the approved mockup: icon + small label,
// selected destination gets a pale-green container and forest tint.
@Composable
private fun CadenceNavBar(
    tabs: List<Tab>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface),
    ) {
        HorizontalDivider(thickness = 1.dp, color = Outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selected == index
                val tint = if (isSelected) Primary else TextSecondary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) PrimaryContainer else Surface)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(IconSize.Nav),
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = tint,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}
