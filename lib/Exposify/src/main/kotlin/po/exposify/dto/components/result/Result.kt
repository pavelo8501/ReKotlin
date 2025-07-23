package po.exposify.dto.components.result

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.getOrOperations
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.exceptions.ManagedException
import po.misc.types.castListOrThrow

interface ExposifyResult{
    val dtoClass: DTOBase<*, *, *>
    val resultMessage: String
    val size : Int
    var activeCRUD: CrudOperation
    var failureCause: ManagedException?
}



sealed class ResultBase<DTO, D>(
    dtoClass: DTOBase<DTO, D, *>
): CTX  where DTO: ModelDTO, D: DataModel{

}

class ResultList<DTO, D, E> internal constructor(
    override val dtoClass: DTOBase<DTO, D, E>,
    private var result : List<CommonDTO<DTO, D, E>> = emptyList()
):ResultBase<DTO, D>(dtoClass),  ExposifyResult where DTO: ModelDTO, D: DataModel, E : LongEntity {

    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dtoClass)

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
        return result.map { it.dataContainer.source }
    }

    fun getDTO(): List<DTO> {
        val typeRecord = dtoClass.dtoType
        return result.castListOrThrow(typeRecord.kClass) { str, th->
            operationsException(str, ExceptionCode.CAST_FAILURE, dtoClass)
        }
    }

    fun getTrackers(): List<DTOTracker<DTO, D, E>>{
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
): ResultBase<DTO, D>(dtoClass), ExposifyResult where DTO : ModelDTO, D: DataModel, E : LongEntity {

    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dtoClass)

    override var resultMessage: String = ""
    override val size: Int get()  {
        return if(result != null){ 1 }else{ 0 }
    }
    override var activeCRUD: CrudOperation = CrudOperation.Create
    override var failureCause: ManagedException? = null

    val isFaulty: Boolean get() = failureCause != null

    val dto: DTO get(){
       return getDTOForced()
    }

    internal fun appendDto(dtoToAppend: CommonDTO<DTO, D, E>): ResultSingle<DTO, D, E> {
        result = dtoToAppend
        return this
    }

    fun getData(): D? {
        return result?.dataContainer?.source
    }

    fun getDataForced(): D {
        return getData().getOrOperations("getData",this)
    }

    internal fun getAsCommonDTO(): CommonDTO<DTO, D, E>? {
        return result
    }

    internal fun getAsCommonDTOForced(): CommonDTO<DTO, D, E> {
        return result.getOrOperations("Result", this)
    }

    fun getDTO(): DTO? {
        @Suppress("UNCHECKED_CAST")
        return result as? DTO
    }

    fun getDTOForced(): DTO {
       return result?.asDTO() ?:run {
            val managed = failureCause
            if(managed != null){
                throw managed
            }else{
                throw operationsException("Result is null with reason unknow", ExceptionCode.DTO_LOOKUP_FAILURE, dtoClass)
            }
        }
    }

    fun getTracker():DTOTracker<DTO, D, E>?{
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
