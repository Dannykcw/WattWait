package com.example.wattwait.ui.screen.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wattwait.domain.model.AppMapping
import com.example.wattwait.domain.model.ApplianceType
import com.example.wattwait.domain.model.EfficiencyCategory
import com.example.wattwait.domain.repository.IAppMappingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appMappingRepository: IAppMappingRepository
) : ViewModel() {

    data class UiState(
        val existingMappings: List<AppMapping> = emptyList(),
        val availableApps: List<InstalledAppInfo> = emptyList(),
        val selectedApp: InstalledAppInfo? = null,
        val showApplianceDialog: Boolean = false,
        val editingMapping: AppMapping? = null,
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            appMappingRepository.getAllMappings().collect { mappings ->
                val mappedPackages = mappings.map { it.packageName }.toSet()
                val installedApps = getInstalledSmartHomeApps()
                    .filter { it.packageName !in mappedPackages }

                _uiState.update { state ->
                    state.copy(
                        existingMappings = mappings,
                        availableApps = installedApps,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun getInstalledSmartHomeApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                // Filter to show user-installed apps (smart home apps are typically user-installed)
                if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    InstalledAppInfo(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        icon = try {
                            pm.getApplicationIcon(appInfo.packageName)
                        } catch (e: Exception) {
                            null
                        }
                    )
                } else null
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun selectApp(appInfo: InstalledAppInfo) {
        _uiState.update { state ->
            state.copy(
                selectedApp = appInfo,
                showApplianceDialog = true,
                editingMapping = null
            )
        }
    }

    fun editMapping(mapping: AppMapping) {
        val appInfo = InstalledAppInfo(
            packageName = mapping.packageName,
            appName = mapping.appName,
            icon = try {
                context.packageManager.getApplicationIcon(mapping.packageName)
            } catch (e: Exception) {
                null
            }
        )
        _uiState.update { state ->
            state.copy(
                selectedApp = appInfo,
                showApplianceDialog = true,
                editingMapping = mapping
            )
        }
    }

    fun dismissDialog() {
        _uiState.update { state ->
            state.copy(
                selectedApp = null,
                showApplianceDialog = false,
                editingMapping = null
            )
        }
    }

    fun createMapping(applianceType: ApplianceType, efficiency: EfficiencyCategory) {
        val selectedApp = _uiState.value.selectedApp ?: return
        val editingMapping = _uiState.value.editingMapping

        viewModelScope.launch {
            if (editingMapping != null) {
                appMappingRepository.updateMapping(
                    editingMapping.copy(
                        applianceType = applianceType,
                        efficiencyCategory = efficiency
                    )
                )
            } else {
                appMappingRepository.addMapping(
                    AppMapping(
                        packageName = selectedApp.packageName,
                        appName = selectedApp.appName,
                        applianceType = applianceType,
                        efficiencyCategory = efficiency,
                        isEnabled = true
                    )
                )
            }
            dismissDialog()
        }
    }

    fun toggleMapping(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            appMappingRepository.toggleMapping(id, enabled)
        }
    }

    fun deleteMapping(id: Long) {
        viewModelScope.launch {
            appMappingRepository.deleteMapping(id)
        }
    }
}
