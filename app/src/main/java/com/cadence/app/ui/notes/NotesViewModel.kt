package com.cadence.app.ui.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.data.CadenceRepository
import com.cadence.app.data.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repo: CadenceRepository) : ViewModel() {

    val query = MutableStateFlow("")

    val notes: StateFlow<List<NoteEntity>> =
        combine(repo.notes, query) { list, q ->
            if (q.isBlank()) list
            else list.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.text.contains(q, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(note: NoteEntity) = viewModelScope.launch {
        repo.upsertNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.deleteNote(id)
    }

    fun togglePin(note: NoteEntity) = viewModelScope.launch {
        repo.upsertNote(note.copy(pinned = !note.pinned, updatedAt = System.currentTimeMillis()))
    }

    fun toggleCheckLine(note: NoteEntity, index: Int) = viewModelScope.launch {
        val lines = note.text.split("\n").toMutableList()
        if (index !in lines.indices) return@launch
        val line = lines[index]
        lines[index] = when {
            line.startsWith("[x] ") -> "[ ] " + line.removePrefix("[x] ")
            line.startsWith("[ ] ") -> "[x] " + line.removePrefix("[ ] ")
            else -> "[x] $line"
        }
        repo.upsertNote(note.copy(text = lines.joinToString("\n"), updatedAt = System.currentTimeMillis()))
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = CadenceDatabase.get(context)
                    return NotesViewModel(CadenceRepository(db)) as T
                }
            }
    }
}
