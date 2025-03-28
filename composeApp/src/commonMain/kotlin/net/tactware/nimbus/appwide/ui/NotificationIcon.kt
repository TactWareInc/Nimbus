package net.tactware.nimbus.appwide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.NotificationService

/**
 * A composable that displays a notification icon with a badge showing the number of unread notifications.
 * When clicked, it shows a dropdown menu with the list of notifications.
 */
@Composable
fun NotificationIcon() {
    val unreadCount = NotificationService.getUnreadCount()
    var showDropdown by remember { mutableStateOf(false) }

    BadgedBox(badge = {
        if (unreadCount > 0) {
            Badge(content = { Text(unreadCount.toString()) })
        }
     }) {


      IconButton( { showDropdown = true }, enabled = unreadCount > 0) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Dropdown menu for notifications
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            if (NotificationService.notifications.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No notifications") },
                    onClick = { showDropdown = false }
                )
            } else {
                // Header with actions
                DropdownMenuItem(
                    text = { Text("Notifications") },
                    onClick = { /* Do nothing */ },
                    trailingIcon = {
                        Text(
                            "Mark all as read",
                            modifier = Modifier.clickable {
                                NotificationService.markAllAsRead()
                            },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
                
                // List of notifications
                NotificationService.notifications.forEach { notification ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                notification.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (!notification.isRead) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            )
                        },
                        onClick = {
                            NotificationService.markAsRead(notification.id)
                        }
                    )
                }
                
                // Clear all option
                DropdownMenuItem(
                    text = { Text("Clear all") },
                    onClick = {
                        NotificationService.clearAllNotifications()
                        showDropdown = false
                    }
                )
            }
        }
    }
}