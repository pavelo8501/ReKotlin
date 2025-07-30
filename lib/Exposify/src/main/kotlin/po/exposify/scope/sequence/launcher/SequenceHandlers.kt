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


sealed class ExecutionHandlerBase<DTO, D>(
): CTX where DTO: ModelDTO, D: DataModel  {

    override val identity: CTXIdentity<ExecutionHandlerBase<DTO, D>> = asIdentity()

    private val deferredQueryError = "DeferredQuery required but not provided"
    private var deferredQueryBacking: DeferredContainer<WhereQuery<*>>? = null

    val isWhereQueryAvailable: Boolean get() = deferredQueryBacking != null
    val whereQuery: DeferredContainer<WhereQuery<*>> get() {
      return deferredQueryBacking.getOrOperations(this)
    }
    internal fun provideWhereQuery(query: DeferredContainer<WhereQuery<*>>){
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

class SingleTypeHandler<DTO, D> internal constructor():ExecutionHandlerBase<DTO, D>()
        where DTO: ModelDTO, D: DataModel
{
    val descriptor: RootDescriptorBase<DTO, D>? = null
    var result: ResultSingle<DTO, D>? = null

    private var deferredInputBacking: DeferredContainer<D>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<D> get() {
        return deferredInputBacking.getOrOperations(this)
    }
    internal fun provideDeferredInput(input: DeferredContainer<D>){
        deferredInputBacking = input
    }
}


class ListTypeHandler<DTO, D> internal constructor():ExecutionHandlerBase<DTO, D>()
        where DTO : ModelDTO, D : DataModel
{
    private var descriptor: ListDescriptor<DTO, D>? = null
    var result: ResultList<DTO, D>? = null

    private var deferredInputBacking: DeferredContainer<List<D>>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<List<D>> get() {
        return deferredInputBacking.getOrOperations(this)
    }
    internal fun provideDeferredInput(input: DeferredContainer<List<D>>){
        deferredInputBacking = input
    }
}

class SingleTypeSwitchHandler<DTO, D, F, FD, >
internal constructor(
   val descriptor: SwitchDescriptorBase<DTO, D, F>,
   internal val parentDTO: CommonDTO<F, FD, *>
):ExecutionHandlerBase<DTO, D>()
        where DTO: ModelDTO, D: DataModel, F: ModelDTO, FD : DataModel
{

    private var deferredInputBacking: DeferredContainer<D>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<D> get() {
        return deferredInputBacking.getOrOperations(this)
    }
    internal fun provideDeferredInput(input: DeferredContainer<D>?){
        deferredInputBacking = input
    }
}



