package com.cadence.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Matcha
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Red
import com.cadence.app.ui.theme.TeaMist

// T9.0 shared design system: every dialog in the app is a paper sheet with a
// forest header card, pill chips and a big rounded primary button.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadenceSheet(
    onDismiss: () -> Unit,
    label: String,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Paper,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TeaMist) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 30.dp),
        ) {
            SheetHeader(label = label, title = title, subtitle = subtitle)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun SheetHeader(label: String, title: String, subtitle: String?) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Forest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Matcha,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Paper,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Paper.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
fun SheetLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Faint,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
    )
}

@Composable
fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, color = Faint) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CardWhite,
            unfocusedContainerColor = CardWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            cursorColor = Forest,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SheetChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    label: (String) -> String = { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Forest else CardWhite)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(
                    label(option),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Paper else Ink,
                )
            }
        }
    }
}

@Composable
fun SheetPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Forest,
    content: Color = Paper,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = content,
        )
    }
}

// Long-press action sheet: Edit / Delete with two-step delete confirm.
@Composable
fun ItemActionsSheet(
    name: String,
    kind: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    CadenceSheet(
        onDismiss = onDismiss,
        label = kind,
        title = name,
        subtitle = "What would you like to do?",
    ) {
        SheetActionRow(
            icon = Icons.Outlined.Edit,
            text = "Edit",
            tint = Ink,
            onClick = onEdit,
        )
        Spacer(Modifier.height(8.dp))
        SheetActionRow(
            icon = Icons.Outlined.Delete,
            text = if (confirmDelete) "Tap again to delete" else "Delete",
            tint = Red,
            onClick = {
                if (confirmDelete) onDelete() else confirmDelete = true
            },
        )
    }
}

@Composable
fun SheetActionRow(
    icon: ImageVector,
    text: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Paper),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = tint,
        )
    }
}

// Restyled dropdown: rounded paper card, icon + label rows (menu inspo).
data class MenuEntry(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
fun CadenceMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    entries: List<MenuEntry>,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(20.dp),
        offset = DpOffset(x = (-8).dp, y = 4.dp),
    ) {
        entries.forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss(); entry.onClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Paper),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        entry.icon,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    entry.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                )
                Spacer(Modifier.width(28.dp))
            }
        }
    }
}
