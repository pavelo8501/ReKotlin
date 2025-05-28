package po.exposify.dto.components.result

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.extensions.TrackableDTONode
import po.exposify.dto.components.tracker.extensions.collectTrackerTree
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.getOrOperationsEx
import po.misc.types.castListOrThrow

sealed class ResultBase<DTO, D, E>(
    internal val dtoClass: DTOBase<DTO, D, E>,
) where DTO : ModelDTO, D: DataModel, E : LongEntity{

    var resultMessage : String = ""

}

class ResultList<DTO, D, E> internal constructor(
    dtoClass: DTOBase<DTO, D, E>,
    private var result : MutableList<CommonDTO<DTO, D, E>>
) : ResultBase<DTO, D, E>(dtoClass) where DTO : ModelDTO, D: DataModel, E : LongEntity {


    fun addResult(list: List<CommonDTO<DTO, D, E>>): ResultList<DTO, D, E> {
        result = list.toMutableList()
        return this
    }

    internal fun appendDto(dto: CommonDTO<DTO, D, E>): ResultList<DTO, D, E> {
        result.add(dto)
        return this
    }

    internal fun appendDto(single: ResultSingle<DTO, D, E>): ResultList<DTO, D, E> {
        single.getAsCommonDTO()?.let {
            result.add(it)
        }
        return this
    }

    fun getData(): List<D> {
        return result.map { it.dataModel }
    }

    fun getDTO(): List<DTO> {
        val clazz = dtoClass.config.registry.getRecord<DTO, OperationsException>(ComponentType.DTO).clazz
        return result.castListOrThrow<DTO, OperationsException>(clazz)
    }

    internal fun getAsCommonDTO(): List<CommonDTO<DTO, D, E>> {
        return result
    }


    fun setWarningMessage(message: String): ResultList<DTO, D, E>{
        resultMessage = message
        return this
    }
}

class ResultSingle<DTO, D, E> internal constructor(
    dtoClass: DTOBase<DTO, D, E>,
    private var result: CommonDTO<DTO, D, E>? = null
): ResultBase<DTO, D, E>(dtoClass) where DTO : ModelDTO, D: DataModel, E : LongEntity {

    internal fun appendDto(dtoToAppend: CommonDTO<DTO, D, E>): ResultSingle<DTO, D, E> {
        result = dtoToAppend
        return this
    }

    fun getData(): D? {
        val dataModel =  result?.dataModel
        return dataModel
    }

    fun getDataForced(): D {
        val dataModel = getAsCommonDTOForced().dataModel
        return dataModel
    }

    internal fun getAsCommonDTO(): CommonDTO<DTO, D, E>? {
        return result
    }

    internal fun getAsCommonDTOForced(): CommonDTO<DTO, D, E> {
        return result.getOrOperationsEx("No result")
    }

    fun getDTO(): DTO? {
        @Suppress("UNCHECKED_CAST")
        return result as? DTO
    }

    fun getDTOForced(): DTO {
        @Suppress("UNCHECKED_CAST")
        return result as DTO
    }

    fun getTrackerInfo(): TrackableDTO{
        return result.getOrOperationsEx().tracker.collectTrackers()
    }

    fun getTrackerTree():TrackableDTONode{
        return result.getOrOperationsEx().tracker.collectTrackerTree()
    }

    fun setWarningMessage(message: String): ResultSingle<DTO, D, E>{
        resultMessage = message
        return this
    }
}
