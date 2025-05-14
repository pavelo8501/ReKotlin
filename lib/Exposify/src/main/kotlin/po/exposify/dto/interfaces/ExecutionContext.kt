package po.exposify.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.WhereQuery


interface ExecutionContext<DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity>{

    val providerName : String

    suspend fun  select(): ResultList<DTO, DATA, ENTITY>
    suspend fun <T: IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY>
    suspend fun  select(conditions: SimpleQuery): ResultList<DTO, DATA, ENTITY>

    suspend fun  pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>
    suspend fun  pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>

    suspend fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY>
    suspend fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>

}