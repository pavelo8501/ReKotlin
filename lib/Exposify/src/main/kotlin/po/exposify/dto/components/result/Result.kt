package po.exposify.dto.components.result

import po.auth.sessions.models.SessionBase
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.helpers.asDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.exceptions.ManagedException
import po.misc.types.castListOrThrow
import po.misc.types.token.TypeToken

interface ExposifyResult {
    val dtoClass: DTOBase<*, *, *>
    val resultMessage: String
    val size: Int
    var activeCRUD: CrudOperation
    var failureCause: ManagedException?
}

sealed class ResultBase<DTO, D, R : Any>(
    dtoClass: DTOBase<DTO, D, *>,
    protected var result: R?,
) : ExposifyResult,
    CTX where DTO : ModelDTO, D : DataModel {
    protected val dtoType: TypeToken<DTO> = dtoClass.commonDTOType.dtoType
    protected val dataType: TypeToken<D> = dtoClass.commonDTOType.dataType

    protected val noResultNoExceptionMsg: String get() = "Result class has no data nor registered exception"
    val abnormalState: ExceptionCode = ExceptionCode.ABNORMAL_STATE
    override var failureCause: ManagedException? = null

    var authorizedSession: SessionBase? = null
        private set

    val isFaulty: Boolean get() = failureCause != null || result == null

    internal fun saveSession(session: SessionBase?){
        authorizedSession = session
    }

}

class ResultList<DTO, D> internal constructor(
    override val dtoClass: DTOBase<DTO, D, *>,
    private var resultBacking: MutableList<CommonDTO<DTO, D, *>> = mutableListOf(),
) : ResultBase<DTO, D, List<CommonDTO<DTO, D, *>>>(dtoClass, resultBacking) where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<ResultList<DTO, D>> get() =  asSubIdentity(dtoClass)

    override var resultMessage: String = ""
    override val size: Int get() = resultBacking.size
    override var activeCRUD: CrudOperation = CrudOperation.Create

    val dto: List<DTO> get() =
        resultBacking.castListOrThrow(this, dtoClass.commonDTOType.dtoType.kClass) { payload ->
            operationsException(payload.setCode(ExceptionCode.CAST_FAILURE))
        }

    val data: List<D> get() {
        try {
            failureCause?.let { throw it }
          return  resultBacking.map { it.dataContainer.getValue(this) }
        }catch (th: Throwable){
            if(th is ManagedException){ failureCause = th }
            return emptyList()
        }
    }
    val dataUnsafe: List<D> get() {
        try {
           return resultBacking.map { it.dataContainer.getValue(this) }
        }catch (th: Throwable){
            throw failureCause?:th
        }
    }

    init {
        identity.setNamePattern { "ResultList<${dtoType.typeName}, ${dataType.typeName}>" }
    }

    fun addResult(list: List<CommonDTO<DTO, D, *>>): ResultList<DTO, D> {
        result = list.toMutableList()
        return this
    }
    internal fun appendDto(dto: CommonDTO<DTO, D, *>): ResultList<DTO, D> {
        resultBacking.add(dto)
        return this
    }

    internal fun appendDto(single: ResultSingle<DTO, D>): ResultList<DTO, D> {
        appendDto(single.getAsCommonDTO())
        return this
    }

    fun getTrackers(): List<DTOTracker<DTO, D, *>> = resultBacking.map { it.tracker }

    internal fun getAsCommonDTO(): List<CommonDTO<DTO, D, *>> = resultBacking

    override fun toString(): String = identifiedByName
}

class ResultSingle<DTO, D> internal constructor(
    override val dtoClass: DTOBase<DTO, D, *>,
    private var initialResult: CommonDTO<DTO, D, *>? = null,
) : ResultBase<DTO, D, CommonDTO<DTO, D, *>>(dtoClass, initialResult),
    ExposifyResult where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<ResultSingle<DTO, D>> get() =  asSubIdentity(dtoClass)

    override var resultMessage: String = ""
    override val size: Int get() {
        return if (result != null) {
            1
        } else {
            0
        }
    }
    override var activeCRUD: CrudOperation = CrudOperation.Create

    val dto: DTO? get() = result?.asDTO()
    val data: D? get() = result?.dataContainer?.value
    val dataUnsafe:D get() =
            result?.dataContainer?.value
                ?: throw failureCause
                    ?: operationsException(noResultNoExceptionMsg, abnormalState)

    internal val commonDTO: CommonDTO<DTO, D, *>? = result

    init {
        identity.setNamePattern { "ResultSingle<${dtoType.typeName}, ${dataType.typeName}>" }
    }


    internal fun getAsCommonDTO(): CommonDTO<DTO, D, *> = result?:run {
        throw failureCause?:operationsException(noResultNoExceptionMsg,abnormalState)
    }

    fun getDTOForced(): DTO {
        return dto ?: throw failureCause?:operationsException(noResultNoExceptionMsg, abnormalState)

    }
    fun getTracker(): DTOTracker<DTO, D, *>? = result?.tracker

    override fun toString(): String = identifiedByName
}
