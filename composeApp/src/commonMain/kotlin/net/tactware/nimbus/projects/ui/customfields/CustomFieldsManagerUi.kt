package net.tactware.nimbus.projects.ui.customfields

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.projects.bl.GetAllProjectNameUseCase
import net.tactware.nimbus.projects.dal.customfields.CustomField
import net.tactware.nimbus.projects.dal.customfields.CustomFieldStore
import net.tactware.nimbus.projects.dal.customfields.CustomFieldType
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.compose.koinInject

/**
 * UI component for managing custom fields.
 * This allows users to add, edit, and remove custom fields that can be used in work items.
 */
@Composable
fun CustomFieldsManagerUi() {
    val customFields by CustomFieldStore.customFields.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var fieldToEdit by remember { mutableStateOf<CustomField?>(null) }

    // Inject the GetAllProjectNameUseCase
    val getAllProjectNameUseCase = koinInject<GetAllProjectNameUseCase>()

    // State for available projects
    var availableProjects by remember { mutableStateOf<List<ProjectIdentifier>>(emptyList()) }

    // Load available projects
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                availableProjects = getAllProjectNameUseCase()
            } catch (e: Exception) {
                // Handle error
                println("Error loading projects: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
    ) {
        // Header
        Text(
            "Custom Fields Manager",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        )

        // Add button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Field")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Custom Field")
        }

        // List of custom fields
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (customFields.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No custom fields defined yet. Click 'Add Custom Field' to create one.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
                ) {
                    items(customFields) { field ->
                        CustomFieldItem(
                            field = field,
                            onEdit = { fieldToEdit = field },
                            onDelete = { CustomFieldStore.removeCustomField(field.name) }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit dialog
    if (showAddDialog || fieldToEdit != null) {
        CustomFieldDialog(
            field = fieldToEdit,
            availableProjects = availableProjects,
            onDismiss = {
                showAddDialog = false
                fieldToEdit = null
            },
            onSave = { field ->
                if (fieldToEdit != null) {
                    CustomFieldStore.updateCustomField(field)
                } else {
                    CustomFieldStore.addCustomField(field)
                }
                showAddDialog = false
                fieldToEdit = null
            }
        )
    }
}

/**
 * UI component for displaying a single custom field in the list.
 */
@Composable
private fun CustomFieldItem(
    field: CustomField,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Field info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    field.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Type: ${getFieldTypeDisplayName(field.type)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (field.isRequired) {
                    Text(
                        "Required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (field.type is CustomFieldType.Options) {
                    Text(
                        "Options: ${field.type.options.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Show associated projects if any
                if (field.projectIds.isNotEmpty()) {
                    Text(
                        "Projects: ${field.projectIds.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Available for all projects",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

/**
 * Dialog for adding or editing a custom field.
 */
@Composable
private fun CustomFieldDialog(
    field: CustomField?,
    availableProjects: List<ProjectIdentifier>,
    onDismiss: () -> Unit,
    onSave: (CustomField) -> Unit
) {
    val isEditing = field != null
    var name by remember { mutableStateOf(field?.name ?: "") }
    var isRequired by remember { mutableStateOf(field?.isRequired ?: false) }
    var selectedType by remember { mutableStateOf(field?.type ?: CustomFieldType.SingleLineText) }
    var options by remember { mutableStateOf((field?.type as? CustomFieldType.Options)?.options?.joinToString("\n") ?: "") }
    var nameError by remember { mutableStateOf("") }

    // State for selected projects
    var selectedProjectIds by remember { mutableStateOf(field?.projectIds ?: emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Custom Field" else "Add Custom Field") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                // Field name
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = ""
                    },
                    label = { Text("Field Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError.isNotEmpty(),
                    supportingText = { if (nameError.isNotEmpty()) Text(nameError) }
                )

                // Field type
                Text("Field Type", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType is CustomFieldType.SingleLineText,
                        onClick = { selectedType = CustomFieldType.SingleLineText }
                    )
                    Text("Single Line Text")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType is CustomFieldType.MultiLineText,
                        onClick = { selectedType = CustomFieldType.MultiLineText }
                    )
                    Text("Multi-line Text")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType is CustomFieldType.Options,
                        onClick = { selectedType = CustomFieldType.Options(emptyList()) }
                    )
                    Text("Options")
                }

                // Options (only if Options type is selected)
                if (selectedType is CustomFieldType.Options) {
                    OutlinedTextField(
                        value = options,
                        onValueChange = { options = it },
                        label = { Text("Options (one per line)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        minLines = 3
                    )
                }

                // Required checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRequired,
                        onCheckedChange = { isRequired = it }
                    )
                    Text("Required Field")
                }

                // Project selection
                if (availableProjects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text("Associated Projects", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Leave empty to make this field available for all projects",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // List of projects with checkboxes
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableProjects.forEach { project ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedProjectIds.contains(project.id.toString()),
                                    onCheckedChange = { checked ->
                                        selectedProjectIds = if (checked) {
                                            selectedProjectIds + project.id.toString()
                                        } else {
                                            selectedProjectIds - project.id.toString()
                                        }
                                    }
                                )
                                Text(project.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Field name cannot be empty"
                        return@Button
                    }

                    // If editing, check if the name is changed and already exists
                    if (isEditing && name != field?.name && CustomFieldStore.getCustomField(name) != null) {
                        nameError = "A field with this name already exists"
                        return@Button
                    }

                    // If adding, check if the name already exists
                    if (!isEditing && CustomFieldStore.getCustomField(name) != null) {
                        nameError = "A field with this name already exists"
                        return@Button
                    }

                    val finalType = when (selectedType) {
                        is CustomFieldType.SingleLineText -> CustomFieldType.SingleLineText
                        is CustomFieldType.MultiLineText -> CustomFieldType.MultiLineText
                        is CustomFieldType.Options -> {
                            val optionsList = options.split("\n")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            CustomFieldType.Options(optionsList)
                        }
                    }

                    onSave(
                        CustomField(
                            name = name,
                            type = finalType,
                            isRequired = isRequired,
                            projectIds = selectedProjectIds
                        )
                    )
                }
            ) {
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

/**
 * Returns a display name for a field type.
 */
private fun getFieldTypeDisplayName(type: CustomFieldType): String {
    return when (type) {
        is CustomFieldType.SingleLineText -> "Single Line Text"
        is CustomFieldType.MultiLineText -> "Multi-line Text"
        is CustomFieldType.Options -> "Options"
    }
}
