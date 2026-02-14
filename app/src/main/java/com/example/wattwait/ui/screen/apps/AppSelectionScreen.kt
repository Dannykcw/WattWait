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
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
                title = { Text("App Configuration") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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

            // Available Apps Section
            item {
                Text(
                    text = "Add New App",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                AvailableAppCard(
                    appInfo = appInfo,
                    onClick = { viewModel.selectApp(appInfo) }
                )
            }
        }
    }

    // Appliance Mapping Dialog
    if (uiState.showApplianceDialog && uiState.selectedApp != null) {
        ApplianceMappingDialog(
            appName = uiState.selectedApp!!.appName,
            initialAppliance = uiState.editingMapping?.applianceType,
            initialEfficiency = uiState.editingMapping?.efficiencyCategory,
            onConfirm = { appliance, efficiency ->
                viewModel.createMapping(appliance, efficiency)
            },
            onDismiss = { viewModel.dismissDialog() }
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

            Text(
                text = appInfo.appName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
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
