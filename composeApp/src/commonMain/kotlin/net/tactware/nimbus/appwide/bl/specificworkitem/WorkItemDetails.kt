package net.tactware.nimbus.appwide.bl.specificworkitem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemDetails(
    @SerialName("id") val id: Int? = null,
    @SerialName("rev") val rev: Int? = null,
    @SerialName("fields") val fields: Fields? = null,
    @SerialName("relations") val relations: List<Relation>? = null,
    @SerialName("_links") val links: WorkItemLinks? = null,
    @SerialName("url") val url: String? = null
)

@Serializable
data class Fields(
    @SerialName("System.Id") val systemId: Int? = null,
    @SerialName("System.AreaId") val systemAreaId: Int? = null,
    @SerialName("System.AreaPath") val systemAreaPath: String? = null,
    @SerialName("System.TeamProject") val systemTeamProject: String? = null,
    @SerialName("System.NodeName") val systemNodeName: String? = null,
    @SerialName("System.AreaLevel1") val systemAreaLevel1: String? = null,
    @SerialName("System.Rev") val systemRev: Int? = null,
    @SerialName("System.AuthorizedDate") val systemAuthorizedDate: String? = null,
    @SerialName("System.RevisedDate") val systemRevisedDate: String? = null,
    @SerialName("System.IterationId") val systemIterationId: Int? = null,
    @SerialName("System.IterationPath") val systemIterationPath: String? = null,
    @SerialName("System.IterationLevel1") val systemIterationLevel1: String? = null,
    @SerialName("System.WorkItemType") val systemWorkItemType: String? = null,
    @SerialName("System.State") val systemState: String? = null,
    @SerialName("System.Reason") val systemReason: String? = null,
    @SerialName("System.AssignedTo") val systemAssignedTo: IdentityRef? = null,
    @SerialName("System.CreatedDate") val systemCreatedDate: String? = null,
    @SerialName("System.CreatedBy") val systemCreatedBy: IdentityRef? = null,
    @SerialName("System.ChangedDate") val systemChangedDate: String? = null,
    @SerialName("System.ChangedBy") val systemChangedBy: IdentityRef? = null,
    @SerialName("System.AuthorizedAs") val systemAuthorizedAs: IdentityRef? = null,
    @SerialName("System.PersonId") val systemPersonId: Int? = null,
    @SerialName("System.Watermark") val systemWatermark: Int? = null,
    @SerialName("System.CommentCount") val systemCommentCount: Int? = null,
    @SerialName("System.Title") val systemTitle: String? = null,
    @SerialName("Microsoft.VSTS.Common.StateChangeDate") val stateChangeDate: String? = null,
    @SerialName("Microsoft.VSTS.Common.ActivatedDate") val activatedDate: String? = null,
    @SerialName("Microsoft.VSTS.Common.ActivatedBy") val activatedBy: IdentityRef? = null,
    @SerialName("Microsoft.VSTS.Common.Priority") val priority: Int? = null,
    @SerialName("Microsoft.VSTS.TCM.AutomationStatus") val automationStatus: String? = null,
    @SerialName("Microsoft.VSTS.TCM.Steps") val steps: String? = null,
    @SerialName("System.Description") val description: String? = null
)

@Serializable
data class IdentityRef(
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("_links") val links: IdentityRefLinks? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("uniqueName") val uniqueName: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("inactive") val inactive: Boolean? = null,
    @SerialName("descriptor") val descriptor: String? = null
)

@Serializable
data class IdentityRefLinks(
    @SerialName("avatar") val avatar: Avatar? = null
)

@Serializable
data class Avatar(
    @SerialName("href") val href: String? = null
)

@Serializable
data class Relation(
    @SerialName("rel") val rel: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("attributes") val attributes: RelationAttributes? = null
)

@Serializable
data class RelationAttributes(
    @SerialName("authorizedDate") val authorizedDate: String? = null,
    @SerialName("id") val id: Int? = null,
    @SerialName("resourceCreatedDate") val resourceCreatedDate: String? = null,
    @SerialName("resourceModifiedDate") val resourceModifiedDate: String? = null,
    @SerialName("revisedDate") val revisedDate: String? = null,
    @SerialName("name") val name: String? = null
)

@Serializable
data class WorkItemLinks(
    @SerialName("self") val self: HrefOnly? = null,
    @SerialName("workItemUpdates") val workItemUpdates: HrefOnly? = null,
    @SerialName("workItemRevisions") val workItemRevisions: HrefOnly? = null,
    @SerialName("workItemComments") val workItemComments: HrefOnly? = null,
    @SerialName("html") val html: HrefOnly? = null,
    @SerialName("workItemType") val workItemType: HrefOnly? = null,
    @SerialName("fields") val fields: HrefOnly? = null
)

@Serializable
data class HrefOnly(
    @SerialName("href") val href: String? = null
)