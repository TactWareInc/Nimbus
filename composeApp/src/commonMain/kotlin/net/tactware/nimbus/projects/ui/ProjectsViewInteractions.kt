package net.tactware.nimbus.projects.ui

import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier

sealed class ProjectsViewInteractions {

    data class SelectProject(val project: ProjectIdentifier, val index : Int) : ProjectsViewInteractions()

    data object AddProject : ProjectsViewInteractions()
}
