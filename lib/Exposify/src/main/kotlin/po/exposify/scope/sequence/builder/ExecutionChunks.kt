package po.exposify.scope.sequence.builder

import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.createDTOProvider
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.models.SequenceParameter
import po.misc.data.monitor.HealthMonitor
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.functions.containers.DeferredContainer
import po.misc.functions.containers.LambdaContainer
import po.misc.functions.containers.LazyContainerWithReceiver
import po.misc.functions.containers.LazyExecutionContainer
import po.misc.interfaces.CTX
import po.misc.types.TaggedType
import po.misc.types.TypeData
import po.misc.types.interfaces.TagTypedClass

enum class ChunkType {
    PickByIdChunk,
    SelectChunk,
    UpdateChunk
}

enum class ChunkEvent {
    Initialized,
    InputReceived,
    ResultEvaluation,
    TriggerEvaluation,
    ResultReceived
}

enum class ChunkLambdaType {
    WithInputValue,
}


sealed class ExecutionChunkBase<DTO, D, P>(): CTX
        where DTO : ModelDTO, D : DataModel, P : Any
{

    internal val exceptionPayload: ManagedCallSitePayload = ManagedCallSitePayload(this)
    //internal open val resultContainer: LazyExecutionContainer<P, ResultBase<DTO, D>> = LazyExecutionContainer(this)
    val withBaseResultContainer: LambdaContainer<ResultBase<DTO, D>> = LambdaContainer(this)

    @PublishedApi
    internal val inputContainer: DeferredContainer<P> = DeferredContainer(this)
    val currentValue: P? get() = inputContainer.currentValue

    val withInputContainer: LambdaContainer<P> = LambdaContainer(this)
    val healthMonitor: HealthMonitor<ExecutionChunkBase<DTO, D, P>> = HealthMonitor(this)

    abstract fun configure()

    protected fun preResultComputation():P{
        val parameter = inputContainer.resolve()
        if(withInputContainer.isLambdaProvided){
            withInputContainer.resolve(parameter)
        }
        return parameter
    }
}

sealed class SingleResultChunks<DTO, D, P>(
    val configurationBlock: SingleResultChunks<DTO, D, P>.() -> Unit
):ExecutionChunkBase<DTO, D, P>(), CTX
        where DTO : ModelDTO, D : DataModel, P : Any
{
    abstract var activeResult: ResultSingle<DTO, D, *>?
    internal val configContainer: LambdaContainer<SingleResultChunks<DTO, D, P>> = LambdaContainer(this)

    val resultContainer: LazyExecutionContainer<P, ResultSingle<DTO, D, *>> = LazyExecutionContainer(this)
    val withResultContainer: LambdaContainer<ResultSingle<DTO, D, *>> = LambdaContainer(this)

    val switchContainers: MutableMap<TypeData<*>, SwitchChunkContainer<DTO, D, *, *>> = mutableMapOf()

    init {
        configContainer.registerProvider(configurationBlock)
    }

    fun <F: ModelDTO, FD: DataModel> createSwitchContainer(
        dto: CommonDTO<DTO, D, *>,
        foreignClass: DTOClass<F, FD, *>
    ): SwitchChunkContainer<DTO, D, F, FD> {
        val executionProvider = createDTOProvider(dto, foreignClass)
        val switchContainer = SwitchChunkContainer(executionProvider)
        val dataType = foreignClass.dataType.toTypeData()
        switchContainers.put(dataType, switchContainer)
        return switchContainer
    }

    fun computeResult():ResultSingle<DTO, D, *>{
        val parameter = preResultComputation()
        val result = resultContainer.resolve(parameter)
        if(withResultContainer.isLambdaProvided){
            withResultContainer.resolve(result)
        }
        return result
    }

    override fun configure() {
        configContainer.resolve(this)
    }
}

class PickByIdChunk<DTO, D>(
    override val tagType: TaggedType<PickByIdChunk<DTO, D>, ChunkType>,
    configBlock: SingleResultChunks<DTO, D, Long>.() -> Unit,
): SingleResultChunks<DTO, D, Long>(configBlock), TagTypedClass<PickByIdChunk<DTO, D>, ChunkType>
        where DTO : ModelDTO, D : DataModel {

    override val contextName: String get() = "PickByIdChunk"
    override var activeResult: ResultSingle<DTO, D, *>? = null


    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D>  create(
            configBlock: SingleResultChunks<DTO, D, Long>.() -> Unit
        ):PickByIdChunk<DTO, D> where DTO : ModelDTO, D : DataModel{
            val tagType = TaggedType.create<PickByIdChunk<DTO, D>, ChunkType>(ChunkType.PickByIdChunk)
            return PickByIdChunk(tagType, configBlock)
        }
    }
}

class UpdateChunk<DTO, D>(
    override val tagType: TaggedType<UpdateChunk<DTO, D>, ChunkType>,
    configBlock: SingleResultChunks<DTO, D, D>.() -> Unit,
):SingleResultChunks<DTO, D, D>(configBlock), TagTypedClass<UpdateChunk<DTO, D>, ChunkType>
        where DTO: ModelDTO, D : DataModel
{
    override val contextName: String get() = "UpdateChunk"
    override var activeResult: ResultSingle<DTO, D, *>? = null

    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D> create(
            configBlock:  SingleResultChunks<DTO, D, D>.() -> Unit
        ):UpdateChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
            val tagType = TaggedType.create<UpdateChunk<DTO, D>, ChunkType>(ChunkType.UpdateChunk)
            return UpdateChunk(tagType, configBlock)
        }
    }
}


sealed class ListResultChunks<DTO, D, P>(
    val configurationBlock: ListResultChunks<DTO, D, P>.() -> Unit
):ExecutionChunkBase<DTO, D, P>(), CTX
        where DTO : ModelDTO, D : DataModel, P : Any
{
    protected var activeResult: ResultList<DTO, D, *>? = null


    internal val configContainer: LambdaContainer<ListResultChunks<DTO, D, P>> = LambdaContainer(this)

    val resultContainer: LazyExecutionContainer<P, ResultList<DTO, D, *>> = LazyExecutionContainer(this)
    internal val withResultContainer: LambdaContainer<ResultList<DTO, D, *>> = LambdaContainer(this)

    init {
        configContainer.registerProvider(configurationBlock)
    }

    fun computeResult():ResultList<DTO, D, *>{
       // val parameter = preResultComputation()
        val result = resultContainer.resolve()
        if(withResultContainer.isLambdaProvided){
            withResultContainer.resolve(result)
        }
        return result
    }

    override fun configure() {
        configContainer.resolve(this)
    }
}

class SelectChunk<DTO, D>(
    override val tagType: TaggedType<SelectChunk<DTO, D>, ChunkType>,
    configBlock: ListResultChunks<DTO, D, Unit>.() -> Unit,
):ListResultChunks<DTO, D, Unit>(configBlock), TagTypedClass<SelectChunk<DTO, D>, ChunkType>
        where DTO: ModelDTO, D : DataModel
{

    override val contextName: String get() = "SelectChunk"

    init {
        resultContainer.provideReceiver(Unit)
    }


    companion object{
        fun <DTO, D> create(
            configBlock:  ListResultChunks<DTO, D, Unit>.() -> Unit
        ):SelectChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
            val tagType = TaggedType.create<SelectChunk<DTO, D>, ChunkType>(ChunkType.SelectChunk)
            return SelectChunk(tagType, configBlock)
        }
    }
}