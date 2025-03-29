
package net.tactware.nimbus.appwide.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.appwide.ui.main.MainViewModel
import net.tactware.nimbus.appwide.ui.theme.AppTheme
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.appwide.ui.NotificationIcon
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.ui.ShowProjects
import org.koin.compose.viewmodel.koinViewModel

// Data class for navigation items
data class NavItem(
    val title: String,
    val icon: ImageVector,
    val contentDescription: String
)

@Composable
fun App() {
    AppTheme {
        val mainViewModel = koinViewModel<MainViewModel>()
        val state = mainViewModel.uiState.collectAsState().value

        // State for selected navigation item
        var selectedNavItem by remember { mutableStateOf(0) }

        // State for navigation expansion
        var isNavExpanded by remember { mutableStateOf(false) }

        // State for navigation expansion
        var showNavItemTitles by remember { mutableStateOf(false) }

        LaunchedEffect(isNavExpanded) {
            if (isNavExpanded) {
                delay(200)
            }
            showNavItemTitles = isNavExpanded
        }

        // Navigation items
        val navItems = listOf(
            NavItem("Dashboard", Icons.Default.Home, "Dashboard"),
            NavItem("Projects", Icons.Default.Build, "Projects"),
            NavItem("Work Items", Icons.Default.PlayArrow, "Work Items"),
            NavItem("Settings", Icons.Default.Settings, "Settings")
        )

        Scaffold(
            // Modern dashboard doesn't need a bottom bar
        ) { innerPadding ->
            Row(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                // Left sidebar navigation - expandable/collapsible with animation
                val navWidth by animateDpAsState(
                    targetValue = if (isNavExpanded) 200.dp else 56.dp,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                    label = "navWidth"
                )

                Surface(
                    modifier = Modifier.width(navWidth).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight().padding(vertical = MaterialTheme.spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium, Alignment.Top)
                    ) {
                        // App logo or icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "N",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        // Navigation items
                        navItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.spacing.small)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedNavItem == index) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { selectedNavItem = index }
                                    .padding(MaterialTheme.spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    item.icon,
                                    contentDescription = item.contentDescription,
                                    tint = if (selectedNavItem == index) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )

                                // Animate the text visibility
                                AnimatedVisibility(
                                    visible = showNavItemTitles,
                                    enter = expandHorizontally(animationSpec = tween(durationMillis = 400,)),
                                    exit = shrinkHorizontally(animationSpec = tween(durationMillis = 100, ))
                                ) {
                                    Row {
                                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                        Text(
                                            item.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (selectedNavItem == index) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // Main content area
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small)
                    ) {
                        // Header with title, search, and user profile - smaller and more compact
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.small).height(48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Row for title and toggle button
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Toggle button that straddles the navigation bar
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary) // Different color
                                        .clickable { isNavExpanded = !isNavExpanded },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Animate the rotation of the icon
                                    val rotation by animateFloatAsState(
                                        targetValue = if (isNavExpanded) 0f else 180f,
                                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                                        label = "iconRotation"
                                    )

                                    Icon(
                                        Icons.Default.ArrowBack, // Always use ArrowBack, but rotate it
                                        contentDescription = if (isNavExpanded) "Collapse Navigation" else "Expand Navigation",
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(20.dp).rotate(rotation)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    navItems[selectedNavItem].title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                            ) {
                                // Search box
                                Surface(
                                    modifier = Modifier.width(240.dp).height(40.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = MaterialTheme.spacing.small),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                        Text(
                                            "Search...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                // Notification icon
                                NotificationIcon()

                                // User profile
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "User Profile",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Content based on selected nav item
                        // Track if we're navigating to Projects to add a new project
                        var showAddProject by remember { mutableStateOf(false) }

                        when (selectedNavItem) {
                            0 -> DashboardContent(state, onNavigateToProjects = { 
                                showAddProject = true
                                selectedNavItem = 1 
                            })
                            1 -> {
                                ProjectsContent(state, showAddProject = showAddProject)
                                // Reset after navigation
                                if (showAddProject) {
                                    showAddProject = false
                                }
                            }
                            2 -> WorkItemsContent()
                            3 -> SettingsContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        Text(
            "Settings page is under construction",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun WorkItemsContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Work Items",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        Text(
            "Work items page is under construction",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Composable
fun DashboardContent(state: MainViewModel.UiState, onNavigateToProjects: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // Projects summary card
            Card(
                modifier = Modifier.weight(1f).height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Projects",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        when (state) {
                            is MainViewModel.UiState.LoadedProjects -> "${state.projects.size}"
                            else -> "0"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Repositories summary card
            Card(
                modifier = Modifier.weight(1f).height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Repositories",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        "12", // Placeholder value
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Work items summary card
            Card(
                modifier = Modifier.weight(1f).height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Work Items",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Text(
                        "24", // Placeholder value
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Recent projects section
        Text(
            "Recent Projects",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        )

        when (state) {
            MainViewModel.UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error loading projects")
                    Button(onClick = {}) {
                        Text("Retry")
                    }
                }
            }

            is MainViewModel.UiState.LoadedProjects -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    items(state.projects) { project ->
                        ProjectCard(project)
                    }
                    item {
                        AddProjectCard(onAddProject = onNavigateToProjects)
                    }
                }
            }

            MainViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ProjectCard(project: ProjectIdentifier) {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                project.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "5 repos", // Placeholder
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "12 items", // Placeholder
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectsContent(state: MainViewModel.UiState, showAddProject: Boolean = false) {
    when (state) {
        MainViewModel.UiState.Error -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error loading projects")
                Button(onClick = {}) {
                    Text("Retry")
                }
            }
        }

        is MainViewModel.UiState.LoadedProjects -> {
            ShowProjects(projects = state.projects, showAddProject = showAddProject)
        }

        MainViewModel.UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun AddProjectCard(onAddProject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onAddProject() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = "Add Project",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Text(
                "Add New Project",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
