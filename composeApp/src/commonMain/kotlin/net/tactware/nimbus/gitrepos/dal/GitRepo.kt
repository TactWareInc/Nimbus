package net.tactware.nimbus.gitrepos.dal

data class GitRepo(
    val id: Long,
    val name: String,
    val url: String,
)