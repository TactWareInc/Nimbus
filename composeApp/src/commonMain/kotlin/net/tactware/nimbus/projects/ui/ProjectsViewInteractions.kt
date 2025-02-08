package net.tactware.nimbus.projects.ui

sealed class ProjectsViewInteractions {

    data class SelectProject(val project: String, val index : Int) : ProjectsViewInteractions()

    data object AddProject : ProjectsViewInteractions()
}
