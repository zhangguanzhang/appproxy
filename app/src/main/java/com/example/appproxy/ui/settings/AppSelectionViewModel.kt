package com.example.appproxy.ui.settings

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproxy.data.AppSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class SettingsNav(
    val onNavigateToAbout: () -> Unit,
    val onNavigateToAppSelection: () -> Unit,
    val onNavigateBackHome: () -> Unit,
)

class AppSelectionViewModel(
    private val packageManager: PackageManager,
    private val repository: AppSelectionRepository
) : ViewModel() {
    data class AppInfo(
        val name: String,
        val packageName: String,
        val icon: Drawable,
        val isSystemApp: Boolean
    )

    enum class AppFilterType {
        ALL,
        USER,
        SYSTEM
    }

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _filteredApps.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _filterType = MutableStateFlow(AppFilterType.ALL)
    val filterType: StateFlow<AppFilterType> = _filterType.asStateFlow()

    init {
        loadApps()
        loadSelectedApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val appList = installedApps.map { app ->
                AppInfo(
                    name = packageManager.getApplicationLabel(app).toString(),
                    packageName = app.packageName,
                    icon = packageManager.getApplicationIcon(app),
                    isSystemApp = app.flags and ApplicationInfo.FLAG_SYSTEM != 0
                )
            }.sortedBy { it.name }
            _allApps.value = appList
            _filteredApps.value = appList
        }
    }

    private fun loadSelectedApps() {
        viewModelScope.launch {
            _selectedApps.value = repository.getSelectedApps().toSet()
        }
    }

    fun toggleAppSelection(packageName: String) {
        viewModelScope.launch {
            val newSelectedApps = _selectedApps.value.toMutableSet()
            if (newSelectedApps.contains(packageName)) {
                newSelectedApps.remove(packageName)
            } else {
                newSelectedApps.add(packageName)
            }
            _selectedApps.value = newSelectedApps
            repository.saveSelectedApps(newSelectedApps.toList())
        }
    }

    fun setFilterType(type: AppFilterType) {
        _filterType.value = type
        viewModelScope.launch {
            _filteredApps.value = when (type) {
                AppFilterType.ALL -> _allApps.value
                AppFilterType.USER -> _allApps.value.filter { !it.isSystemApp }
                AppFilterType.SYSTEM -> _allApps.value.filter { it.isSystemApp }
            }
        }
    }
} 