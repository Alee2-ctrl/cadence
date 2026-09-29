package com.cadence.app.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Clay
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Soft

@Composable
fun ItemEditorDialog(
    initial: EditorState?,
    onDismiss: () -> Unit,
    onSave: (EditorState) -> Unit,
    onDelete: (EditorState) -> Unit,
) {
    var type by remember { mutableStateOf(initial?.type ?: "TASK") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var daysMask by remember { mutableIntStateOf(initial?.daysMask ?: 127) }
    var timeOfDay by remember { mutableStateOf(initial?.timeOfDay ?: "ANYTIME") }
    var priority by remember { mutableStateOf(initial?.priority ?: "NORMAL") }
    var steps by remember { mutableStateOf(initial?.steps ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (initial == null) "Add to today" else "Edit",
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))

                ChipRow(
                    label = "Type",
                    options = listOf("TASK", "HABIT", "ROUTINE"),
                    selected = type,
                    onSelect = { type = it },
                )

                if (type == "TASK") {
                    ChipRow(
                        label = "Priority",
                        options = listOf("LOW", "NORMAL", "HIGH"),
                        selected = priority,
                        onSelect = { priority = it },
                    )
                } else {
                    ChipRow(
                        label = "Frequency",
                        options = listOf("DAILY", "WEEKDAYS", "WEEKENDS"),
                        selected = when (daysMask) {
                            31 -> "WEEKDAYS"
                            96 -> "WEEKENDS"
                            else -> "DAILY"
                        },
                        onSelect = {
                            daysMask = when (it) {
                                "WEEKDAYS" -> 31
                                "WEEKENDS" -> 96
                                else -> 127
                            }
                        },
                    )
                    ChipRow(
                        label = "Time of day",
                        options = listOf("MORNING", "AFTERNOON", "EVENING", "ANYTIME"),
                        selected = timeOfDay,
                        onSelect = { timeOfDay = it },
                    )
                }

                if (type == "ROUTINE") {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = steps,
                        onValueChange = { steps = it },
                        label = { Text("Steps (one per line)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            EditorState(
                                id = initial?.id ?: 0,
                                type = type,
                                name = name.trim(),
                                daysMask = daysMask,
                                timeOfDay = timeOfDay,
                                priority = priority,
                                steps = steps,
                            ),
                        )
                    }
                },
            ) { Text("Save", color = Clay, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            Row {
                if (initial != null) {
                    TextButton(onClick = { onDelete(initial) }) { Text("Delete", color = Faint) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel", color = Faint) }
            }
        },
    )
}

@Composable
private fun ChipRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Faint)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = {
                        Text(
                            option.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardWhite,
                        labelColor = Faint,
                        selectedContainerColor = Soft,
                        selectedLabelColor = Ink,
                    ),
                )
            }
        }
    }
}
