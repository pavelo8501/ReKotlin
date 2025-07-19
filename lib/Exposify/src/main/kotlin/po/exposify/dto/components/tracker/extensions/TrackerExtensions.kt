package po.exposify.dto.components.tracker.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.context.Identifiable
import po.misc.lookups.HierarchyNode

fun TrackableDTO.collectTrackerTree(): TrackableDTONode {
    val rootNode = TrackableDTONode(this)
    collectChildNodesInto(rootNode)
    return rootNode
}

private fun collectChildNodesInto(parentNode: TrackableDTONode) {
    for (child in parentNode.dto.childTrackers) {
        val childNode = TrackableDTONode(child)
        parentNode.children.add(childNode)
        collectChildNodesInto(childNode)
    }
}

data class TrackableDTONode(
    val dto: TrackableDTO,
    val children: MutableList<TrackableDTONode> = mutableListOf()
)


fun <DTO: ModelDTO, D: DataModel, E: LongEntity> CommonDTO<DTO, D, E>.addTrackerInfo(
    operation:CrudOperation,
    moduleName: Identifiable
):CommonDTO<DTO, D, E>{
    tracker.addTrackInfo(operation)
    return this
}

fun <DTO: ModelDTO, D: DataModel, E : LongEntity> CommonDTO<DTO, D, E>.addTrackerResult(
    operation:CrudOperation? = null
):CommonDTO<DTO, D, E>{
    tracker.addTrackResult(operation)
    return this
}

fun <DTO: ModelDTO, D: DataModel, E : LongEntity> CommonDTO<DTO, D, E>.resolveHierarchy(): HierarchyNode<ModelDTO> {

    fun traverse(dtos: List<ModelDTO>): List<HierarchyNode<ModelDTO>> {
        return dtos.groupBy { it.typeData }
            .map { (type, group) ->
                val children = group
                    .flatMap { it.hub.relationDelegates.flatMap {
                            delegate -> delegate.getChild().valueAsList() } }

                val child = traverse(children)
                val groupAsList = group.toList()

                HierarchyNode(typeData, groupAsList).addChildNodes(child)
            }
    }
    val rootNode = HierarchyNode<ModelDTO>(this.typeData, mutableListOf(this.asDTO()))
    val firstLevelChildren = this.hub.relationDelegates
        .flatMap { it.getChild().valueAsList() }
    rootNode.children += traverse(firstLevelChildren)
    return rootNode
}
