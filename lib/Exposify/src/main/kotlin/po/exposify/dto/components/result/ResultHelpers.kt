package po.exposify.dto.components.result

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerResult
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.exceptions.ManagedException
import kotlin.collections.forEach




fun <T: ResultBase<*,*,*>> T.addFailureCause(exception: ManagedException):T{
    failureCause = exception
    return this
}

fun <T: ResultBase<*,*,*>> T.addCrudOperation(operation : CrudOperation):T{
    activeOperation = operation
    return this
}

fun <DTO, D, E>  CommonDTO<DTO, D, E>.toResult(
    operation : CrudOperation
): ResultSingle<DTO,D,E> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultSingle(dtoClass, this)
        .addCrudOperation(operation)
}

fun <DTO, D, E>  CommonDTO<DTO, D, E>.toResult(
    failureCause: ManagedException,
    operation : CrudOperation
): ResultSingle<DTO,D,E> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultSingle(dtoClass)
        .addFailureCause(failureCause)
        .addCrudOperation(operation)
}

fun <DTO, D, E>  List<CommonDTO<DTO, D, E>>.toResult(
    dtoClass: DTOBase<DTO, D, E>,
    operation : CrudOperation
): ResultList<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity {
    forEach { it.addTrackerResult(operation) }
    return ResultList(dtoClass, this)
}


fun <DTO, D, E>  ResultList<DTO, D, E>.toResultSingle(): ResultSingle<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    val dtoList = this.getAsCommonDTO()
    return  ResultSingle(this.dtoClass, dtoList.firstOrNull())
}

fun <DTO, D, E>  ResultSingle<DTO, D, E>.toResultList(): ResultList<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultList(dtoClass, mutableListOf(this.getAsCommonDTOForced()))
}

fun <DTO, D, E>  List<ResultSingle<DTO, D, E>>.toResult(
   dtoClass: DTOBase<DTO, D, E>
): ResultList<DTO,D,E> where  DTO: ModelDTO, D : DataModel, E : LongEntity {
    val resultList = ResultList(dtoClass)
    forEach { resultList.appendDto(it) }
    return resultList
}
