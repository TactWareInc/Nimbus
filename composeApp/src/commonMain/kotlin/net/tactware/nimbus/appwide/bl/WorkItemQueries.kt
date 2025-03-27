package net.tactware.nimbus.appwide.bl

object WorkItemQueries {
     fun items(team : String = "", types : List<String> = emptyList()) = """
        SELECT [System.Id], [System.Parent]
FROM WorkItems
    """.trimIndent()
}