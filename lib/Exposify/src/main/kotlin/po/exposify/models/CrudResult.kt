package po.exposify.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.components.eventhandler.models.Event
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.HostDTO


interface OperationResult {
    fun isSuccess(): Boolean
}

abstract class AbstractOperationResult : OperationResult {
    override fun isSuccess(): Boolean = true
}

data class CrudHostedResult<DATA, ENTITY>(
    val rootDTOs: List<HostDTO<DATA, ENTITY, *, *>>,
    val event: Event?
) : AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity



data class CrudResult<DATA, ENTITY>(
    val rootDTOs: List<CommonDTO<DATA, ENTITY>>,
    val event: Event?
) : AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity