package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.TrackableDTONode
import po.exposify.dto.components.tracker.extensions.addTrackerResult
import po.exposify.dto.components.tracker.extensions.collectTrackerTree
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.getOrOperationsEx
import po.misc.types.castListOrThrow



sealed class ResultBase<DTO, DATA, ENTITY>(
    internal val dtoClass: DTOBase<DTO, DATA, ENTITY>,
) where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity{


}

class ResultList<DTO, DATA, ENTITY>(
   dtoClass: DTOBase<DTO, DATA, ENTITY>,
   internal var resultList : MutableList<CommonDTO<DTO, DATA, ENTITY>> = mutableListOf()
) : ResultBase<DTO, DATA, ENTITY>(dtoClass) where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity {

  //  internal val dtoList: MutableList<CommonDTO<DTO, DATA, ENTITY>> = mutableListOf()


    fun addResult(list: List<CommonDTO<DTO, DATA, ENTITY>>): ResultList<DTO, DATA, ENTITY> {
        resultList = list.toMutableList()
        return this
    }

    internal fun appendDto(dto: CommonDTO<DTO, DATA, ENTITY>): ResultList<DTO, DATA, ENTITY> {
        resultList?.add(dto)?:listOf(dto)
        return this
    }

    internal fun appendDto(single: ResultSingle<DTO, DATA, ENTITY>): ResultList<DTO, DATA, ENTITY> {
        single.getAsCommonDTO()?.let {
            resultList?.add(it)?:listOf(it)
        }
        return this
    }

    fun getData(): List<DATA> {
        return resultList.getOrOperationsEx("Result is null").map { it.dataModel }
    }

    fun getDTO(): List<DTO> {
        return resultList.castListOrThrow<DTO, OperationsException>(dtoClass.config.registryRecord.derivedDTOClazz)
    }

    internal fun getAsCommonDTO():  List<CommonDTO<DTO, DATA, ENTITY>> {
        return resultList.getOrOperationsEx("Result is null")
    }

    fun getTrackerInfo(): List<TrackableDTO>{
        val result : MutableList<TrackableDTO> = mutableListOf()
        resultList.getOrOperationsEx().forEach {
            result.add(it.tracker.collectTrackers())
        }
        return result
    }
}

class ResultSingle<DTO, DATA, ENTITY>(
    dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private var dto: CommonDTO<DTO, DATA, ENTITY>? = null
): ResultBase<DTO, DATA, ENTITY>(dtoClass) where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity {

    internal fun appendDto(dtoToAppend: CommonDTO<DTO, DATA, ENTITY>): ResultSingle<DTO, DATA, ENTITY> {
        dto = dtoToAppend
        return this
    }

    fun getData(): DATA? {
        val dataModel =  dto?.dataModel
        return dataModel
    }

    fun getDataForced(): DATA {
        val dataModel = getAsCommonDTOForced().dataModel
        return dataModel
    }

    internal fun getAsCommonDTO(): CommonDTO<DTO, DATA, ENTITY>? {
        return dto
    }

    internal fun getAsCommonDTOForced(): CommonDTO<DTO, DATA, ENTITY> {
        return dto.getOrOperationsEx("No result")
    }

    fun getDTO(): DTO? {
        @Suppress("UNCHECKED_CAST")
        return dto as? DTO
    }

    fun getDTOForced(): DTO {
        @Suppress("UNCHECKED_CAST")
        return dto as DTO
    }

    fun getTrackerInfo(): TrackableDTO{
       return dto.getOrOperationsEx().tracker.collectTrackers()
    }

    fun getTrackerTree():TrackableDTONode{
       return dto.getOrOperationsEx().tracker.collectTrackerTree()
    }

}


fun <DTO, D, E>  DTOBase<DTO, D, E>.createResultList(
    initial: List<CommonDTO<DTO, D, E>>
): ResultList<DTO, D, E> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultList(this, initial.toMutableList())
}

context(dtoClass: DTOBase<DTO, D, E>)
fun <DTO, D, E>  List<CommonDTO<DTO, D, E>>.createResultList(operation : CrudOperation): ResultList<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
            this.forEach {
                it.addTrackerResult(operation)
            }
   return ResultList(dtoClass, this.toMutableList())
}


fun <DTO, D, E>  DTOBase<DTO, D, E>.createSingleResult(initial : CommonDTO<DTO, D, E>? = null): ResultSingle<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultSingle(this, initial)
}

context(dtoClass: DTOBase<DTO, D, E>)
fun <DTO, D, E>  CommonDTO<DTO, D, E>.createSingleResult(operation : CrudOperation): ResultSingle<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
            this.addTrackerResult(operation)
    return ResultSingle(dtoClass, this)
}


fun <DTO, D, E>  ResultSingle<DTO, D, E>.toResultList(): ResultList<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{

    return ResultList(this.dtoClass).appendDto(this)
}

fun <DTO, D, E>  ResultList<DTO, D, E>.toResultSingle(): ResultSingle<DTO, D, E>
        where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    val dtoList = this.getAsCommonDTO()
    return  ResultSingle(this.dtoClass, dtoList.firstOrNull())
}