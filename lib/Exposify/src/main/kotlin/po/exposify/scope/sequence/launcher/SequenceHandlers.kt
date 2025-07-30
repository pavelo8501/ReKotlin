package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.misc.functions.containers.DeferredContainer
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity


sealed class ExecutionHandlerBase<DTO, D, E>(
): CTX where DTO: ModelDTO, D: DataModel, E : LongEntity  {

    override val identity: CTXIdentity<ExecutionHandlerBase<DTO, D, E>> = asIdentity()

    private val deferredQueryError = "DeferredQuery required but not provided"
    private var deferredQueryBacking: DeferredContainer<WhereQuery<E>>? = null
    val isWhereQueryAvailable: Boolean get() = deferredQueryBacking != null
    val whereQuery: DeferredContainer<WhereQuery<E>> get() {
      return deferredQueryBacking.getOrOperations(this)
    }
    internal fun provideWhereQuery(query: DeferredContainer<WhereQuery<E>>){
        deferredQueryBacking = query
    }

    private var deferredParameterBacking: DeferredContainer<Long>? = null
    val isDeferredParameterAvailable: Boolean get() = deferredParameterBacking != null
    val deferredParameter: DeferredContainer<Long> get() {
        return deferredParameterBacking.getOrOperations(this)
    }
    internal fun provideDeferredParameter(parameter: DeferredContainer<Long>){
        deferredParameterBacking = parameter
    }
}

class SingleTypeHandler<DTO, D, E> internal constructor():ExecutionHandlerBase<DTO, D, E>()
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    val descriptor: RootDescriptorBase<DTO, D, E>? = null
    var result: ResultSingle<DTO, D, E>? = null

    private var deferredInputBacking: DeferredContainer<D>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<D> get() {
        return deferredInputBacking.getOrOperations(this)
    }
    internal fun provideDeferredInput(input: DeferredContainer<D>){
        deferredInputBacking = input
    }
}


class ListTypeHandler<DTO, D, E> internal constructor():ExecutionHandlerBase<DTO, D, E>()
        where DTO : ModelDTO, D : DataModel, E : LongEntity
{
    private var descriptor: ListDescriptor<DTO, D, E>? = null
    var result: ResultList<DTO, D, E>? = null

    private var deferredInputBacking: DeferredContainer<List<D>>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<List<D>> get() {
        return deferredInputBacking.getOrOperations(this)
    }
    internal fun provideDeferredInput(input: DeferredContainer<List<D>>){
        deferredInputBacking = input
    }
}

class SingleTypeSwitchHandler<DTO, D, E, F, FD, FE>
internal constructor(
   val descriptor: SwitchDescriptorBase<DTO, D, E, F>,
   internal val parentDTO: CommonDTO<F, FD, FE>
):ExecutionHandlerBase<DTO, D, E>()
        where DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD : DataModel, FE : LongEntity
{

    private var deferredInputBacking: DeferredContainer<D>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<D> get() {
        return deferredInputBacking.getOrOperations(this)
    }
    internal fun provideDeferredInput(input: DeferredContainer<D>){
        deferredInputBacking = input
    }
}



