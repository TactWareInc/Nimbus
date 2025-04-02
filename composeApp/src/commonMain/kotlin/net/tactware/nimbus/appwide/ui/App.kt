
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import net.tactware.nimbus.appwide.ui.main.MainViewModel
import net.tactware.nimbus.appwide.ui.theme.AppTheme
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.appwide.ui.profile.ProfilePage
import net.tactware.nimbus.appwide.ui.settings.SettingsContent
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.ui.ShowProjects

import net.tactware.nimbus.projects.dal.ProjectsRepository
import org.koin.compose.koinInject
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
        var causeNavigationToExpand by remember { mutableStateOf(false) }

        // State for navigation expansion
        var showNavItemTitles by remember { mutableStateOf(false) }
        var expandColumn by remember { mutableStateOf(false) }


        // State for showing profile page
        var showProfilePage by remember { mutableStateOf(false) }

        LaunchedEffect(causeNavigationToExpand) {
            if (causeNavigationToExpand) {
                delay(200)
            }
            showNavItemTitles = causeNavigationToExpand
        }

        LaunchedEffect(causeNavigationToExpand) {
            if (!causeNavigationToExpand) {
                delay(200)
            }
            expandColumn = causeNavigationToExpand
        }

        // Navigation items
        val navItems = listOf(
            NavItem("Dashboard", Icons.Default.Home, "Dashboard"),
            NavItem("Projects", Icons.Default.Build, "Projects"),
            NavItem("Work Items", Icons.Default.PlayArrow, "Work Items"),
            NavItem("Git Branches", Icons.Default.Search, "Git Branches"),
            NavItem("Build Agents", Icons.Default.Build, "Build Agents"),
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
                    targetValue = if (expandColumn) 200.dp else 56.dp,
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
                                        .clickable { causeNavigationToExpand = !causeNavigationToExpand },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Animate the rotation of the icon
                                    val rotation by animateFloatAsState(
                                        targetValue = if (causeNavigationToExpand) 0f else 180f,
                                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                                        label = "iconRotation"
                                    )

                                    Icon(
                                        Icons.Default.ArrowBack, // Always use ArrowBack, but rotate it
                                        contentDescription = if (causeNavigationToExpand) "Collapse Navigation" else "Expand Navigation",
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
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable { showProfilePage = !showProfilePage },
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

                        // Content based on selected nav item or profile page
                        // Track if we're navigating to Projects to add a new project
                        var showAddProject by remember { mutableStateOf(false) }

                        if (showProfilePage) {
                            // Show profile page
                            ProfilePage()
                        } else {
                            // Show regular content based on selected nav item
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
                                3 -> net.tactware.nimbus.gitrepos.ui.GitBranchesUi()
                                4 -> net.tactware.nimbus.buildagents.ui.BuildAgentsUi()
                                5 -> SettingsContent()
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun WorkItemsContent() {
    // Get the first project to use for the WorkItemListDetailPage
    val viewModel = koinViewModel<MainViewModel>()
    val state = viewModel.uiState.collectAsState().value

    // State for view mode (true = list detail mode, false = filter mode)
    var isListDetailMode by remember { mutableStateOf(true) }

    when (state) {
        is MainViewModel.UiState.LoadedProjects -> {
            val projects = state.projects
            if (projects.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Toggle button row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View Mode:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                        )

                        // Filter mode button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp, 0.dp, 0.dp, 8.dp))
                                .clickable { isListDetailMode = false },
                            color = if (!isListDetailMode) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Filter Mode",
                                    tint = if (!isListDetailMode) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Filter",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (!isListDetailMode) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // List detail mode button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp))
                                .clickable { isListDetailMode = true },
                            color = if (isListDetailMode) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "List Mode",
                                    tint = if (isListDetailMode) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Details",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isListDetailMode) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Content based on selected mode
                    Box(modifier = Modifier.weight(1f)) {
                        if (isListDetailMode) {
                            // List detail mode
                            net.tactware.nimbus.projects.ui.specific.WorkItemListDetailPage(
                                projectIdentifier = projects.first(),
                                onNavigateBack = { /* No-op, we're in the main navigation */ },
                                onNavigateToCreateWorkItem = { /* Navigate to create work item page */ }
                            )
                        } else {
                            // Filter mode
                            net.tactware.nimbus.projects.ui.specific.WorkItemsUi(
                                projectIdentifier = projects.first(),
                                onNavigateToCreateWorkItem = { /* Navigate to create work item page */ }
                            )
                        }
                    }
                }
            } else {
                // No projects available
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No projects available. Please add a project first.")
                }
            }
        }
        else -> {
            // Loading or error state
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
fun DashboardContent(state: MainViewModel.UiState, onNavigateToProjects: () -> Unit) {
    // Get the DashboardViewModel
    val dashboardViewModel = koinViewModel<net.tactware.nimbus.appwide.ui.dashboard.DashboardViewModel>()

    // Collect state from the ViewModel
    val recentBranches by dashboardViewModel.recentBranches.collectAsState()
    val recentWorkItems by dashboardViewModel.recentWorkItems.collectAsState()
    val isLoadingBranches by dashboardViewModel.isLoadingBranches.collectAsState()
    val isLoadingWorkItems by dashboardViewModel.isLoadingWorkItems.collectAsState()

    // Repository and work item counts
    val totalRepositoriesCount by dashboardViewModel.totalRepositoriesCount.collectAsState()
    val totalWorkItemsCount by dashboardViewModel.totalWorkItemsCount.collectAsState()
    val projectRepositoryCounts by dashboardViewModel.projectRepositoryCounts.collectAsState()
    val projectWorkItemCounts by dashboardViewModel.projectWorkItemCounts.collectAsState()
    val isLoadingCounts by dashboardViewModel.isLoadingCounts.collectAsState()

    // Bug creation state
    val bugTitle by dashboardViewModel.bugTitle.collectAsState()
    val bugDescription by dashboardViewModel.bugDescription.collectAsState()
    val isCreatingBug by dashboardViewModel.isCreatingBug.collectAsState()

    // State for showing the create bug dialog
    var showCreateBugDialog by remember { mutableStateOf(false) }

    // Create bug dialog
    if (showCreateBugDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isCreatingBug) {
                    showCreateBugDialog = false
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Bug",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Create Bug",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    OutlinedTextField(
                        value = bugTitle,
                        onValueChange = { dashboardViewModel.updateBugTitle(it) },
                        label = { Text("Bug Title") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreatingBug
                    )

                    OutlinedTextField(
                        value = bugDescription,
                        onValueChange = { dashboardViewModel.updateBugDescription(it) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        enabled = !isCreatingBug
                    )

                    if (isCreatingBug) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating bug...")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        // Call createBug without a coroutine scope
                        // The ViewModel will handle the coroutine internally
                        val bugId = dashboardViewModel.createBug()
                        if (bugId != null) {
                            showCreateBugDialog = false
                        }
                    },
                    enabled = bugTitle.isNotBlank() && !isCreatingBug
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateBugDialog = false },
                    enabled = !isCreatingBug
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary cards and quick actions
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

                    if (isLoadingCounts) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Text(
                            totalRepositoriesCount.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
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

                    if (isLoadingCounts) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    } else {
                        Text(
                            totalWorkItemsCount.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Quick action button for creating a bug
        Button(
            onClick = { showCreateBugDialog = true },
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.medium),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Bug",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Create Bug",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Recent branches section
        Text(
            "Recent Branches",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        )

        if (isLoadingBranches) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (recentBranches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No recent branches found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(recentBranches) { branchWithRepo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    branchWithRepo.branch.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Repository: ${branchWithRepo.repo.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            if (!branchWithRepo.branch.isCurrent) {
                                Button(
                                    onClick = { dashboardViewModel.switchBranch(branchWithRepo) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Checkout")
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Current",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent work items section
        Text(
            "Recent Work Items",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium)
        )

        if (isLoadingWorkItems) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (recentWorkItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No recent work items found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(recentWorkItems) { workItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (workItem.type) {
                                "Bug" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                "Task" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.medium)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    workItem.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = when (workItem.state) {
                                        "Active" -> MaterialTheme.colorScheme.primary
                                        "New" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.tertiary
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        workItem.state,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Type: ${workItem.type} | ID: ${workItem.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Recent projects section
        Text(
            "Recent Projects",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium)
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
                        ProjectCard(
                            project = project,
                            projectRepositoryCounts = projectRepositoryCounts,
                            projectWorkItemCounts = projectWorkItemCounts,
                            isLoadingCounts = isLoadingCounts
                        )
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
fun ProjectCard(
    project: ProjectIdentifier,
    projectRepositoryCounts: Map<ProjectIdentifier, Int>,
    projectWorkItemCounts: Map<ProjectIdentifier, Int>,
    isLoadingCounts: Boolean
) {
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
                    if (isLoadingCounts) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        val repoCount = projectRepositoryCounts[project] ?: 0
                        Text(
                            "$repoCount repos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
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
                    if (isLoadingCounts) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        val workItemCount = projectWorkItemCounts[project] ?: 0
                        Text(
                            "$workItemCount items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
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
