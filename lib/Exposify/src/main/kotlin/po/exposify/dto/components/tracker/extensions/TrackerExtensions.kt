package po.exposify.dto.components.tracker.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext

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
    moduleName: IdentifiableContext
):CommonDTO<DTO, D, E>{
    tracker.addTrackInfo(operation, moduleName)
    return this
}

fun <DTO: ModelDTO, D: DataModel, E : LongEntity> CommonDTO<DTO, D, E>.addTrackerResult(
    operation:CrudOperation? = null
):CommonDTO<DTO, D, E>{
    tracker.addTrackResult(operation)
    return this
}
