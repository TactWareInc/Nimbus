package net.tactware.nimbus.projects.dal.entities

/**
 * Data class representing information about a Personal Access Token (PAT).
 *
 * @property token The PAT token value
 * @property displayName The display name of the PAT
 * @property validFrom The date when the PAT becomes valid (milliseconds since epoch)
 * @property validTo The date when the PAT expires (milliseconds since epoch)
 * @property scope The scope of the PAT (what it has access to)
 * @property isValid Whether the PAT is currently valid
 */
data class PatInfo(
    val token: String,
    val displayName: String = "",
    val validFrom: Long? = null,
    val validTo: Long? = null,
    val scope: String = "",
    val isValid: Boolean = true
) {
    /**
     * Checks if the PAT is about to expire within the specified warning period.
     *
     * @param warningPeriodMs The warning period in milliseconds. Default is 7 days.
     * @return True if the PAT is about to expire, false otherwise.
     */
    fun isExpiring(warningPeriodMs: Long = 7 * 24 * 60 * 60 * 1000): Boolean {
        val now = System.currentTimeMillis()
        return validTo?.let { expirationDate ->
            expirationDate > now && expirationDate - now <= warningPeriodMs
        } ?: false
    }

    /**
     * Checks if the PAT has already expired.
     *
     * @return True if the PAT has expired, false otherwise.
     */
    fun isExpired(): Boolean {
        val now = System.currentTimeMillis()
        return validTo?.let { expirationDate ->
            expirationDate <= now
        } ?: false
    }
}