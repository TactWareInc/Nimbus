package net.tactware.nimbus.appwide

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Singleton service for handling notifications across the application.
 * Currently uses in-memory storage for notifications.
 */
object NotificationService {
    
    /**
     * Data class representing a notification
     */
    data class Notification(
        val id: String = generateId(),
        val title: String,
        val message: String,
        val timestamp: Instant = Clock.System.now(),
        val isRead: Boolean = false
    )
    
    // In-memory storage for notifications
    private val _notifications = mutableStateListOf<Notification>()
    
    // Public read-only access to notifications
    val notifications: SnapshotStateList<Notification> = _notifications
    
    /**
     * Adds a new notification
     */
    fun addNotification(title: String, message: String) {
        _notifications.add(
            Notification(
                title = title,
                message = message
            )
        )
    }
    
    /**
     * Marks a notification as read
     */
    fun markAsRead(id: String) {
        val index = _notifications.indexOfFirst { it.id == id }
        if (index != -1) {
            val notification = _notifications[index]
            _notifications[index] = notification.copy(isRead = true)
        }
    }
    
    /**
     * Marks all notifications as read
     */
    fun markAllAsRead() {
        val updatedNotifications = _notifications.map { it.copy(isRead = true) }
        _notifications.clear()
        _notifications.addAll(updatedNotifications)
    }
    
    /**
     * Removes a notification
     */
    fun removeNotification(id: String) {
        _notifications.removeIf { it.id == id }
    }
    
    /**
     * Clears all notifications
     */
    fun clearAllNotifications() {
        _notifications.clear()
    }
    
    /**
     * Returns the count of unread notifications
     */
    fun getUnreadCount(): Int {
        return _notifications.count { !it.isRead }
    }
    
    /**
     * Generates a unique ID for a notification
     */
    private fun generateId(): String {
        return System.currentTimeMillis().toString()
    }
}