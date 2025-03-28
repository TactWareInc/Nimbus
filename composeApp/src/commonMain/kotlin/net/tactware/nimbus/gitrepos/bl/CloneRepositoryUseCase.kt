package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.eclipse.jgit.api.Git
import org.koin.core.annotation.Factory
import java.io.File

/**
 * Use case for cloning a Git repository to a local directory using JGit.
 */
@Factory
class CloneRepositoryUseCase(
    private val getProjectByIdUseCase: GetProjectByIdUseCase
) {

    /**
     * Clones a Git repository to the specified directory using JGit.
     * 
     * @param repo The Git repository to clone
     * @param directory The directory to clone the repository to
     * @param projectIdentifier The identifier of the project that the repository belongs to
     * @return Result indicating success or failure with an error message
     */
    suspend operator fun invoke(repo: GitRepo, directory: String, projectIdentifier: ProjectIdentifier? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Create a File object for the target directory
            val targetDir = File(directory, repo.name)

            // Ensure the parent directory exists
            targetDir.parentFile?.mkdirs()

            // Clone the repository using JGit
            val cloneCommand = Git.cloneRepository()
                .setURI(repo.url)
                .setDirectory(targetDir)

            // Add authentication if project identifier is provided
            if (projectIdentifier != null) {
                // Get the project to access its PAT
                val project = getProjectByIdUseCase(projectIdentifier.id)

                // Add authentication if project is found and has a PAT
                if (project != null && project.personalAccessToken.isNotBlank()) {
                    // For GitHub, GitLab, etc., PAT can be used as password with any username
                    cloneCommand.setCredentialsProvider(
                        org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider("oauth2", project.personalAccessToken)
                    )
                }
            }

            cloneCommand.call().close()

            // Return success
            Result.success(Unit)
        } catch (e: Exception) {
            // Return failure with the error message
            Result.failure(e)
        }
    }
}
