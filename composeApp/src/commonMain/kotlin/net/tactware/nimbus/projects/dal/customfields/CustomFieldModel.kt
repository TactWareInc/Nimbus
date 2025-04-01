package net.tactware.nimbus.projects.dal.customfields

/**
 * Sealed class representing different types of custom fields that can be added to work items.
 * This addresses the use case where someone has added a custom field to their work item that is mandatory.
 */
sealed class CustomFieldType {
    /**
     * Single line text field.
     */
    object SingleLineText : CustomFieldType()

    /**
     * Multi-line text field.
     */
    object MultiLineText : CustomFieldType()

    /**
     * Options field with a list of possible values.
     *
     * @property options List of possible values for this field
     */
    data class Options(val options: List<String>) : CustomFieldType()
}

/**
 * Represents a custom field that can be added to work items.
 *
 * @property name The name of the custom field
 * @property type The type of the custom field
 * @property isRequired Whether the field is required
 * @property projectIds List of project IDs this field is associated with. Empty list means it's available for all projects.
 */
data class CustomField(
    val name: String,
    val type: CustomFieldType,
    val isRequired: Boolean = false,
    val projectIds: List<String> = emptyList()
)
