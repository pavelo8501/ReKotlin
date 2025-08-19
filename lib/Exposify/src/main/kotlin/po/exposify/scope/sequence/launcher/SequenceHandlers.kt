package po.exposify.scope.sequence.launcher

import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.RootDescriptorBase
import po.exposify.scope.sequence.builder.SequenceDescriptor
import po.exposify.scope.sequence.builder.SwitchDescriptorBase
import po.exposify.scope.sequence.builder.SwitchListDescriptor
import po.exposify.scope.sequence.builder.SwitchSingeDescriptor
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.containers.DeferredContainer




sealed class ExecutionHandlerBase<DTO, D, R : ResultBase<DTO, D, *>>(
    internal val descriptor: SequenceDescriptor<DTO, D>
) : CTX where DTO : ModelDTO, D : DataModel {
    private val deferredQueryError = "DeferredQuery required but not provided"
    private var deferredQueryBacking: DeferredContainer<WhereQuery<*>>? = null

    val isWhereQueryAvailable: Boolean get() = deferredQueryBacking != null
    val whereQuery: DeferredContainer<WhereQuery<*>> get() {
        return deferredQueryBacking.getOrOperations(this)
    }

    internal fun provideWhereQuery(query: DeferredContainer<WhereQuery<*>>) {
        deferredQueryBacking = query
    }

    private var deferredParameterBacking: DeferredContainer<Long>? = null
    val isDeferredParameterAvailable: Boolean get() = deferredParameterBacking != null

    val deferredParameter: DeferredContainer<Long> get() {
        return deferredParameterBacking.getOrOperations(this)
    }

    internal fun provideDeferredParameter(parameter: DeferredContainer<Long>) {
        deferredParameterBacking = parameter
    }

    var parameterCallback: ((Long) -> R)? = null

    fun subscribeParameter(callback: (Long) -> R) {
        parameterCallback = callback
    }

    fun triggerByParameter(parameter: Long): R = parameterCallback.getOrOperations(this).invoke(parameter)

    var queryCallback: ((DeferredContainer<WhereQuery<*>>) -> R)? = null

    fun subscribeQuery(callback: (DeferredContainer<WhereQuery<*>>) -> R) {
        queryCallback = callback
    }

    fun triggerQuery(query: DeferredContainer<WhereQuery<*>>): R = queryCallback.getOrOperations(this).invoke(query)
}

class SingleTypeHandler<DTO, D> internal constructor(
    descriptor: RootDescriptorBase<DTO, D>,
) : ExecutionHandlerBase<DTO, D, ResultSingle<DTO, D>>(descriptor) where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<SingleTypeHandler<DTO, D>> = asIdentity()

    private var deferredInputBacking: DeferredContainer<D>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<D> get() {
        return deferredInputBacking.getOrOperations(this)
    }

    internal fun provideDeferredInput(input: DeferredContainer<D>) {
        deferredInputBacking = input
    }
}

class ListTypeHandler<DTO, D> internal constructor(
    descriptor: RootDescriptorBase<DTO, D>,
) : ExecutionHandlerBase<DTO, D, ResultList<DTO, D>>(descriptor)
    where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<ListTypeHandler<DTO, D>> = asIdentity()

    private var deferredInputBacking: DeferredContainer<List<D>>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<List<D>> get() {
        return deferredInputBacking.getOrOperations(this)
    }

    internal fun provideDeferredInput(input: DeferredContainer<List<D>>) {
        deferredInputBacking = input
    }
}

class SingleTypeSwitchHandler<DTO, D, F, FD> internal constructor(
    descriptor: SwitchDescriptorBase<DTO, D, F, FD>,
) : ExecutionHandlerBase<DTO, D, ResultSingle<DTO, D>>(descriptor)
    where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
    override val identity: CTXIdentity<SingleTypeSwitchHandler<DTO, D, F, FD>> = asIdentity()

    private var deferredInputBacking: DeferredContainer<D>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<D> get() {
        return deferredInputBacking.getOrOperations(this)
    }

    internal fun provideDeferredInput(input: DeferredContainer<D>?) {
        deferredInputBacking = input
    }
}

class ListTypeSwitchHandler<DTO, D, F, FD> internal constructor(
    descriptor: SwitchDescriptorBase<DTO, D, F, FD>,
) : ExecutionHandlerBase<DTO, D, ResultSingle<DTO, D>>(descriptor)
        where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {

    override val identity: CTXIdentity<ListTypeSwitchHandler<DTO, D, F, FD>> = asIdentity()

    private var deferredInputBacking: DeferredContainer<List<D>>? = null
    val isDeferredInputAvailable: Boolean get() = deferredInputBacking != null
    val deferredInput: DeferredContainer<List<D>> get() {
        return deferredInputBacking.getOrOperations(this)
    }
    internal fun provideDeferredInput(input: DeferredContainer<List<D>>?) {
        deferredInputBacking = input
    }
}
