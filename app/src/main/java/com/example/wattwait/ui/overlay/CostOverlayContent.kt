package com.example.wattwait.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wattwait.ui.theme.OffPeakTeal
import com.example.wattwait.ui.theme.PeakWarning
import com.example.wattwait.ui.theme.SavingsGreen
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CostOverlayContent(
    appName: String,
    applianceName: String,
    estimatedCost: Double,
    currentRate: Double,
    isPeakTime: Boolean,
    offPeakTime: LocalTime?,
    hoursUntilOffPeak: Int?,
    savingsAmount: Double,
    savingsPercentage: Double,
    environmentalMessage: String,
    isBlockingMode: Boolean = false,
    onDismiss: () -> Unit,
    onContinueAnyway: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    // Check if this is a fallback overlay (no rate data available)
    val isFallback = estimatedCost < 0

    if (isBlockingMode) {
        // Full-screen blocking overlay
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                BlockingOverlayCard(
                    appName = appName,
                    applianceName = applianceName,
                    estimatedCost = estimatedCost,
                    currentRate = currentRate,
                    offPeakTime = offPeakTime,
                    hoursUntilOffPeak = hoursUntilOffPeak,
                    savingsAmount = savingsAmount,
                    savingsPercentage = savingsPercentage,
                    environmentalMessage = environmentalMessage,
                    onContinueAnyway = {
                        visible = false
                        onContinueAnyway()
                    },
                    onWaitForOffPeak = {
                        visible = false
                        onDismiss()
                    }
                )
            }
        }
    } else {
        // Non-blocking notification overlay
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            NonBlockingOverlayCard(
                appName = appName,
                applianceName = applianceName,
                estimatedCost = estimatedCost,
                currentRate = currentRate,
                isPeakTime = isPeakTime,
                isFallback = isFallback,
                offPeakTime = offPeakTime,
                hoursUntilOffPeak = hoursUntilOffPeak,
                savingsAmount = savingsAmount,
                savingsPercentage = savingsPercentage,
                environmentalMessage = environmentalMessage,
                onDismiss = {
                    visible = false
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun BlockingOverlayCard(
    appName: String,
    applianceName: String,
    estimatedCost: Double,
    currentRate: Double,
    offPeakTime: LocalTime?,
    hoursUntilOffPeak: Int?,
    savingsAmount: Double,
    savingsPercentage: Double,
    environmentalMessage: String,
    onContinueAnyway: () -> Unit,
    onWaitForOffPeak: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PeakWarning
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Warning icon
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Peak Hours Alert",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$appName ($applianceName)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cost info
            if (estimatedCost >= 0) {
                Text(
                    text = "Estimated Cost: $${String.format("%.2f", estimatedCost)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = "Current rate: $${String.format("%.4f", currentRate)}/kWh",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Off-peak info
            offPeakTime?.let { time ->
                val formattedTime = time.format(DateTimeFormatter.ofPattern("h:mm a"))
                Text(
                    text = "Off-peak starts at $formattedTime",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                hoursUntilOffPeak?.let { hours ->
                    Text(
                        text = "(in $hours hour${if (hours != 1) "s" else ""})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            if (savingsAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Save $${String.format("%.2f", savingsAmount)} (${savingsPercentage.toInt()}%) by waiting!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Environmental message
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = environmentalMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary: Wait for off-peak
                Button(
                    onClick = onWaitForOffPeak,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SavingsGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wait for Off-Peak",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Secondary: Continue anyway
                OutlinedButton(
                    onClick = onContinueAnyway,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Continue Anyway")
                }
            }
        }
    }
}

@Composable
private fun NonBlockingOverlayCard(
    appName: String,
    applianceName: String,
    estimatedCost: Double,
    currentRate: Double,
    isPeakTime: Boolean,
    isFallback: Boolean,
    offPeakTime: LocalTime?,
    hoursUntilOffPeak: Int?,
    savingsAmount: Double,
    savingsPercentage: Double,
    environmentalMessage: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFallback -> OffPeakTeal.copy(alpha = 0.95f)
                isPeakTime -> PeakWarning.copy(alpha = 0.95f)
                else -> SavingsGreen.copy(alpha = 0.95f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with dismiss button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isFallback -> Icons.Default.Info
                            isPeakTime -> Icons.Default.Warning
                            else -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = applianceName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isFallback) {
                    // Fallback display when no rate data
                    Text(
                        text = "WattWait Active",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = environmentalMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                } else {
                    // Normal cost display
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$${String.format("%.2f", estimatedCost)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "estimated cost",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }

                    Text(
                        text = "Current rate: $${String.format("%.4f", currentRate)}/kWh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Peak/Off-peak info
                    if (isPeakTime) {
                        PeakTimeInfo(
                            offPeakTime = offPeakTime,
                            hoursUntilOffPeak = hoursUntilOffPeak,
                            savingsAmount = savingsAmount,
                            savingsPercentage = savingsPercentage
                        )
                    } else {
                        OffPeakInfo()
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Environmental message
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = environmentalMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
}

@Composable
private fun PeakTimeInfo(
    offPeakTime: LocalTime?,
    hoursUntilOffPeak: Int?,
    savingsAmount: Double,
    savingsPercentage: Double
) {
    Column {
        Text(
            text = "PEAK HOURS - Higher rates!",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        offPeakTime?.let { time ->
            val formattedTime = time.format(DateTimeFormatter.ofPattern("h:mm a"))
            Text(
                text = "Off-peak starts at $formattedTime" +
                        (hoursUntilOffPeak?.let { " (in $it hour${if (it != 1) "s" else ""})" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }

        if (savingsAmount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Save $${String.format("%.2f", savingsAmount)} (${savingsPercentage.toInt()}%) by waiting!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun OffPeakInfo() {
    Column {
        Text(
            text = "OFF-PEAK HOURS - Great time!",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "You're saving money with lower rates right now!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
        )
    }
}
