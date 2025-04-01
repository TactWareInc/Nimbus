package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.appwide.dal.IDatabaseProvider
import net.tactware.nimbus.db.NimbusDb
import net.tactware.nimbus.gitrepos.dal.GitBranch
import net.tactware.nimbus.gitrepos.dal.GitBranchesRepository
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import net.tactware.nimbus.projects.dal.ProjectsRepository
import org.eclipse.jgit.api.Git
import org.koin.core.annotation.Single
import java.io.File

/**
 * Use case for fetching branches from git repositories.
 */
@Single
class FetchBranchesUseCase(
    private val gitReposRepository: GitReposRepository,
    private val gitBranchesRepository: GitBranchesRepository,
    private val databaseProvider: IDatabaseProvider<NimbusDb>,
    private val projectsRepository: ProjectsRepository
) {
    /**
     * Fetches branches for all repositories associated with a project.
     *
     * @param projectId The ID of the project
     * @return A list of repositories with their branches fetched
     */
    suspend fun fetchBranchesForProject(projectId: String): List<GitRepo> {
        try {
            // Get all repositories for the project directly from the database
            val repos = databaseProvider.database.repositoriesQueries
                .getAllGitReposForProject(projectId)
                .executeAsList()
                .map { gitRepos ->
                    GitRepo(
                        id = gitRepos.id,
                        name = gitRepos.name,
                        url = gitRepos.url,
                        isCloned = gitRepos.is_cloned,
                        clonePath = gitRepos.clone_path
                    )
                }

            // For each repository, fetch its branches if it's cloned
            repos.forEach { repo ->
                if (repo.isCloned && !repo.clonePath.isNullOrBlank()) {
                    val branches = fetchBranchesFromRepo(repo)
                    gitBranchesRepository.updateBranches(repo.id, branches)
                }
            }

            // Only return cloned repositories
            return repos.filter { it.isCloned && !it.clonePath.isNullOrBlank() }
        } catch (e: Exception) {
            // Log the error
            println("Error fetching branches for project: ${e.message}")

            // Send a notification to the user about the error
            NotificationService.addNotification(
                title = "Failed to Fetch Branches",
                message = "Failed to fetch branches for project: ${e.message ?: "Unknown error"}"
            )

            return emptyList()
        }
    }

    /**
     * Fetches branches for a specific repository.
     *
     * @param repoId The ID of the repository
     * @param projectId The ID of the project that the repository belongs to
     * @return The repository with its branches fetched, or null if the repository doesn't exist
     */
    suspend fun fetchBranchesForRepo(repoId: Long, projectId: String): GitRepo? {
        try {
            // Get all repositories for the project directly from the database
            val repos = databaseProvider.database.repositoriesQueries
                .getAllGitReposForProject(projectId)
                .executeAsList()
                .map { gitRepos ->
                    GitRepo(
                        id = gitRepos.id,
                        name = gitRepos.name,
                        url = gitRepos.url,
                        isCloned = gitRepos.is_cloned,
                        clonePath = gitRepos.clone_path
                    )
                }

            // Find the repository with the specified ID
            val repo = repos.find { it.id == repoId } ?: return null

            // Only proceed if the repository is cloned
            if (!repo.isCloned || repo.clonePath.isNullOrBlank()) {
                return null
            }

            // Fetch branches
            val branches = fetchBranchesFromRepo(repo)
            gitBranchesRepository.updateBranches(repo.id, branches)

            return repo
        } catch (e: Exception) {
            // Log the error
            println("Error fetching branches for repository: ${e.message}")

            // Send a notification to the user about the error
            NotificationService.addNotification(
                title = "Failed to Fetch Branches",
                message = "Failed to fetch branches for repository: ${e.message ?: "Unknown error"}"
            )

            return null
        }
    }

    /**
     * Fetches branches from a git repository using JGit.
     *
     * @param repo The repository to fetch branches from
     * @return A list of branches in the repository
     */
    private suspend fun fetchBranchesFromRepo(repo: GitRepo): List<GitBranch> = withContext(Dispatchers.IO) {
        try {
            val branches = mutableListOf<GitBranch>()

            // Open the repository
            val repoDir = File(repo.clonePath!!)
            Git.open(repoDir).use { git ->
                // Get the current branch
                val currentBranch = git.repository.branch

                // Get all branches (both local and remote)
                val branchRefs = git.branchList().setListMode(org.eclipse.jgit.api.ListBranchCommand.ListMode.ALL).call()

                // Convert JGit refs to our GitBranch model
                branches.addAll(branchRefs.map { ref ->
                    val name = ref.name
                    val isRemote = name.startsWith("refs/remotes/")
                    val branchName = when {
                        name.startsWith("refs/heads/") -> name.removePrefix("refs/heads/")
                        name.startsWith("refs/remotes/") -> {
                            val remoteParts = name.removePrefix("refs/remotes/").split("/", limit = 2)
                            if (remoteParts.size > 1) "${remoteParts[0]}/${remoteParts[1]}" else name
                        }
                        else -> name
                    }
                    GitBranch(
                        name = branchName,
                        isCurrent = !isRemote && branchName == currentBranch,
                        repoId = repo.id,
                        isRemote = isRemote
                    )
                })
            }

            branches
        } catch (e: Exception) {
            // Log the error
            println("Error fetching branches from repository: ${e.message}")

            // Return an empty list if there's an error
            emptyList()
        }
    }

    /**
     * Fetches branches for all repositories across all projects in the database.
     *
     * @return A list of all repositories with their branches fetched
     */
    suspend fun fetchBranchesForAllProjects(): List<GitRepo> {
        try {
            val allRepos = mutableListOf<GitRepo>()

            // Get all projects from the database
            val projects = projectsRepository.getProjects()

            // For each project, fetch its repositories and branches
            projects.forEach { project ->
                val projectRepos = fetchBranchesForProject(project.id)
                allRepos.addAll(projectRepos)
            }

            return allRepos
        } catch (e: Exception) {
            // Log the error
            println("Error fetching branches for all projects: ${e.message}")

            // Send a notification to the user about the error
            NotificationService.addNotification(
                title = "Failed to Fetch Branches",
                message = "Failed to fetch branches for all projects: ${e.message ?: "Unknown error"}"
            )

            return emptyList()
        }
    }
}
