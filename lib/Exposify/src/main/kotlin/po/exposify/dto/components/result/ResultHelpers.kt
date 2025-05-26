package po.exposify.dto.components.result

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerResult
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.collections.forEach


fun <DTO, D, E>  DTOBase<DTO, D, E>.createResultList(
    initial: List<CommonDTO<DTO, D, E>>
): ResultList<DTO, D, E> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultList(this, initial.toMutableList())
}


fun <DTO, D, E>  List<CommonDTO<DTO, D, E>>.createResultList(
    dtoClass: DTOBase<DTO, D, E>,
    operation : CrudOperation
): ResultList<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity {

    forEach {
        it.addTrackerResult(operation)
    }
    return ResultList(dtoClass, toMutableList())
}


fun <DTO, D, E>  DTOBase<DTO, D, E>.createSingleResult(dto : CommonDTO<DTO, D, E>): ResultSingle<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultSingle(this, dto)
}

fun <DTO, D, E>  CommonDTO<DTO, D, E>.createSingleResult(operation : CrudOperation): ResultSingle<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    this.addTrackerResult(operation)
    return ResultSingle(dtoClass, this)
}


fun <DTO, D, E>  ResultSingle<DTO, D, E>.toResultList(): ResultList<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{

    return ResultList(dtoClass, mutableListOf(this.getAsCommonDTOForced()))
}

fun <DTO, D, E>  ResultList<DTO, D, E>.toResultSingle(): ResultSingle<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    val dtoList = this.getAsCommonDTO()
    return  ResultSingle(this.dtoClass, dtoList.firstOrNull())
}