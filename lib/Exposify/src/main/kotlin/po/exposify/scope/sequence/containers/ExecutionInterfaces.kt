package po.exposify.scope.sequence.containers

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO



//interface RootSingleCTX<DTO: ModelDTO<DTO>, D : DataModel, E: LongEntity>{
//    fun RootExecCTX<DTO, D, E>.pickById(id: Long, block: RootExecCTX<DTO, D, E>.(Long)->  ResultSingle<DTO, D, E>)
//    fun <T: IdTable<Long>> RootExecCTX<DTO, D, E>.pick(conditions:  WhereQuery<T>, block:()->  ResultSingle<DTO, D, E>)
//    fun RootExecCTX<DTO, D, E>.update(dataModel: D, block: () -> ResultList<DTO, D, E>)
//    fun RootExecCTX<DTO, D, E>.insert(dataModel: D, block:()-> ResultSingle<DTO, D, E>)
//
//    fun toResult(): ResultSingle<DTO, D, E>
//
//}
//
//
//interface RootListCTX<DTO: ModelDTO<DTO>, D : DataModel, E: LongEntity>{
//    fun RootExecCTX<DTO, D, E>.update(dataModels: List<D>, block:()-> ResultList<DTO, D, E>)
//    fun RootExecCTX<DTO, D, E>.select(block:()-> ResultList<DTO, D, E>)
//    fun <T : IdTable<Long>> RootExecCTX<DTO, D, E>.select(conditions: WhereQuery<T>, block: () -> ResultList<DTO, D, E>)
//    fun RootExecCTX<DTO, D, E>.insert(dataModels: List<D>, block: () -> ResultList<DTO, D, E>)
//}