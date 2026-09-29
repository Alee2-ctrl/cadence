package com.cadence.app.ui.modes

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.data.CadenceRepository
import com.cadence.app.data.LockoutEntity
import com.cadence.app.data.ModeEntity
import com.cadence.app.lockout.LockoutService
import com.cadence.app.modes.ModesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppEntry(val label: String, val packageName: String)

class ModesViewModel(
    private val app: Context,
    private val repo: CadenceRepository,
) : ViewModel() {

    val modes: StateFlow<List<ModeEntity>> = repo.modes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lockout: StateFlow<LockoutEntity> = repo.lockout
        .map { it ?: LockoutEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LockoutEntity())

    val activeModeId = MutableStateFlow(ModesManager.activeModeId(app))
    val activeModeName = MutableStateFlow(ModesManager.activeModeName(app))

    val apps = MutableStateFlow<List<AppEntry>>(emptyList())
    private var appsRequested = false

    init {
        viewModelScope.launch {
            if (repo.allModes().isEmpty()) {
                repo.upsertMode(ModeEntity(name = "Do Not Disturb", colorKey = "honey", dnd = true, blocklist = false))
                repo.upsertMode(ModeEntity(name = "Bedtime", colorKey = "sky", dnd = true, blocklist = true))
                repo.upsertMode(ModeEntity(name = "Focus", colorKey = "leaf", dnd = true, blocklist = false))
            }
        }
    }

    private fun refreshActive() {
        activeModeId.value = ModesManager.activeModeId(app)
        activeModeName.value = ModesManager.activeModeName(app)
    }

    fun activate(mode: ModeEntity) {
        ModesManager.activate(app, mode)
        refreshActive()
    }

    fun deactivate() {
        ModesManager.deactivate(app)
        viewModelScope.launch {
            if (repo.lockoutOnce()?.enabled == true) {
                LockoutService.start(app)
            } else {
                LockoutService.stop(app)
            }
            refreshActive()
        }
    }

    fun saveMode(mode: ModeEntity) = viewModelScope.launch {
        repo.upsertMode(mode)
    }

    fun deleteMode(mode: ModeEntity) = viewModelScope.launch {
        if (ModesManager.activeModeId(app) == mode.id) {
            deactivate()
        }
        repo.deleteMode(mode.id)
    }

    fun saveLockout(value: LockoutEntity) = viewModelScope.launch {
        repo.upsertLockout(value)
        if (value.enabled || ModesManager.isBlocklistForced(app)) {
            LockoutService.start(app)
        } else {
            LockoutService.stop(app)
        }
    }

    fun loadApps() {
        if (appsRequested) return
        appsRequested = true
        viewModelScope.launch(Dispatchers.IO) {
            val pm = app.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            apps.value = resolved
                .map {
                    AppEntry(
                        label = it.loadLabel(pm).toString(),
                        packageName = it.activityInfo.packageName,
                    )
                }
                .distinctBy { it.packageName }
                .filter { it.packageName != app.packageName }
                .sortedBy { it.label.lowercase() }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = CadenceDatabase.get(context)
                    return ModesViewModel(
                        context.applicationContext,
                        CadenceRepository(db),
                    ) as T
                }
            }
    }
}
