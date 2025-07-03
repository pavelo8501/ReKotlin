package po.exposify.dto.components.result

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.components.tracker.extensions.TrackableDTONode
import po.exposify.dto.components.tracker.extensions.collectTrackerTree
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.getOrOperations
import po.misc.exceptions.ManagedException
import po.misc.types.castListOrThrow


internal interface ExposifyResult{
    val dtoClass: DTOBase<*, *, *>
    val resultMessage: String
    val size : Int
    var activeCRUD: CrudOperation
    var failureCause: ManagedException?
}

class ResultList<DTO, D, E> internal constructor(
    override val  dtoClass: DTOBase<DTO, D, E>,
    private var result : List<CommonDTO<DTO, D, E>> = emptyList()
) :ExposifyResult where DTO : ModelDTO, D: DataModel, E : LongEntity {


    override var resultMessage: String = ""
    override val size: Int get() = result.size
    override var activeCRUD: CrudOperation = CrudOperation.Create
    override var failureCause: ManagedException? = null

    fun addResult(list: List<CommonDTO<DTO, D, E>>): ResultList<DTO, D, E> {
        result = list.toMutableList()
        return this
    }

    internal fun appendDto(dto: CommonDTO<DTO, D, E>): ResultList<DTO, D, E> {
        val mutable = result.toMutableList()
        mutable.add(dto)
        result = mutable.toList()
        return this
    }

    internal fun appendDto(single: ResultSingle<DTO, D, E>): ResultList<DTO, D, E> {
        appendDto(single.getAsCommonDTOForced())
        return this
    }

    fun getData(): List<D> {
        return result.map { it.dataModel }
    }

    fun getDTO(): List<DTO> {
        val typeRecord = dtoClass.dtoType
        return result.castListOrThrow<DTO, OperationsException>(typeRecord.clazz) {
            operationsException(it, ExceptionCode.CAST_FAILURE)
        }
    }

    fun getTrackers(): List<DTOTracker<DTO, D>>{
        return result.map { it.tracker }
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
    override val dtoClass: DTOBase<DTO, D, E>,
    private var result: CommonDTO<DTO, D, E>? = null
): ExposifyResult where DTO : ModelDTO, D: DataModel, E : LongEntity {

    override var resultMessage: String = ""
    override val size: Int get()  {
        return if(result != null){ 1 }else{ 0 }
    }
    override var activeCRUD: CrudOperation = CrudOperation.Create
    override var failureCause: ManagedException? = null

    val isFaulty: Boolean get() = failureCause != null

    internal fun appendDto(dtoToAppend: CommonDTO<DTO, D, E>): ResultSingle<DTO, D, E> {
        result = dtoToAppend
        return this
    }

    fun getData(): D? {
        val dataModel = result?.dataModel
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
        return result.getOrOperations("No result")
    }

    fun getDTO(): DTO? {
        @Suppress("UNCHECKED_CAST")
        return result as? DTO
    }

    fun getDTOForced(): DTO {
        @Suppress("UNCHECKED_CAST")
        return result as DTO
    }

    fun getTracker():DTOTracker<DTO, D>?{
        return result?.tracker
    }

    fun setWarningMessage(message: String): ResultSingle<DTO, D, E> {
        resultMessage = message
        return this
    }

    fun toResultList(): ResultList<DTO, D, E> {
        val transform = ResultList(dtoClass)
        result?.let {
            transform.addResult(listOf(it))
        } ?: run {
            transform.setWarningMessage(resultMessage)
        }
        return transform
    }
}
