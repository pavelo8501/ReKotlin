package po.exposify.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.Query
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.WhereQuery


typealias ChildClass <C_DTO, CD> = DTOClass<C_DTO, CD>

interface ExecutionContext<DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity>{

   // val hostingDTO: CommonDTO<DTO, DATA, ENTITY>

    val providerName : String

    suspend fun <C_DTO: ModelDTO, CD : DataModel> select(
        childClass: ChildClass<C_DTO, CD>
    ): ResultList<C_DTO, CD>

    suspend fun <C_DTO: ModelDTO, CD : DataModel, T: IdTable<Long>> select(
        childClass: ChildClass<C_DTO, CD>,
        conditions: WhereQuery<T>
    ): ResultList<C_DTO, CD>

    suspend fun <C_DTO: ModelDTO, CD : DataModel> pickById(childClass: ChildClass<C_DTO, CD>, id: Long): ResultSingle<C_DTO, CD>

    suspend fun <C_DTO: ModelDTO, CD : DataModel, T: IdTable<Long>> pick(
        childClass: ChildClass<C_DTO, CD>,
        conditions: WhereQuery<T>
    ): ResultSingle<C_DTO, CD>

}