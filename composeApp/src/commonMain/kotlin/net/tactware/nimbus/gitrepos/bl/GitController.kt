package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.gitrepos.dal.BranchWithRepo
import net.tactware.nimbus.gitrepos.dal.GitBranch
import net.tactware.nimbus.gitrepos.dal.GitBranchesRepository
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.koin.core.annotation.Single
import java.io.File

/**
 * A centralized controller for Git operations.
 * This controller provides methods for interacting with Git repositories and branches.
 */
@Single
class GitController(
    private val gitBranchesRepository: GitBranchesRepository,
    private val gitReposRepository: GitReposRepository,
    private val cloneRepositoryUseCase: CloneRepositoryUseCase,
    private val fetchBranchesUseCase: FetchBranchesUseCase,
    private val switchBranchUseCase: SwitchBranchUseCase,
    private val linkExistingRepositoryUseCase: LinkExistingRepositoryUseCase,
    private val repositoryDownloadTracker: RepositoryDownloadTracker
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Fetches branches for a specific project.
     *
     * @param projectId The ID of the project
     * @return A list of repositories with their branches
     */
    suspend fun fetchBranchesForProject(projectId: String): List<GitRepo> {
        return fetchBranchesUseCase.fetchBranchesForProject(projectId)
    }

    /**
     * Fetches branches for all projects.
     *
     * @return A list of repositories with their branches
     */
    suspend fun fetchBranchesForAllProjects(): List<GitRepo> {
        return fetchBranchesUseCase.fetchBranchesForAllProjects()
    }

    /**
     * Fetches branches for a specific repository.
     *
     * @param repoId The ID of the repository
     * @param projectId The ID of the project
     * @return The repository with its branches fetched, or null if the repository doesn't exist
     */
    suspend fun fetchBranchesForRepo(repoId: Long, projectId: String): GitRepo? {
        return fetchBranchesUseCase.fetchBranchesForRepo(repoId, projectId)
    }

    /**
     * Switches to a branch in a repository.
     *
     * @param branchWithRepo The branch and repository to switch to
     * @return True if the switch was successful, false otherwise
     */
    suspend fun switchBranch(branchWithRepo: BranchWithRepo): Boolean {
        return switchBranchUseCase.switchBranch(branchWithRepo.repo.id, branchWithRepo.branch.name)
    }

    /**
     * Creates a new branch in one or more repositories.
     *
     * @param branchName The name of the branch to create
     * @param repos The repositories to create the branch in
     * @param pushToRemote Whether to push the branch to the remote repository
     * @param autoCheckout Whether to automatically check out the branch after creating it
     * @return True if the branch was created successfully in all repositories, false otherwise
     */
    suspend fun createBranch(
        branchName: String,
        repos: List<GitRepo>,
        pushToRemote: Boolean = false,
        autoCheckout: Boolean = false
    ): Boolean {
        var allSuccessful = true

        repos.forEach { repo ->
            val repoPath = repo.clonePath
            if (repoPath == null) {
                allSuccessful = false
                return@forEach
            }

            val repoFile = File(repoPath)
            if (!repoFile.exists()) {
                allSuccessful = false
                return@forEach
            }

            try {
                withContext(Dispatchers.IO) {
                    val git = Git.open(repoFile)

                    // Create the branch
                    git.branchCreate()
                        .setName(branchName)
                        .call()

                    // Checkout the branch if requested
                    if (autoCheckout) {
                        git.checkout()
                            .setName(branchName)
                            .call()
                    }

                    // Push to remote if requested
                    if (pushToRemote) {
                        git.push()
                            .setRemote("origin")
                            .setRefSpecs(RefSpec("refs/heads/$branchName:refs/heads/$branchName"))
                            .call()
                    }

                    // Refresh branches
                    fetchBranchesForRepo(repo.id, "")
                }
            } catch (e: Exception) {
                allSuccessful = false
            }
        }

        return allSuccessful
    }

    /**
     * Clones a repository.
     *
     * @param repo The repository to clone
     * @param directory The directory to clone the repository to
     * @param projectIdentifier The identifier of the project that the repository belongs to
     * @param customName Optional custom name for the cloned repository
     * @return True if the clone was successful, false otherwise
     */
    suspend fun cloneRepository(
        repo: GitRepo,
        directory: String,
        projectIdentifier: ProjectIdentifier? = null,
        customName: String? = null
    ): Boolean {
        return cloneRepositoryUseCase(repo, directory, projectIdentifier, customName).isSuccess
    }

    /**
     * Links an existing repository.
     *
     * @param repo The Git repository to link
     * @param localPath The local path of the repository
     * @return Result indicating success or failure with an error message
     */
    suspend fun linkExistingRepository(repo: GitRepo, localPath: String): Result<Unit> {
        return linkExistingRepositoryUseCase(repo, localPath)
    }

    /**
     * Gets the IDs of repositories that are currently being downloaded.
     *
     * @return A set of repository IDs that are being downloaded
     */
    fun getDownloadingRepoIds(): Set<Long> {
        return repositoryDownloadTracker.downloadingRepoIds.value
    }

    /**
     * Checks if a repository is currently being downloaded.
     *
     * @param repoId The ID of the repository
     * @return True if the repository is being downloaded, false otherwise
     */
    fun isDownloading(repoId: Long): Boolean {
        return repositoryDownloadTracker.isDownloading(repoId)
    }

    /**
     * Stops tracking a repository as being downloaded.
     *
     * @param repoId The ID of the repository to stop tracking
     */
    fun stopDownloading(repoId: Long) {
        repositoryDownloadTracker.stopDownloading(repoId)
    }

    /**
     * Starts tracking a repository as being downloaded.
     *
     * @param repoId The ID of the repository to start tracking
     */
    fun startDownloading(repoId: Long) {
        repositoryDownloadTracker.startDownloading(repoId)
    }
}
