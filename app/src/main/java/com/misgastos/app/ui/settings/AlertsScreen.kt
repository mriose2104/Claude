package com.misgastos.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.misgastos.app.data.notification.SummaryScheduler
import com.misgastos.app.di.LocalAppContainer
import com.misgastos.app.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: AlertsViewModel = viewModel(
        factory = viewModelFactory { initializer { AlertsViewModel(container.budgetRepository) } }
    )
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(settings.weeklySummaryEnabled) { SummaryScheduler.applyWeekly(context, settings.weeklySummaryEnabled) }
    LaunchedEffect(settings.monthlySummaryEnabled) { SummaryScheduler.applyMonthly(context, settings.monthlySummaryEnabled) }

    var dailyLimitText by remember(settings.dailyLimitAmount) {
        mutableStateOf(if (settings.dailyLimitAmount > 0) settings.dailyLimitAmount.toInt().toString() else "")
    }
    var thresholdText by remember(settings.budgetPercentThreshold) { mutableStateOf(settings.budgetPercentThreshold.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item {
                SwitchRow(
                    title = "Notificaciones activas",
                    subtitle = "Interruptor general para todas las alertas",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.update { it.copy(notificationsEnabled = enabled) }
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }

            item { SectionTitle("Presupuesto") }
            item {
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = {
                        thresholdText = it.filter { c -> c.isDigit() }
                        thresholdText.toIntOrNull()?.let { pct -> viewModel.update { s -> s.copy(budgetPercentThreshold = pct.coerceIn(1, 100)) } }
                    },
                    label = { Text("Avisar al alcanzar este % del presupuesto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                SwitchRow(
                    title = "Alertar cuando una categoría supere su presupuesto",
                    subtitle = null,
                    checked = settings.categoryBudgetAlertEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(categoryBudgetAlertEnabled = it) } }
                )
            }

            item { SectionTitle("Gasto diario") }
            item {
                SwitchRow(
                    title = "Alertar si el gasto diario supera un límite",
                    subtitle = null,
                    checked = settings.dailyLimitEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(dailyLimitEnabled = it) } }
                )
            }
            if (settings.dailyLimitEnabled) {
                item {
                    OutlinedTextField(
                        value = dailyLimitText,
                        onValueChange = {
                            dailyLimitText = it.filter { c -> c.isDigit() }
                            viewModel.update { s -> s.copy(dailyLimitAmount = dailyLimitText.toDoubleOrNull() ?: 0.0) }
                        },
                        label = { Text("Límite diario") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            item { SectionTitle("Resúmenes periódicos") }
            item {
                SwitchRow(
                    title = "Resumen semanal",
                    subtitle = "Recibe un aviso con tu gasto total de la semana",
                    checked = settings.weeklySummaryEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(weeklySummaryEnabled = it) } }
                )
            }
            item {
                SwitchRow(
                    title = "Resumen mensual",
                    subtitle = "Recibe un aviso con tu gasto total del mes",
                    checked = settings.monthlySummaryEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(monthlySummaryEnabled = it) } }
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
