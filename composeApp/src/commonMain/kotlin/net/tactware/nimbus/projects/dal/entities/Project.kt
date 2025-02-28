package net.tactware.nimbus.projects.dal.entities


data class Project(
    val id: String,
    val projectUrl: String,
    val name: String,
    val isServerOrService: DevOpsServerOrService,
    val personalAccessToken: String,
) {
    val orgOrCollection: String
        get() {
            val parts = projectUrl.split("/")
            return if (projectUrl.endsWith("/")) {
                parts[parts.size - 3]
            } else {
                parts[parts.size - 2]
            }
        }

    val orgOrCollectionUrl: String
        get() {
            val parts = projectUrl.split("/")
            return if (projectUrl.endsWith("/")) {
                projectUrl.substring(0, projectUrl.length - parts[parts.size - 2].length - 1)
            } else {
                projectUrl.substring(0, projectUrl.length - parts[parts.size - 1].length)
            }
        }

    val projectName: String
        get() {
            val parts = projectUrl.split("/")
            return parts.filter { it.isNotEmpty() }.let { it[it.size - 1] }
        }
}
