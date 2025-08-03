package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.DTOExecutionContext
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.enums.DTOStatus
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.helpers.restoreDTOTypes
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType

@PublishedApi
internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> RootDTO<DTO, D, E>.shallowDTO(): CommonDTO<DTO, D, E> {
    val dto = dtoConfiguration.dtoFactory.createDto()
    dto.updateDataStatus(DataStatus.PreflightCheckMock)
    return dto
}

@Suppress("ktlint:standard:max-line-length")
internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity> DTOClass<DTO, D, E>.shallowDTO(
    parentDTO: CommonDTO<F, FD, FE>,
): CommonDTO<DTO, D, E> =
    parentDTO.withDTOContext(commonDTOType) {
        val newDTO = dtoFactory.createDto()
        newDTO.registerParentDTO(parentDTO)

        newDTO.updateDataStatus(DataStatus.PreflightCheckMock)
        val restored: CommonDTO<DTO, D, E> = restoreDTOTypes(newDTO)
        restored
    }

internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> DTOBase<DTO, D, E>.newDTO(): CommonDTO<DTO, D, E> {
    val dto = dtoConfiguration.dtoFactory.createDto()
    return dto
}

inline fun <DTO, D, F, FD, R> CommonDTO<DTO, D, *>.withDTOContext(
    commonDTOType: CommonDTOType<F, FD, *>,
    block: DTOExecutionContext<F, FD, *, DTO, D, *>.() -> R,
): R where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel, R : Any {
    val context = executionContextMap.getUnsafeCasting<DTOExecutionContext<F, FD, *, DTO, D, *>>(commonDTOType)
    return block.invoke(context)
}

suspend fun <DTO, D, F, FD, R> CommonDTO<DTO, D, *>.withDTOContextSuspending(
    commonDTOType: CommonDTOType<F, FD, *>,
    block: suspend DTOExecutionContext<F, FD, *, DTO, D, *>.() -> R,
): R where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel, R : Any {
    val context = executionContextMap.getUnsafeCasting<DTOExecutionContext<F, FD, *, DTO, D, *>>(commonDTOType)
    return block.invoke(context)
}
