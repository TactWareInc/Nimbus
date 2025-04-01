package net.tactware.nimbus.gitrepos.dal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Repository for managing git branches in memory.
 * This repository maintains an in-memory collection of branches for all repositories.
 */
@Single
class GitBranchesRepository {
    // In-memory storage of branches, grouped by repository ID
    private val _branches = MutableStateFlow<Map<Long, List<GitBranch>>>(emptyMap())
    
    /**
     * Updates the branches for a specific repository.
     * 
     * @param repoId The ID of the repository
     * @param branches The list of branches for the repository
     */
    suspend fun updateBranches(repoId: Long, branches: List<GitBranch>) {
        val currentBranches = _branches.value.toMutableMap()
        currentBranches[repoId] = branches
        _branches.value = currentBranches
    }
    
    /**
     * Gets all branches for a specific repository.
     * 
     * @param repoId The ID of the repository
     * @return A flow of the list of branches for the repository
     */
    fun getBranchesByRepoId(repoId: Long): Flow<List<GitBranch>> {
        return _branches.asStateFlow().map { it[repoId] ?: emptyList() }
    }
    
    /**
     * Gets all branches for all repositories.
     * 
     * @return A flow of all branches, grouped by repository ID
     */
    fun getAllBranches(): Flow<Map<Long, List<GitBranch>>> {
        return _branches.asStateFlow()
    }
    
    /**
     * Gets the current branch for a specific repository.
     * 
     * @param repoId The ID of the repository
     * @return A flow of the current branch for the repository, or null if no current branch is set
     */
    fun getCurrentBranch(repoId: Long): Flow<GitBranch?> {
        return _branches.asStateFlow().map { branches ->
            branches[repoId]?.find { it.isCurrent }
        }
    }
    
    /**
     * Sets the current branch for a specific repository.
     * 
     * @param repoId The ID of the repository
     * @param branchName The name of the branch to set as current
     */
    suspend fun setCurrentBranch(repoId: Long, branchName: String) {
        val currentBranches = _branches.value.toMutableMap()
        val repoBranches = currentBranches[repoId]?.toMutableList() ?: mutableListOf()
        
        // Update the current branch
        val updatedBranches = repoBranches.map { branch ->
            branch.copy(isCurrent = branch.name == branchName)
        }
        
        currentBranches[repoId] = updatedBranches
        _branches.value = currentBranches
    }
    
    /**
     * Clears all branches for a specific repository.
     * 
     * @param repoId The ID of the repository
     */
    suspend fun clearBranches(repoId: Long) {
        val currentBranches = _branches.value.toMutableMap()
        currentBranches.remove(repoId)
        _branches.value = currentBranches
    }
    
    /**
     * Clears all branches for all repositories.
     */
    suspend fun clearAllBranches() {
        _branches.value = emptyMap()
    }
}