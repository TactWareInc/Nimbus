package net.tactware.nimbus.projects.dal.entities

import kotlin.test.Test


class ProjectTest {

    @Test
    fun testEntity() {
        val project = Project(
            id = "1",
            projectUrl = "https://dev.azure.com/TactWare/SampleProject/",
            name = "Project 1",
            isServerOrService = DevOpsServerOrService.SERVER,
            personalAccessToken = "token",
        )

        assert(project.id == "1")
        assert(project.name == "Project 1")
        assert(project.projectUrl == "https://dev.azure.com/TactWare/SampleProject/")
        assert(project.projectName == "SampleProject")
        assert(project.personalAccessToken == "token")
        assert(project.orgOrCollectionUrl == "https://dev.azure.com/TactWare/")
        assert(project.orgOrCollection == "TactWare")
    }
}