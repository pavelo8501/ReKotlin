package po.exposify.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.DTOBase
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler


interface ExecutionContext<DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity> : TasksManaged{

    val dtoClass : DTOBase<DTO, DATA, ENTITY>
    val logger : TaskHandler<*>

    fun  select(): ResultList<DTO, DATA, ENTITY>
    fun <T: IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY>
    fun  select(conditions: SimpleQuery): ResultList<DTO, DATA, ENTITY>

    fun  pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>
    fun  pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>

    fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY>

    fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>

}