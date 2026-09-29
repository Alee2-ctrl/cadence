package com.cadence.app.ui.today

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Paper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ItemEditorDialog(
    initial: EditorState?,
    defaultDate: Long,
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
    var reminderMin by remember { mutableIntStateOf(initial?.reminderMin ?: -1) }
    var dateEpoch by remember {
        mutableLongStateOf(
            if (initial != null && initial.date > 0) initial.date else defaultDate,
        )
    }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (initial == null) "Add item" else "Edit",
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
                    DateRow(
                        dateEpoch = dateEpoch,
                        onPick = {
                            val d = LocalDate.ofEpochDay(dateEpoch)
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    dateEpoch = LocalDate.of(year, month + 1, day).toEpochDay()
                                },
                                d.year,
                                d.monthValue - 1,
                                d.dayOfMonth,
                            ).show()
                        },
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

                ReminderRow(
                    reminderMin = reminderMin,
                    onOff = { reminderMin = -1 },
                    onPick = {
                        val startMin = if (reminderMin >= 0) reminderMin else 8 * 60
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> reminderMin = hour * 60 + minute },
                            startMin / 60,
                            startMin % 60,
                            true,
                        ).show()
                    },
                )

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
                                reminderMin = reminderMin,
                                date = dateEpoch,
                                done = initial?.done ?: false,
                            ),
                        )
                    }
                },
            ) { Text("Save", color = Leaf, fontWeight = FontWeight.Bold) }
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
private fun DateRow(dateEpoch: Long, onPick: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Date", style = MaterialTheme.typography.labelMedium, color = Faint)
        Spacer(Modifier.height(6.dp))
        FilterChip(
            selected = true,
            onClick = onPick,
            label = {
                Text(
                    LocalDate.ofEpochDay(dateEpoch).format(
                        DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = CardWhite,
                labelColor = Faint,
                selectedContainerColor = Mist,
                selectedLabelColor = Ink,
            ),
        )
    }
}

@Composable
private fun ReminderRow(reminderMin: Int, onOff: () -> Unit, onPick: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Reminder", style = MaterialTheme.typography.labelMedium, color = Faint)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = reminderMin < 0,
                onClick = onOff,
                label = { Text("Off", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardWhite,
                    labelColor = Faint,
                    selectedContainerColor = Mist,
                    selectedLabelColor = Ink,
                ),
            )
            FilterChip(
                selected = reminderMin >= 0,
                onClick = onPick,
                label = {
                    Text(
                        if (reminderMin >= 0)
                            String.format(Locale.getDefault(), "%02d:%02d", reminderMin / 60, reminderMin % 60)
                        else "Set time",
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardWhite,
                    labelColor = Faint,
                    selectedContainerColor = Mist,
                    selectedLabelColor = Leaf,
                ),
            )
        }
    }
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
                        selectedContainerColor = Mist,
                        selectedLabelColor = Ink,
                    ),
                )
            }
        }
    }
}
