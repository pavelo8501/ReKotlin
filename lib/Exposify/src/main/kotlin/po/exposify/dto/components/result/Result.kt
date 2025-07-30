package po.exposify.dto.components.result

import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
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


sealed class ResultBase<DTO, D, R: Any>(
    dtoClass: DTOBase<DTO, D, *>,
    protected var result: R?
): ExposifyResult, CTX  where DTO: ModelDTO, D: DataModel{


    override var failureCause: ManagedException? = null
    protected val noResultException: ManagedException
        get() = failureCause?: OperationsException("Result not available", ExceptionCode.UNDEFINED, this)

    val isFaulty: Boolean get() = failureCause != null || result == null
}

class ResultList<DTO, D> internal constructor(
    override val dtoClass: DTOBase<DTO, D, *>,
    private var resultBacking : List<CommonDTO<DTO, D, *>> = emptyList()
):ResultBase<DTO, D, List<CommonDTO<DTO, D, *>>>(dtoClass, resultBacking) where DTO: ModelDTO, D: DataModel{

    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dtoClass)

    override var resultMessage: String = ""
    override val size: Int get() = resultBacking.size
    override var activeCRUD: CrudOperation = CrudOperation.Create

    val dto: List<DTO> get() = resultBacking.castListOrThrow(dtoClass.commonDTOType.dtoType.kClass, this){payload->
        operationsException(payload.setCode( ExceptionCode.CAST_FAILURE))
    }

    val data: List<D> get() = resultBacking.map { it.dataContainer.getValue(this) }

    fun addResult(list: List<CommonDTO<DTO, D, *>>): ResultList<DTO, D> {
        result = list.toMutableList()
        return this
    }

    internal fun appendDto(dto: CommonDTO<DTO, D, *>): ResultList<DTO, D> {
        val mutable = resultBacking.toMutableList()
        mutable.add(dto)
        result = mutable.toList()
        return this
    }

    internal fun appendDto(single: ResultSingle<DTO, D>): ResultList<DTO, D> {
        appendDto(single.getAsCommonDTOForced())
        return this
    }

    fun getTrackers(): List<DTOTracker<DTO, D, *>>{
        return resultBacking.map { it.tracker }
    }

    internal fun getAsCommonDTO(): List<CommonDTO<DTO, D, *>> {
        return resultBacking
    }

}

class ResultSingle<DTO, D> internal constructor(
    override val dtoClass: DTOBase<DTO, D, *>,
    private var initialResult: CommonDTO<DTO, D, *>? = null
): ResultBase<DTO, D, CommonDTO<DTO, D, *>>(dtoClass, initialResult), ExposifyResult where DTO : ModelDTO, D: DataModel{

    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dtoClass)

    override var resultMessage: String = ""
    override val size: Int get()  {
        return if(result != null){ 1 }else{ 0 }
    }
    override var activeCRUD: CrudOperation = CrudOperation.Create


    val dto: DTO? get() = result?.asDTO()
    val data: D? get() = result?.dataContainer?.value

    private fun getCommonForced(): CommonDTO<DTO, D, *>{
      return  result?: throw noResultException
    }

    internal fun getAsCommonDTO(): CommonDTO<DTO, D, *>? {
        return result
    }

    internal fun getAsCommonDTOForced(): CommonDTO<DTO, D, *> {
        return result.getOrOperations(this)
    }

    fun getDTOForced(): DTO {
        return dto?: throw noResultException
    }

    fun getDataForced(): D {
        val dto = getCommonForced()
        return dto.dataContainer.getValue(this)
    }

    fun getTracker():DTOTracker<DTO, D, *>?{
        return result?.tracker
    }

}
