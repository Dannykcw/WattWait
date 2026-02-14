package com.example.wattwait.ui.screen.apps

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wattwait.domain.model.AppMapping
import com.example.wattwait.domain.model.ApplianceType
import com.example.wattwait.domain.model.EfficiencyCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isBatchMode) {
                        Text("Select Apps (${uiState.selectedApps.size})")
                    } else {
                        Text("App Configuration")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (uiState.isBatchMode) {
                        IconButton(onClick = { viewModel.selectAllApps() }) {
                            Icon(Icons.Default.SelectAll, "Select All")
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Clear, "Clear Selection")
                        }
                        IconButton(onClick = { viewModel.toggleBatchMode() }) {
                            Icon(Icons.Default.Delete, "Cancel")
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleBatchMode() }) {
                            Icon(Icons.Default.CheckCircle, "Batch Select")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isBatchMode && uiState.selectedApps.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.showBatchConfigDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, "Add Selected")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add ${uiState.selectedApps.size}")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Configured Apps Section
            if (uiState.existingMappings.isNotEmpty()) {
                item {
                    Text(
                        text = "Monitored Apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.existingMappings, key = { it.id }) { mapping ->
                    ConfiguredAppCard(
                        mapping = mapping,
                        onToggle = { viewModel.toggleMapping(mapping.id, it) },
                        onEdit = { viewModel.editMapping(mapping) },
                        onDelete = { viewModel.deleteMapping(mapping.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Quick Add Section - Show if there are suggested smart home apps
            val suggestedApps = uiState.availableApps.filter { it.suggestedAppliance != null }
            if (suggestedApps.isNotEmpty() && !uiState.isBatchMode) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Quick Add",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${suggestedApps.size} recognized smart home apps found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = { viewModel.quickAddWithSuggestions() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add All Recognized Apps")
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Available Apps Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isBatchMode) "Select Apps to Add" else "Add New App",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!uiState.isBatchMode && uiState.availableApps.size > 3) {
                        TextButton(onClick = { viewModel.toggleBatchMode() }) {
                            Text("Select Multiple")
                        }
                    }
                }
            }

            if (uiState.availableApps.isEmpty() && !uiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "No additional apps available to add",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(uiState.availableApps, key = { it.packageName }) { appInfo ->
                if (uiState.isBatchMode) {
                    SelectableAppCard(
                        appInfo = appInfo,
                        isSelected = appInfo.packageName in uiState.selectedApps,
                        onToggle = { viewModel.toggleAppSelection(appInfo.packageName) }
                    )
                } else {
                    AvailableAppCard(
                        appInfo = appInfo,
                        onClick = { viewModel.selectApp(appInfo) }
                    )
                }
            }
        }
    }

    // Appliance Mapping Dialog
    if (uiState.showApplianceDialog && uiState.selectedApp != null) {
        ApplianceMappingDialog(
            appName = uiState.selectedApp!!.appName,
            initialAppliance = uiState.editingMapping?.applianceType
                ?: uiState.selectedApp!!.suggestedAppliance,
            initialEfficiency = uiState.editingMapping?.efficiencyCategory,
            onConfirm = { appliance, efficiency ->
                viewModel.createMapping(appliance, efficiency)
            },
            onDismiss = { viewModel.dismissDialog() }
        )
    }

    // Batch Configuration Dialog
    if (uiState.showBatchConfigDialog) {
        BatchConfigDialog(
            selectedCount = uiState.selectedApps.size,
            hasSuggestions = uiState.availableApps
                .filter { it.packageName in uiState.selectedApps }
                .any { it.suggestedAppliance != null },
            onConfirm = { appliance, efficiency ->
                viewModel.createBatchMappings(appliance, efficiency)
            },
            onDismiss = { viewModel.dismissBatchConfigDialog() }
        )
    }
}

@Composable
private fun ConfiguredAppCard(
    mapping: AppMapping,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (mapping.isEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mapping.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${mapping.applianceType.displayName} - ${mapping.efficiencyCategory.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit")
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }

            Switch(
                checked = mapping.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun AvailableAppCard(
    appInfo: InstalledAppInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appInfo.icon != null) {
                Image(
                    bitmap = appInfo.icon.toBitmap(48, 48).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.titleSmall
                )
                if (appInfo.suggestedAppliance != null) {
                    Text(
                        text = "Suggested: ${appInfo.suggestedAppliance.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SelectableAppCard(
    appInfo: InstalledAppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (appInfo.icon != null) {
                Image(
                    bitmap = appInfo.icon.toBitmap(48, 48).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.titleSmall
                )
                if (appInfo.suggestedAppliance != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = appInfo.suggestedAppliance.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchConfigDialog(
    selectedCount: Int,
    hasSuggestions: Boolean,
    onConfirm: (ApplianceType, EfficiencyCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAppliance by remember { mutableStateOf(ApplianceType.GENERIC) }
    var selectedEfficiency by remember { mutableStateOf(EfficiencyCategory.NORMAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure $selectedCount Apps") },
        text = {
            Column {
                if (hasSuggestions) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Apps with recognized appliance types will use their suggested settings.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Default Appliance Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "For apps without a suggestion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(Modifier.selectableGroup()) {
                    listOf(
                        ApplianceType.GENERIC,
                        ApplianceType.THERMOSTAT,
                        ApplianceType.AIR_CONDITIONER,
                        ApplianceType.LIGHTING
                    ).forEach { appliance ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .selectable(
                                    selected = selectedAppliance == appliance,
                                    onClick = { selectedAppliance = appliance },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAppliance == appliance,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appliance.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Efficiency Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(Modifier.selectableGroup()) {
                    EfficiencyCategory.entries.forEach { efficiency ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .selectable(
                                    selected = selectedEfficiency == efficiency,
                                    onClick = { selectedEfficiency = efficiency },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedEfficiency == efficiency,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = efficiency.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedAppliance, selectedEfficiency) }) {
                Text("Add $selectedCount Apps")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ApplianceMappingDialog(
    appName: String,
    initialAppliance: ApplianceType?,
    initialEfficiency: EfficiencyCategory?,
    onConfirm: (ApplianceType, EfficiencyCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAppliance by remember { mutableStateOf(initialAppliance ?: ApplianceType.GENERIC) }
    var selectedEfficiency by remember { mutableStateOf(initialEfficiency ?: EfficiencyCategory.NORMAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure $appName") },
        text = {
            Column {
                Text(
                    text = "Appliance Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Appliance selection - show common ones
                Column(Modifier.selectableGroup()) {
                    listOf(
                        ApplianceType.OVEN,
                        ApplianceType.WASHER,
                        ApplianceType.DRYER,
                        ApplianceType.DISHWASHER,
                        ApplianceType.AIR_CONDITIONER,
                        ApplianceType.THERMOSTAT,
                        ApplianceType.WATER_HEATER,
                        ApplianceType.EV_CHARGER,
                        ApplianceType.LIGHTING,
                        ApplianceType.GENERIC
                    ).forEach { appliance ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .selectable(
                                    selected = selectedAppliance == appliance,
                                    onClick = { selectedAppliance = appliance },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAppliance == appliance,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${appliance.displayName} (${appliance.averageKwhPerUse} kWh)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Efficiency Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(Modifier.selectableGroup()) {
                    EfficiencyCategory.entries.forEach { efficiency ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = selectedEfficiency == efficiency,
                                    onClick = { selectedEfficiency = efficiency },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedEfficiency == efficiency,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = efficiency.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = efficiency.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedAppliance, selectedEfficiency) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
