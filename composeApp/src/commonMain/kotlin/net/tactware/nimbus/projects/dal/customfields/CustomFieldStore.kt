package net.tactware.nimbus.projects.dal.customfields

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton store for custom fields.
 * This is a temporary in-memory solution, with persistence to be implemented later.
 */
object CustomFieldStore {
    private val _customFields = MutableStateFlow<List<CustomField>>(emptyList())
    val customFields: StateFlow<List<CustomField>> = _customFields.asStateFlow()

    /**
     * Adds a new custom field to the store.
     *
     * @param customField The custom field to add
     * @return True if the field was added, false if a field with the same name already exists
     */
    fun addCustomField(customField: CustomField): Boolean {
        val currentFields = _customFields.value

        // Check if a field with the same name already exists
        if (currentFields.any { it.name == customField.name }) {
            return false
        }

        // Add the new field
        _customFields.value = currentFields + customField
        return true
    }

    /**
     * Updates an existing custom field.
     *
     * @param customField The custom field to update
     * @return True if the field was updated, false if no field with the given name exists
     */
    fun updateCustomField(customField: CustomField): Boolean {
        val currentFields = _customFields.value

        // Find the index of the field to update
        val index = currentFields.indexOfFirst { it.name == customField.name }
        if (index == -1) {
            return false
        }

        // Update the field
        val updatedFields = currentFields.toMutableList()
        updatedFields[index] = customField
        _customFields.value = updatedFields
        return true
    }

    /**
     * Removes a custom field from the store.
     *
     * @param fieldName The name of the field to remove
     * @return True if the field was removed, false if no field with the given name exists
     */
    fun removeCustomField(fieldName: String): Boolean {
        val currentFields = _customFields.value

        // Check if the field exists
        if (currentFields.none { it.name == fieldName }) {
            return false
        }

        // Remove the field
        _customFields.value = currentFields.filter { it.name != fieldName }
        return true
    }

    /**
     * Gets a custom field by name.
     *
     * @param fieldName The name of the field to get
     * @return The custom field, or null if no field with the given name exists
     */
    fun getCustomField(fieldName: String): CustomField? {
        return _customFields.value.find { it.name == fieldName }
    }

    /**
     * Gets custom fields for a specific project.
     * Returns fields that either have an empty projectIds list (available for all projects)
     * or have the specified project ID in their projectIds list.
     *
     * @param projectId The ID of the project to get fields for, or null to get fields available for all projects
     * @return List of custom fields available for the specified project
     */
    fun getCustomFieldsForProject(projectId: Any?): List<CustomField> {
        val projectIdString = projectId?.toString()
        return _customFields.value.filter { field ->
            field.projectIds.isEmpty() || (projectIdString != null && field.projectIds.contains(projectIdString))
        }
    }

    /**
     * Clears all custom fields from the store.
     */
    fun clearCustomFields() {
        _customFields.value = emptyList()
    }
}
