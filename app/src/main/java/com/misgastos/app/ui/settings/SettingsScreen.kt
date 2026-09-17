package com.misgastos.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgastos.app.data.prefs.ThemeMode
import com.misgastos.app.di.LocalAppContainer
import com.misgastos.app.ui.components.SectionTitle
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val themeMode by container.userPreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item { Text("Configuración", style = MaterialTheme.typography.headlineMedium) }

        item { SectionTitle("Apariencia") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { scope.launch { container.userPreferences.setThemeMode(ThemeMode.LIGHT) } },
                    label = { Text("☀️ Claro") }
                )
                FilterChip(
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { scope.launch { container.userPreferences.setThemeMode(ThemeMode.DARK) } },
                    label = { Text("🌙 Oscuro") }
                )
                FilterChip(
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { scope.launch { container.userPreferences.setThemeMode(ThemeMode.SYSTEM) } },
                    label = { Text("⚙️ Sistema") }
                )
            }
        }

        item { SectionTitle("General") }
        item {
            SettingsRow("🍔", "Categorías y establecimientos", "Agrega, edita o elimina categorías y lugares", onNavigateToCategories)
        }
        item {
            SettingsRow("💳", "Formas de pago", "Efectivo, tarjetas, transferencias y más", onNavigateToPaymentMethods)
        }
        item {
            SettingsRow("🎯", "Presupuestos", "Presupuesto mensual y por categoría", onNavigateToBudgets)
        }
        item {
            SettingsRow("🔔", "Alertas", "Avisos de presupuesto y resúmenes periódicos", onNavigateToAlerts)
        }
        item {
            SettingsRow("💾", "Respaldo y exportación", "Exporta a CSV, comparte reportes y respalda tus datos", onNavigateToBackup)
        }
    }
}

@Composable
private fun SettingsRow(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}
