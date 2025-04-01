package net.tactware.nimbus.gitrepos.dal

/**
 * Represents a git branch in a repository.
 *
 * @property name The name of the branch
 * @property isCurrent Whether this branch is the current/active branch in the repository
 * @property repoId The ID of the repository this branch belongs to
 * @property isRemote Whether this branch is a remote branch
 */
data class GitBranch(
    val name: String,
    val isCurrent: Boolean = false,
    val repoId: Long,
    val isRemote: Boolean = false
)
