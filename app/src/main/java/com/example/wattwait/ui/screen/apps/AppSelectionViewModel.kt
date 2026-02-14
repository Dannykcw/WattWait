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
    val icon: Drawable?,
    val suggestedAppliance: ApplianceType? = null
)

// Common smart home app package names with suggested appliance types
object SmartHomeAppSuggestions {
    val knownApps = mapOf(
        // General smart home hubs
        "com.google.android.apps.chromecast.app" to ApplianceType.GENERIC,
        "com.samsung.android.oneconnect" to ApplianceType.GENERIC, // SmartThings
        "com.amazon.dee.app" to ApplianceType.GENERIC, // Alexa
        "com.apple.Home" to ApplianceType.GENERIC,

        // Thermostats
        "com.nest.android" to ApplianceType.THERMOSTAT,
        "com.honeywell.mobile.android.totalconnect" to ApplianceType.THERMOSTAT,
        "com.ecobee.athemis" to ApplianceType.THERMOSTAT,
        "com.emerson.sensi" to ApplianceType.THERMOSTAT,

        // EV Chargers
        "com.tesla.tesla" to ApplianceType.EV_CHARGER,
        "com.chargepoint.mobile" to ApplianceType.EV_CHARGER,
        "com.flo.home" to ApplianceType.EV_CHARGER,
        "com.wallbox.myWallbox" to ApplianceType.EV_CHARGER,

        // AC/Climate
        "com.midea.aircondition.obm" to ApplianceType.AIR_CONDITIONER,
        "com.lg.lgthinq" to ApplianceType.AIR_CONDITIONER, // Also handles washer/dryer
        "com.daikin.daikinunifiedhvac" to ApplianceType.AIR_CONDITIONER,

        // Washer/Dryer
        "com.whirlpool.genesis" to ApplianceType.WASHER,
        "com.ge.kitchen" to ApplianceType.WASHER,
        "com.samsung.android.vdh" to ApplianceType.WASHER, // SmartThings appliances

        // Lighting
        "com.philips.lighting.hue2" to ApplianceType.LIGHTING,
        "com.lifx.lifx" to ApplianceType.LIGHTING,
        "com.signify.hue.blue" to ApplianceType.LIGHTING,

        // Water heaters
        "com.rheem.econet" to ApplianceType.WATER_HEATER,
        "com.aosmith.water" to ApplianceType.WATER_HEATER
    )

    fun getSuggestedAppliance(packageName: String): ApplianceType? {
        return knownApps[packageName]
    }
}

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
        val isLoading: Boolean = true,
        // Batch selection mode
        val isBatchMode: Boolean = false,
        val selectedApps: Set<String> = emptySet(), // Package names
        val showBatchConfigDialog: Boolean = false
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

        // Use appropriate flags for different Android versions
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            @Suppress("DEPRECATION")
            0
        }

        return pm.queryIntentActivities(intent, flags)
            .mapNotNull { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val packageName = appInfo.packageName

                // Skip our own app
                if (packageName == context.packageName) {
                    return@mapNotNull null
                }

                // Include ALL launchable apps - user can monitor any app they want
                InstalledAppInfo(
                    packageName = packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = try {
                        pm.getApplicationIcon(packageName)
                    } catch (e: Exception) {
                        null
                    },
                    suggestedAppliance = SmartHomeAppSuggestions.getSuggestedAppliance(packageName)
                )
            }
            .distinctBy { it.packageName }
            // Sort: known smart home apps first, then alphabetically
            .sortedWith(
                compareByDescending<InstalledAppInfo> { it.suggestedAppliance != null }
                    .thenBy { it.appName.lowercase() }
            )
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

    // Batch selection mode functions
    fun toggleBatchMode() {
        _uiState.update { state ->
            state.copy(
                isBatchMode = !state.isBatchMode,
                selectedApps = emptySet()
            )
        }
    }

    fun toggleAppSelection(packageName: String) {
        _uiState.update { state ->
            val newSelection = if (packageName in state.selectedApps) {
                state.selectedApps - packageName
            } else {
                state.selectedApps + packageName
            }
            state.copy(selectedApps = newSelection)
        }
    }

    fun selectAllApps() {
        _uiState.update { state ->
            state.copy(
                selectedApps = state.availableApps.map { it.packageName }.toSet()
            )
        }
    }

    fun clearSelection() {
        _uiState.update { state ->
            state.copy(selectedApps = emptySet())
        }
    }

    fun showBatchConfigDialog() {
        _uiState.update { state ->
            state.copy(showBatchConfigDialog = true)
        }
    }

    fun dismissBatchConfigDialog() {
        _uiState.update { state ->
            state.copy(showBatchConfigDialog = false)
        }
    }

    fun createBatchMappings(applianceType: ApplianceType, efficiency: EfficiencyCategory) {
        val selectedPackages = _uiState.value.selectedApps
        val availableApps = _uiState.value.availableApps

        viewModelScope.launch {
            selectedPackages.forEach { packageName ->
                val appInfo = availableApps.find { it.packageName == packageName }
                if (appInfo != null) {
                    appMappingRepository.addMapping(
                        AppMapping(
                            packageName = appInfo.packageName,
                            appName = appInfo.appName,
                            applianceType = appInfo.suggestedAppliance ?: applianceType,
                            efficiencyCategory = efficiency,
                            isEnabled = true
                        )
                    )
                }
            }
            _uiState.update { state ->
                state.copy(
                    isBatchMode = false,
                    selectedApps = emptySet(),
                    showBatchConfigDialog = false
                )
            }
        }
    }

    fun quickAddWithSuggestions() {
        // Add all apps that have suggested appliance types with their suggestions
        val suggestedApps = _uiState.value.availableApps.filter { it.suggestedAppliance != null }

        viewModelScope.launch {
            suggestedApps.forEach { appInfo ->
                appMappingRepository.addMapping(
                    AppMapping(
                        packageName = appInfo.packageName,
                        appName = appInfo.appName,
                        applianceType = appInfo.suggestedAppliance!!,
                        efficiencyCategory = EfficiencyCategory.NORMAL,
                        isEnabled = true
                    )
                )
            }
        }
    }
}
