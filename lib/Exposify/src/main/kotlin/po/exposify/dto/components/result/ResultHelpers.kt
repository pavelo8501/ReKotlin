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


internal fun <T: ExposifyResult> T.addFailureCause(exception: ManagedException):T{
    failureCause = exception
    return this
}

internal fun <T: ExposifyResult> T.addCrudOperation(operation : CrudOperation):T{
    activeCRUD = operation
    return this
}

internal fun <EX: ManagedException,  DTO:ModelDTO, D: DataModel, E : LongEntity>  EX.toResultSingle(
    operation : CrudOperation,
    dtoClass: DTOBase<DTO, D, E>
): ResultSingle<DTO, D>{
    val result =  ResultSingle(dtoClass).addCrudOperation(operation)
    return  result.addFailureCause(this)
}

internal fun <EX: ManagedException, DTO:ModelDTO, D: DataModel, E : LongEntity>  EX.toResultList(
    operation : CrudOperation,
    dtoClass: DTOBase<DTO, D, E>
): ResultList<DTO, D>{
    val result =  ResultList(dtoClass).addCrudOperation(operation)
    return  result.addFailureCause(this)
}


fun <DTO, D, E>  CommonDTO<DTO, D, E>.toResult(
    operation : CrudOperation
): ResultSingle<DTO, D> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultSingle(dtoClass, this)
        .addCrudOperation(operation)
}

fun <DTO, D, E>  CommonDTO<DTO, D, E>.toResult(
    failureCause: ManagedException,
    operation : CrudOperation
): ResultSingle<DTO, D> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultSingle(dtoClass)
        .addFailureCause(failureCause)
        .addCrudOperation(operation)
}

fun <DTO, D, E>  List<CommonDTO<DTO, D, E>>.toResult(
    dtoClass: DTOBase<DTO, D, E>,
    operation : CrudOperation
): ResultList<DTO, D>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity {
    forEach { it.addTrackerResult(operation) }
    return ResultList(dtoClass, this)
}

fun <DTO, D, E>  DTOBase<DTO, D, E>.toResult(
    exception:  ManagedException,
): ResultSingle<DTO, D>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity {

    return ResultSingle(this).addFailureCause(exception)
}



fun <DTO, D>  ResultList<DTO, D>.convertToSingle(): ResultSingle<DTO, D>
        where  DTO: ModelDTO, D : DataModel{
    val dtoList = this.getAsCommonDTO()
    return  ResultSingle(this.dtoClass, dtoList.firstOrNull())
}

fun <DTO, D>  List<ResultSingle<DTO, D>>.toResult(
   dtoClass: DTOBase<DTO, D, *>
): ResultList<DTO, D> where  DTO: ModelDTO, D : DataModel{
    val resultList = ResultList(dtoClass)
    forEach { resultList.appendDto(it) }
    return resultList
}
