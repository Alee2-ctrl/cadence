package com.cadence.app.ui.modes

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cadence.app.data.LockoutEntity
import com.cadence.app.data.ModeEntity
import com.cadence.app.modes.ModesManager
import com.cadence.app.ui.components.CadenceSheet
import com.cadence.app.ui.components.SheetLabel
import com.cadence.app.ui.components.SheetPillButton
import com.cadence.app.ui.components.SheetTextField
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
import com.cadence.app.ui.theme.Red
import com.cadence.app.ui.theme.SkyBlue
import com.cadence.app.ui.theme.TeaMist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

val modeColors: Map<String, Color> = mapOf(
    "leaf" to Leaf,
    "matcha" to Matcha,
    "honey" to Honey,
    "sky" to SkyBlue,
    "teamist" to TeaMist,
    "bamboo" to Bamboo,
)

@Composable
fun ModesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vm: ModesViewModel = viewModel(factory = ModesViewModel.factory(context))
    val modes by vm.modes.collectAsState()
    val lockout by vm.lockout.collectAsState()
    val activeId by vm.activeModeId.collectAsState()
    val activeName by vm.activeModeName.collectAsState()
    var editingMode by remember { mutableStateOf<ModeEntity?>(null) }
    var showModeEditor by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var permRefresh by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permRefresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    @Suppress("UNUSED_VARIABLE")
    val refreshSink = permRefresh // recompute permission checks on resume
    val hasPolicy = ModesManager.hasPolicyAccess(context)
    val hasUsage = ModesManager.hasUsageAccess(context)
    val hasOverlay = ModesManager.hasOverlayAccess(context)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Paper),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 110.dp),
    ) {
        item {
            Text(
                "Modes",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
            if (activeName != null) {
                Text(
                    "$activeName is active",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Leaf,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        if (!hasPolicy) {
            item {
                PermissionRow(
                    text = "Do Not Disturb access is needed for modes to silence the phone. Cadence should now appear in the system list.",
                    action = "Grant",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                        )
                    },
                )
            }
        }

        item { SectionTitle("Your modes") }

        modes.forEach { mode ->
            item {
                ModeCard(
                    mode = mode,
                    active = mode.id == activeId,
                    onClick = {
                        if (mode.id == activeId) vm.deactivate() else vm.activate(mode)
                    },
                    onEdit = { editingMode = mode; showModeEditor = true },
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        item {
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingMode = null; showModeEditor = true },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "+ Build your own mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Leaf,
                    )
                }
            }
        }

        item { SectionTitle("Lockout") }

        if (!hasUsage) {
            item {
                PermissionRow(
                    text = "Usage access lets Cadence see which app opens so it can block it.",
                    action = "Grant",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        )
                    },
                )
            }
        }
        if (!hasOverlay) {
            item {
                PermissionRow(
                    text = "Draw-over-apps permission is needed for the lockout screen.",
                    action = "Grant",
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}"),
                            ),
                        )
                    },
                )
            }
        }

        item {
            LockoutCard(
                lockout = lockout,
                enabled = hasUsage && hasOverlay,
                onSave = { vm.saveLockout(it) },
                onPickApps = { vm.loadApps(); showAppPicker = true },
            )
        }

        item {
            Text(
                "Kadie sleeps while Bedtime is active.",
                style = MaterialTheme.typography.bodySmall,
                color = Faint,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    }

    if (showModeEditor) {
        ModeEditorDialog(
            initial = editingMode,
            onDismiss = { showModeEditor = false },
            onSave = { vm.saveMode(it); showModeEditor = false },
            onDelete = { vm.deleteMode(it); showModeEditor = false },
        )
    }

    if (showAppPicker) {
        AppPickerDialog(
            vm = vm,
            selected = lockout.blockedPackages.split(",")
                .map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
            onDismiss = { showAppPicker = false },
            onSave = { pkgs ->
                vm.saveLockout(lockout.copy(blockedPackages = pkgs.joinToString(",")))
                showAppPicker = false
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Faint,
        modifier = Modifier.padding(top = 26.dp, bottom = 10.dp),
    )
}

@Composable
private fun PermissionRow(text: String, action: String, onClick: () -> Unit) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Bamboo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClick) {
                Text(action, color = Forest, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModeCard(
    mode: ModeEntity,
    active: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (active) Forest else CardWhite,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(modeColors[mode.colorKey] ?: Leaf),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    mode.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White else Ink,
                )
                Text(
                    buildList {
                        if (mode.dnd) add("dnd")
                        if (mode.blocklist) add("blocklist")
                    }.joinToString(" · ").ifBlank { "tap to activate" },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) Color.White.copy(alpha = 0.7f) else Faint,
                )
            }
            if (active) {
                Text(
                    "ON",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Matcha,
                )
            } else {
                TextButton(onClick = onEdit) {
                    Text("Edit", color = Faint)
                }
            }
        }
    }
}

@Composable
private fun LockoutCard(
    lockout: LockoutEntity,
    enabled: Boolean,
    onSave: (LockoutEntity) -> Unit,
    onPickApps: () -> Unit,
) {
    val context = LocalContext.current
    var reason by remember(lockout.reason) { mutableStateOf(lockout.reason) }
    val blockedCount = lockout.blockedPackages.split(",")
        .map { it.trim() }.filter { it.isNotEmpty() }.size

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "App lockout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    Text(
                        if (!enabled) "Grant the permissions above first"
                        else if (lockout.enabled) "Guarding your schedule"
                        else "Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = Faint,
                    )
                }
                Switch(
                    checked = lockout.enabled,
                    onCheckedChange = { onSave(lockout.copy(enabled = it)) },
                    enabled = enabled,
                    colors = SwitchDefaults.colors(checkedTrackColor = Leaf),
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("Blocked hours", style = MaterialTheme.typography.labelMedium, color = Faint)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = true,
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m -> onSave(lockout.copy(startMin = h * 60 + m)) },
                            lockout.startMin / 60,
                            lockout.startMin % 60,
                            true,
                        ).show()
                    },
                    label = {
                        Text(
                            "From " + String.format(
                                Locale.getDefault(), "%02d:%02d",
                                lockout.startMin / 60, lockout.startMin % 60,
                            ),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Mist,
                        selectedLabelColor = Ink,
                    ),
                )
                FilterChip(
                    selected = true,
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m -> onSave(lockout.copy(endMin = h * 60 + m)) },
                            lockout.endMin / 60,
                            lockout.endMin % 60,
                            true,
                        ).show()
                    },
                    label = {
                        Text(
                            "Until " + String.format(
                                Locale.getDefault(), "%02d:%02d",
                                lockout.endMin / 60, lockout.endMin % 60,
                            ),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Mist,
                        selectedLabelColor = Ink,
                    ),
                )
            }

            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Your reason (shown on the lock screen)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (reason != lockout.reason) {
                TextButton(
                    onClick = { onSave(lockout.copy(reason = reason.trim())) },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Save reason", color = Leaf, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onPickApps)
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    "Blocked apps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (blockedCount == 0) "Choose apps" else "$blockedCount blocked",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Leaf,
                )
            }
        }
    }
}

@Composable
private fun ModeEditorDialog(
    initial: ModeEntity?,
    onDismiss: () -> Unit,
    onSave: (ModeEntity) -> Unit,
    onDelete: (ModeEntity) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var colorKey by remember { mutableStateOf(initial?.colorKey ?: "leaf") }
    var dnd by remember { mutableStateOf(initial?.dnd ?: true) }
    var blocklist by remember { mutableStateOf(initial?.blocklist ?: false) }
    var confirmDelete by remember { mutableStateOf(false) }

    CadenceSheet(
        onDismiss = onDismiss,
        label = "A state of mind",
        title = if (initial == null) "New mode." else "Edit mode.",
        subtitle = "Silence the noise, guard your attention.",
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SheetTextField(
                value = name,
                onValueChange = { name = it },
                hint = "Name (e.g. Deep work)",
            )

            SheetLabel("Color")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                modeColors.keys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(modeColors[key] ?: Leaf)
                            .clickable { colorKey = key },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (colorKey == key) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            SheetLabel("While active")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Do Not Disturb",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = dnd,
                    onCheckedChange = { dnd = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Leaf),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Block apps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = blocklist,
                    onCheckedChange = { blocklist = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Leaf),
                )
            }

            Spacer(Modifier.height(18.dp))
            SheetPillButton(
                text = if (initial == null) "Create mode" else "Save changes",
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            ModeEntity(
                                id = initial?.id ?: 0,
                                name = name.trim(),
                                colorKey = colorKey,
                                dnd = dnd,
                                blocklist = blocklist,
                                createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                            ),
                        )
                    }
                },
            )
            if (initial != null) {
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        if (confirmDelete) "Tap again to delete" else "Delete",
                        color = if (confirmDelete) Red else Faint,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (confirmDelete) onDelete(initial) else confirmDelete = true
                            }
                            .padding(10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppPickerDialog(
    vm: ModesViewModel,
    selected: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit,
) {
    val apps by vm.apps.collectAsState()
    var checked by remember { mutableStateOf(selected) }
    var query by remember { mutableStateOf("") }

    CadenceSheet(
        onDismiss = onDismiss,
        label = "App lockout",
        title = "Blocked apps.",
        subtitle = "These apps get the Kadie guard during blocked hours.",
    ) {
        SheetTextField(
            value = query,
            onValueChange = { query = it },
            hint = "Search apps",
        )
        Spacer(Modifier.height(10.dp))
        val filtered = apps.filter {
            query.isBlank() || it.label.contains(query, ignoreCase = true)
        }
        if (apps.isEmpty()) {
            Text(
                "Loading apps...",
                style = MaterialTheme.typography.bodyMedium,
                color = Faint,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.height(340.dp)) {
                items(filtered.size) { index ->
                    val appEntry = filtered[index]
                    val isChecked = checked.contains(appEntry.packageName)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isChecked) Mist else CardWhite)
                            .clickable {
                                checked = if (isChecked) {
                                    checked - appEntry.packageName
                                } else {
                                    checked + appEntry.packageName
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        AppIcon(packageName = appEntry.packageName)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            appEntry.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isChecked) Leaf else Color.Transparent),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isChecked) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        SheetPillButton(
            text = "Block ${checked.size} app" + (if (checked.size == 1) "" else "s"),
            onClick = { onSave(checked) },
        )
    }
}

@Composable
private fun AppIcon(packageName: String) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationIcon(packageName)
                    .toBitmap(width = 96, height = 96)
                    .asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Paper),
        contentAlignment = Alignment.Center,
    ) {
        icon?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
