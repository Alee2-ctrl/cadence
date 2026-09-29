package com.cadence.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cadence.app.data.NoteEntity
import com.cadence.app.ui.components.cardShape
import com.cadence.app.ui.theme.Bamboo
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Honey
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Matcha
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Pistachio
import com.cadence.app.ui.theme.SkyBlue
import com.cadence.app.ui.theme.TeaMist

val noteColors: Map<String, Color> = mapOf(
    "paper" to CardWhite,
    "pistachio" to Pistachio,
    "mist" to Mist,
    "bamboo" to Bamboo,
    "teamist" to TeaMist,
    "matcha" to Matcha,
    "sky" to SkyBlue,
    "honey" to Honey,
)

@Composable
fun NotesScreen(modifier: Modifier = Modifier) {
    val vm: NotesViewModel = viewModel(factory = NotesViewModel.factory(LocalContext.current))
    val notes by vm.notes.collectAsState()
    val query by vm.query.collectAsState()
    var editing by remember { mutableStateOf<NoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Paper,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                containerColor = Forest,
                contentColor = Color.White,
                shape = CircleShape,
                text = { Text("New note", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Text(
                "Notes",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.padding(top = 14.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                placeholder = { Text("Search notes", color = Faint) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            if (notes.isEmpty()) {
                Text(
                    if (query.isBlank()) "No notes yet. Tap + to write one."
                    else "Nothing matches that search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint,
                    modifier = Modifier.padding(top = 28.dp),
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    contentPadding = PaddingValues(bottom = 110.dp),
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { editing = note; showEditor = true },
                            onToggleLine = { idx -> vm.toggleCheckLine(note, idx) },
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        NoteEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { vm.save(it); showEditor = false },
            onDelete = { vm.delete(it.id); showEditor = false },
            onTogglePin = { vm.togglePin(it) },
        )
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onToggleLine: (Int) -> Unit,
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = noteColors[note.colorKey] ?: CardWhite,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (note.pinned) {
                Text(
                    "Pinned",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Leaf,
                )
                Spacer(Modifier.height(4.dp))
            }
            if (note.title.isNotBlank()) {
                Text(
                    note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
            }
            if (note.isChecklist) {
                note.text.split("\n").filter { it.isNotBlank() }.take(5)
                    .forEachIndexed { index, line ->
                        val checked = line.startsWith("[x] ")
                        val text = line.removePrefix("[x] ").removePrefix("[ ] ")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleLine(index) }
                                .padding(vertical = 2.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (checked) Leaf else CardWhite),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (checked) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (checked) Faint else Ink,
                                textDecoration = if (checked) TextDecoration.LineThrough else null,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
            } else if (note.text.isNotBlank()) {
                Text(
                    note.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.8f),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    initial: NoteEntity?,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var text by remember { mutableStateOf(initial?.text ?: "") }
    var colorKey by remember { mutableStateOf(initial?.colorKey ?: "paper") }
    var isChecklist by remember { mutableStateOf(initial?.isChecklist ?: false) }
    var pinned by remember { mutableStateOf(initial?.pinned ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (initial == null) "New note" else "Edit note",
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(if (isChecklist) "One item per line" else "Note") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    noteColors.keys.take(4).forEach { key ->
                        ColorDot(key, colorKey == key) { colorKey = key }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    noteColors.keys.drop(4).forEach { key ->
                        ColorDot(key, colorKey == key) { colorKey = key }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Checklist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = isChecklist,
                        onCheckedChange = { isChecklist = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Leaf),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Pinned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = pinned,
                        onCheckedChange = { pinned = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Leaf),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() || text.isNotBlank()) {
                        val cleanedText = if (isChecklist) {
                            text.split("\n").joinToString("\n") { line ->
                                when {
                                    line.startsWith("[x] ") || line.startsWith("[ ] ") -> line
                                    line.isBlank() -> line
                                    else -> "[ ] $line"
                                }
                            }
                        } else text
                        onSave(
                            NoteEntity(
                                id = initial?.id ?: 0,
                                title = title.trim(),
                                text = cleanedText,
                                colorKey = colorKey,
                                isChecklist = isChecklist,
                                pinned = pinned,
                                updatedAt = initial?.updatedAt ?: System.currentTimeMillis(),
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
private fun ColorDot(key: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(noteColors[key] ?: CardWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
