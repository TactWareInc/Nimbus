package net.tactware.nimbus.gitrepos.dal

/**
 * Data class that combines a git branch with its repository information.
 *
 * @property branch The git branch
 * @property repo The repository that the branch belongs to
 */
data class BranchWithRepo(
    val branch: GitBranch,
    val repo: GitRepo
)