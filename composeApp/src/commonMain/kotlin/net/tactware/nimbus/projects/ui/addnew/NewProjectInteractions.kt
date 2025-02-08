package net.tactware.nimbus.projects.ui.addnew

import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService

internal sealed class NewProjectInteractions {

    data class NameProject(val name: String) : NewProjectInteractions()

    data class SetIsServerOrService(val isServer: DevOpsServerOrService) : NewProjectInteractions()

    data class UrlProject(val url: String) : NewProjectInteractions()

    data class PAT(val personalAccessToken : String) : NewProjectInteractions()

    data object SaveProject : NewProjectInteractions()


}