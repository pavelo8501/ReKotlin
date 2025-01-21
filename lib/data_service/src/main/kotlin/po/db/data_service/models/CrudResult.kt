package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.components.eventhandler.models.Event
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.dto.CommonDTO
import po.db.data_service.dto.HostDTO


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