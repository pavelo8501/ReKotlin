package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.misc.types.castListOrThrow


fun <DTO, D, E>  DTOBase<DTO, D, E>.createResultList(
    initial: List<CommonDTO<DTO, D, E>>? = null
): ResultList<DTO, D, E> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultList(this, initial)
}

fun <DTO, D, E>  DTOBase<DTO, D, E>.createSingleResult(initial : CommonDTO<DTO, D, E>? = null): ResultSingle<DTO, D, E>
where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultSingle(this, initial)
}

fun <DTO, D, E>  ResultSingle<DTO, D, E>.toResultList(): ResultList<DTO, D, E>
where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultList(this.dtoClass).appendDto(this)
}

fun <DTO, D, E>  ResultList<DTO, D, E>.toResultSingle(): ResultSingle<DTO, D, E>
where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    val dtoList = this.getAsCommonDTO()
    return  ResultSingle(this.dataClass, dtoList.firstOrNull())
}




class ResultList<DTO, DATA, ENTITY>(
   internal val dataClass: DTOBase<DTO, DATA, ENTITY>,
   private val initialList : List<CommonDTO<DTO, DATA, ENTITY>>? = null
)  where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity {

    internal val dtoList: MutableList<CommonDTO<DTO, DATA, ENTITY>> = mutableListOf()

    init {
        initialList?.let {
            dtoList.addAll(it)
        }
    }


    fun addList(list: List<CommonDTO<DTO, DATA, ENTITY>>): ResultList<DTO, DATA, ENTITY> {
        dtoList.addAll(list)
        return this
    }

    internal fun appendDto(dto: CommonDTO<DTO, DATA, ENTITY>): ResultList<DTO, DATA, ENTITY> {
        dtoList.add(dto)
        return this
    }

    internal fun appendDto(single: ResultSingle<DTO, DATA, ENTITY>): ResultList<DTO, DATA, ENTITY> {
        single.getAsCommonDTO()?.let {
            dtoList.add(it)
        }
        return this
    }

    fun getData(): List<DATA> {
        val dataModels = dtoList.map { it.dataModel }
        return dataModels
    }

    fun getDTO(): List<DTO> {
        return dtoList.castListOrThrow<DTO, OperationsException>(dataClass.config.registryRecord.derivedDTOClazz)
    }

    internal fun getAsCommonDTO():  List<CommonDTO<DTO, DATA, ENTITY>> {
        return dtoList
    }
}

class ResultSingle<DTO, DATA, ENTITY>(
    internal val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private var rootDTO: CommonDTO<DTO, DATA, ENTITY>? = null
) where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity {


    internal fun appendDto(dto: CommonDTO<DTO, DATA, ENTITY>): ResultSingle<DTO, DATA, ENTITY> {
        rootDTO = dto
        return this
    }

    fun getData(): DATA? {
        val dataModel =  rootDTO?.dataModel
        return dataModel
    }

    fun getDataForced(): DATA {
        val dataModel = getAsCommonDTOForced().dataModel
        return dataModel
    }

    internal fun getAsCommonDTO(): CommonDTO<DTO, DATA, ENTITY>? {
        return rootDTO
    }

    internal fun getAsCommonDTOForced(): CommonDTO<DTO, DATA, ENTITY> {
        return rootDTO.getOrOperationsEx("No result")
    }

    fun getDTO(): DTO? {
        @Suppress("UNCHECKED_CAST")
        return rootDTO as? DTO
    }

    fun getDTOForced(): DTO {
        @Suppress("UNCHECKED_CAST")
        return rootDTO as DTO
    }


}