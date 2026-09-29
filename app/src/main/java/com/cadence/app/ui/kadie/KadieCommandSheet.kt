package com.cadence.app.ui.kadie

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.components.CadenceSheet
import com.cadence.app.ui.components.SheetTextField
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Honey
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Paper
import kotlinx.coroutines.launch

private data class ChatMsg(val fromUser: Boolean, val text: String)

// T9.1: Kadie command sheet. Tap Kadie, type a command, it happens offline.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KadieCommandSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember {
        mutableStateListOf(
            ChatMsg(false, "Hey, I'm Kadie. Tell me what to do - or tap a chip below."),
        )
    }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    fun send(text: String) {
        val cmd = text.trim()
        if (cmd.isEmpty()) return
        messages.add(ChatMsg(true, cmd))
        input = ""
        scope.launch {
            val reply = KadieCommands.execute(context.applicationContext, cmd)
            messages.add(ChatMsg(false, reply))
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    CadenceSheet(
        onDismiss = onDismiss,
        label = "Kadie",
        title = "Ask Kadie.",
        subtitle = "Commands run on-device. Nothing leaves your phone.",
    ) {
        Column {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start,
                    ) {
                        if (!msg.fromUser) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Forest),
                                contentAlignment = Alignment.Center,
                            ) {
                                Kadie(mood = KadieMood.HAPPY, modifier = Modifier.size(20.dp), lineColor = Paper)
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 18.dp, topEnd = 18.dp,
                                        bottomStart = if (msg.fromUser) 18.dp else 4.dp,
                                        bottomEnd = if (msg.fromUser) 4.dp else 18.dp,
                                    ),
                                )
                                .background(if (msg.fromUser) Honey else CardWhite)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (msg.fromUser) Paper else Ink,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("add task", "note", "block", "start focus", "bedtime", "chill", "help").forEach { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Mist)
                            .clickable {
                                when (chip) {
                                    "add task" -> input = "add task "
                                    "note" -> input = "note "
                                    "block" -> input = "block "
                                    else -> send(chip)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            chip,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    SheetTextField(
                        value = input,
                        onValueChange = { input = it },
                        hint = "Ask Kadie...",
                    )
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Forest)
                        .clickable { send(input) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Paper,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Try: add task call mom - block tiktok - start focus - bedtime",
                style = MaterialTheme.typography.labelSmall,
                color = Faint,
            )
        }
    }
}
