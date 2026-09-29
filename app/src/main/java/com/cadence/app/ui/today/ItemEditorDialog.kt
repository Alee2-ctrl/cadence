package com.cadence.app.ui.today

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.components.CadenceSheet
import com.cadence.app.ui.components.SheetChipRow
import com.cadence.app.ui.components.SheetLabel
import com.cadence.app.ui.components.SheetPillButton
import com.cadence.app.ui.components.SheetTextField
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Red
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
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val headerLabel = when (type) {
        "HABIT" -> "One step, often"
        "ROUTINE" -> "Your daily rhythm"
        else -> "A clear next step"
    }

    CadenceSheet(
        onDismiss = onDismiss,
        label = headerLabel,
        title = if (initial == null) "Make it happen." else "Tune it up.",
        subtitle = "Give your plan a name and a place in your day.",
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SheetTextField(
                value = name,
                onValueChange = { name = it },
                hint = "What needs doing?",
            )

            SheetLabel("Type")
            SheetChipRow(
                options = listOf("TASK", "HABIT", "ROUTINE"),
                selected = type,
                onSelect = { type = it },
            )

            if (type == "TASK") {
                SheetLabel("Priority")
                SheetChipRow(
                    options = listOf("LOW", "NORMAL", "HIGH"),
                    selected = priority,
                    onSelect = { priority = it },
                )
                SheetLabel("Date")
                SheetChipRow(
                    options = listOf("DATE"),
                    selected = "DATE",
                    onSelect = {
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
                    label = {
                        LocalDate.ofEpochDay(dateEpoch).format(
                            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()),
                        )
                    },
                )
            } else {
                SheetLabel("Frequency")
                SheetChipRow(
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
                SheetLabel("Time of day")
                SheetChipRow(
                    options = listOf("MORNING", "AFTERNOON", "EVENING", "ANYTIME"),
                    selected = timeOfDay,
                    onSelect = { timeOfDay = it },
                )
            }

            SheetLabel("Reminder")
            SheetChipRow(
                options = listOf("OFF", "TIME"),
                selected = if (reminderMin < 0) "OFF" else "TIME",
                onSelect = { opt ->
                    if (opt == "OFF") {
                        reminderMin = -1
                    } else {
                        val startMin = if (reminderMin >= 0) reminderMin else 8 * 60
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> reminderMin = hour * 60 + minute },
                            startMin / 60,
                            startMin % 60,
                            true,
                        ).show()
                    }
                },
                label = {
                    if (it == "OFF") "Off"
                    else if (reminderMin >= 0)
                        String.format(Locale.getDefault(), "%02d:%02d", reminderMin / 60, reminderMin % 60)
                    else "Set time"
                },
            )

            if (type == "ROUTINE") {
                SheetLabel("Steps")
                SheetTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    hint = "One step per line",
                    singleLine = false,
                    minLines = 3,
                )
            }

            Spacer(Modifier.height(20.dp))
            SheetPillButton(
                text = if (initial == null) "Add to your day" else "Save changes",
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
            )
            if (initial != null) {
                TextButton(
                    onClick = {
                        if (confirmDelete) onDelete(initial) else confirmDelete = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        if (confirmDelete) "Tap again to delete" else "Delete",
                        color = if (confirmDelete) Red else Faint,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
