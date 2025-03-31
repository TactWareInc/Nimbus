package net.tactware.nimbus.projects.ui.customfields

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.projects.dal.customfields.CustomField
import net.tactware.nimbus.projects.dal.customfields.CustomFieldStore
import net.tactware.nimbus.projects.dal.customfields.CustomFieldType

/**
 * Data class to hold custom field values.
 */
data class CustomFieldValue(
    val field: CustomField,
    val value: String
)

/**
 * UI component for displaying and entering values for custom fields in the work item form.
 *
 * @param customFieldValues The current values of the custom fields
 * @param onCustomFieldValueChanged Callback for when a custom field value changes
 * @param enabled Whether the form is enabled
 */
@Composable
fun CustomFieldsForm(
    customFieldValues: List<CustomFieldValue>,
    onCustomFieldValueChanged: (CustomFieldValue) -> Unit,
    enabled: Boolean = true
) {
    val customFields by CustomFieldStore.customFields.collectAsState()
    
    if (customFields.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Text(
            "Custom Fields",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
        )

        Divider()

        customFields.forEach { field ->
            val currentValue = customFieldValues.find { it.field.name == field.name }?.value ?: ""
            
            CustomFieldInput(
                field = field,
                value = currentValue,
                onValueChanged = { newValue ->
                    onCustomFieldValueChanged(
                        CustomFieldValue(
                            field = field,
                            value = newValue
                        )
                    )
                },
                enabled = enabled
            )
        }
    }
}

/**
 * UI component for displaying and entering a value for a single custom field.
 *
 * @param field The custom field
 * @param value The current value of the field
 * @param onValueChanged Callback for when the field value changes
 * @param enabled Whether the input is enabled
 */
@Composable
private fun CustomFieldInput(
    field: CustomField,
    value: String,
    onValueChanged: (String) -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small)
    ) {
        when (field.type) {
            is CustomFieldType.SingleLineText -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { 
                        Text(
                            if (field.isRequired) "${field.name} *" else field.name
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    singleLine = true
                )
            }
            is CustomFieldType.MultiLineText -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { 
                        Text(
                            if (field.isRequired) "${field.name} *" else field.name
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    enabled = enabled,
                    minLines = 3
                )
            }
            is CustomFieldType.Options -> {
                val options = field.type.options
                if (options.isEmpty()) {
                    Text(
                        "No options defined for ${field.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (enabled) expanded = it }
                    ) {
                        OutlinedTextField(
                            value = value.ifEmpty { "Select an option" },
                            onValueChange = {},
                            label = { 
                                Text(
                                    if (field.isRequired) "${field.name} *" else field.name
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = enabled,
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onValueChanged(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (field.isRequired) {
            val isError = value.isEmpty()
            if (isError) {
                Text(
                    "This field is required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}