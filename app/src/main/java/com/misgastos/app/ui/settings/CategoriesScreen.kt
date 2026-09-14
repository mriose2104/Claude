package com.misgastos.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.di.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: CategoriesViewModel = viewModel(
        factory = viewModelFactory { initializer { CategoriesViewModel(container.catalogRepository) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var expanded by remember { mutableStateOf(setOf<Long>()) }
    var showAddCategory by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var deletingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var addingEstablishmentFor by remember { mutableStateOf<Long?>(null) }
    var editingEstablishment by remember { mutableStateOf<EstablishmentEntity?>(null) }
    var deletingEstablishment by remember { mutableStateOf<EstablishmentEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Categorías y establecimientos") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item {
                androidx.compose.material3.Button(onClick = { showAddCategory = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" Agregar categoría")
                }
            }

            items(state.categories, key = { it.id }) { category ->
                val isExpanded = expanded.contains(category.id)
                val establishments = state.establishmentsByCategory[category.id].orEmpty()

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = if (isExpanded) expanded - category.id else expanded + category.id }
                                .padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(category.icon, style = MaterialTheme.typography.titleLarge)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${establishments.size} establecimiento(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { editingCategory = category }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar categoría")
                            }
                            IconButton(onClick = { deletingCategory = category }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar categoría")
                            }
                            Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                        }

                        if (isExpanded) {
                            Column(modifier = Modifier.padding(start = 24.dp, end = 8.dp, bottom = 8.dp)) {
                                establishments.forEach { establishment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(establishment.name, style = MaterialTheme.typography.bodyLarge)
                                        Row {
                                            IconButton(onClick = { editingEstablishment = establishment }) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Editar establecimiento")
                                            }
                                            IconButton(onClick = { deletingEstablishment = establishment }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar establecimiento")
                                            }
                                        }
                                    }
                                }
                                TextButton(onClick = { addingEstablishmentFor = category.id }) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Text(" Agregar establecimiento")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategory) {
        NameIconDialog(
            title = "Nueva categoría",
            initialName = "",
            initialIcon = "🏷️",
            onConfirm = { name, icon -> viewModel.addCategory(name, icon) },
            onDismiss = { showAddCategory = false }
        )
    }
    editingCategory?.let { category ->
        NameIconDialog(
            title = "Editar categoría",
            initialName = category.name,
            initialIcon = category.icon,
            onConfirm = { name, icon -> viewModel.updateCategory(category, name, icon) },
            onDismiss = { editingCategory = null }
        )
    }
    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("¿Eliminar \"${category.name}\"?") },
            text = { Text("Se eliminarán también sus establecimientos. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    deletingCategory = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { deletingCategory = null }) { Text("Cancelar") } }
        )
    }

    addingEstablishmentFor?.let { categoryId ->
        NameOnlyDialog(
            title = "Nuevo establecimiento",
            initialName = "",
            onConfirm = { name -> viewModel.addEstablishment(categoryId, name) },
            onDismiss = { addingEstablishmentFor = null }
        )
    }
    editingEstablishment?.let { establishment ->
        NameOnlyDialog(
            title = "Editar establecimiento",
            initialName = establishment.name,
            onConfirm = { name -> viewModel.updateEstablishment(establishment, name) },
            onDismiss = { editingEstablishment = null }
        )
    }
    deletingEstablishment?.let { establishment ->
        AlertDialog(
            onDismissRequest = { deletingEstablishment = null },
            title = { Text("¿Eliminar \"${establishment.name}\"?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEstablishment(establishment)
                    deletingEstablishment = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { deletingEstablishment = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun NameIconDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("Ícono (emoji)") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, icon); onDismiss() }, enabled = name.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun NameOnlyDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name); onDismiss() }, enabled = name.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
