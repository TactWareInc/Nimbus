package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.bl.ArtifactoryClient
import net.tactware.nimbus.appwide.bl.LoggerStatic
import net.tactware.nimbus.appwide.bl.Package
import net.tactware.nimbus.appwide.bl.PackageVersion
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.dal.entities.Project
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import java.io.File

/**
 * ViewModel for the Artifactory UI screen.
 * Manages state and interactions with the ArtifactoryClient.
 */
@Factory
class ArtifactoryViewModel(
    @InjectedParam
    private val projectIdentifier: ProjectIdentifier,
    private val getProjectByIdUseCase: GetProjectByIdUseCase
) : ViewModel() {

    // Project reference
    private val _project = MutableStateFlow<Project?>(null)
    private val project = _project.asStateFlow()

    // ArtifactoryClient instance
    private var artifactoryClient: ArtifactoryClient? = null

    // Feed ID for Artifactory
    private val _feedId = MutableStateFlow("artifacts")
    val feedId = _feedId.asStateFlow()

    // Packages list
    private val _packages = MutableStateFlow<List<Package>>(emptyList())
    val packages = _packages.asStateFlow()

    // Search state
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()

    private val _filteredPackages = MutableStateFlow<List<Package>>(emptyList())
    val filteredPackages = _filteredPackages.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Download state
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _downloadingPackageId = MutableStateFlow<String?>(null)
    val downloadingPackageId = _downloadingPackageId.asStateFlow()

    // Download dialog state
    private val _showDownloadDialog = MutableStateFlow<Package?>(null)
    val showDownloadDialog = _showDownloadDialog.asStateFlow()

    private val _packageVersions = MutableStateFlow<List<PackageVersion>>(emptyList())
    val packageVersions = _packageVersions.asStateFlow()

    private val _selectedVersion = MutableStateFlow<PackageVersion?>(null)
    val selectedVersion = _selectedVersion.asStateFlow()

    private val _downloadPath = MutableStateFlow("")
    val downloadPath = _downloadPath.asStateFlow()

    init {
        // Load the project and initialize the ArtifactoryClient
        viewModelScope.launch(Dispatchers.Default) {
            val loadedProject = getProjectByIdUseCase.invoke(projectIdentifier.id)
            _project.value = loadedProject

            loadedProject?.let {
                artifactoryClient = ArtifactoryClient(it)
                loadPackages()
            }
        }
    }

    /**
     * Updates the search query and filters packages.
     */
    fun updateSearchQuery(query: String) {
        _searchText.value = query
        _isSearchMode.value = query.isNotBlank()

        if (query.isBlank()) {
            _filteredPackages.value = emptyList()
        } else {
            _filteredPackages.value = _packages.value.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.id.contains(query, ignoreCase = true) ||
                it.protocolType.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Updates the feed ID and reloads packages.
     */
    fun updateFeedId(feedId: String) {
        if (feedId.isNotBlank() && feedId != _feedId.value) {
            _feedId.value = feedId
            loadPackages()
        }
    }

    /**
     * Loads packages from the Artifactory feed.
     */
    private fun loadPackages() {
        viewModelScope.launch(Dispatchers.Default) {
            _isLoading.value = true
            try {
                artifactoryClient?.let { client ->
                    val packages = client.listPackages(_feedId.value)
                    _packages.value = packages

                    // Reset search if active
                    if (_isSearchMode.value) {
                        updateSearchQuery(_searchText.value)
                    }
                }
            } catch (e: Exception) {
                LoggerStatic.e("ArtifactoryViewModel", "Error loading packages: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Shows the download dialog for a package and loads its versions.
     */
    fun showDownloadDialog(pkg: Package) {
        viewModelScope.launch(Dispatchers.Default) {
            _showDownloadDialog.value = pkg
            _packageVersions.value = emptyList()
            _selectedVersion.value = null

            // Set default download path
            val defaultPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + pkg.name
            _downloadPath.value = defaultPath

            // Load package versions
            try {
                artifactoryClient?.let { client ->
                    val versions = client.listPackageVersions(_feedId.value, pkg.id)
                    _packageVersions.value = versions

                    // Select latest version by default
                    versions.find { it.isLatest }?.let { latestVersion ->
                        _selectedVersion.value = latestVersion
                    } ?: run {
                        if (versions.isNotEmpty()) {
                            _selectedVersion.value = versions.first()
                        }
                    }
                }
            } catch (e: Exception) {
                LoggerStatic.e("ArtifactoryViewModel", "Error loading package versions: ${e.message}")
            }
        }
    }

    /**
     * Dismisses the download dialog.
     */
    fun dismissDownloadDialog() {
        _showDownloadDialog.value = null
        _packageVersions.value = emptyList()
        _selectedVersion.value = null
        _downloadPath.value = ""
    }

    /**
     * Selects a version for download.
     */
    fun selectVersion(version: PackageVersion) {
        _selectedVersion.value = version
    }

    /**
     * Updates the download path.
     */
    fun updateDownloadPath(path: String) {
        _downloadPath.value = path
    }

    /**
     * Downloads the selected package version.
     */
    fun downloadPackage() {
        val pkg = _showDownloadDialog.value ?: return
        val version = _selectedVersion.value ?: return
        val path = _downloadPath.value

        if (path.isBlank()) return

        viewModelScope.launch(Dispatchers.Default) {
            _isDownloading.value = true
            _downloadingPackageId.value = pkg.id

            try {
                artifactoryClient?.let { client ->
                    val success = client.downloadPackage(_feedId.value, pkg.id, version.version, path)

                    if (success) {
                        LoggerStatic.i("ArtifactoryViewModel", "Package downloaded successfully to $path")
                        dismissDownloadDialog()
                    } else {
                        LoggerStatic.e("ArtifactoryViewModel", "Failed to download package")
                    }
                }
            } catch (e: Exception) {
                LoggerStatic.e("ArtifactoryViewModel", "Error downloading package: ${e.message}")
            } finally {
                _isDownloading.value = false
                _downloadingPackageId.value = null
            }
        }
    }
}
