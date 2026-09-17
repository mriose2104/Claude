package com.misgastos.app.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.misgastos.app.data.backup.BackupManager
import com.misgastos.app.data.export.CsvExporter
import com.misgastos.app.di.LocalAppContainer
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.util.DateRangeUtils
import com.misgastos.app.domain.util.StatsCalculator
import com.misgastos.app.ui.components.SectionTitle
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isWorking by remember { mutableStateOf(false) }

    fun shareFile(file: java.io.File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir"))
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isWorking = true
        scope.launch {
            runCatching { BackupManager.restoreBackup(context, container.database, uri) }
                .onSuccess { snackbarHostState.showSnackbar("Respaldo restaurado correctamente") }
                .onFailure { snackbarHostState.showSnackbar("No se pudo restaurar: ${it.message}") }
            isWorking = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Respaldo y exportación") },
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
            item { SectionTitle("Exportar") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                        Text("Historial completo (CSV)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Exporta todos tus movimientos a un archivo compatible con Excel o Google Sheets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                isWorking = true
                                scope.launch {
                                    val expenses = container.expenseRepository.observeAllExpenses().first()
                                    val file = CsvExporter.exportExpensesCsv(context, expenses)
                                    shareFile(file, "text/csv")
                                    isWorking = false
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Exportar CSV") }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resumen del mes actual", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Genera y comparte un reporte con el periodo, totales, categorías, establecimientos, KPIs y detalle de movimientos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                isWorking = true
                                scope.launch {
                                    val range = DateRangeUtils.resolve(DateRangeFilter.ThisMonth)
                                    val expenses = container.expenseRepository.observeExpensesBetween(range).first()
                                    val stats = StatsCalculator.compute(expenses, range)
                                    val now = java.time.LocalDate.now()
                                    val label = now.month.getDisplayName(TextStyle.FULL, Locale("es", "MX"))
                                        .replaceFirstChar { it.uppercase() } + " ${now.year}"
                                    val file = CsvExporter.exportSummaryReport(context, label, stats, expenses)
                                    shareFile(file, "text/plain")
                                    isWorking = false
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Generar y compartir reporte") }
                    }
                }
            }

            item { SectionTitle("Respaldo de la información") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                        Text("Crear respaldo completo", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Guarda todos tus gastos, categorías, establecimientos y configuración en un solo archivo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                isWorking = true
                                scope.launch {
                                    val file = BackupManager.exportBackup(context, container.database)
                                    shareFile(file, "application/json")
                                    isWorking = false
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Crear y compartir respaldo") }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                        Text("Restaurar respaldo", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "⚠️ Reemplaza todos los datos actuales con los del archivo de respaldo seleccionado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(
                            onClick = { restoreLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Elegir archivo y restaurar") }
                    }
                }
            }

            if (isWorking) {
                item { CircularProgressIndicator() }
            }
        }
    }
}
