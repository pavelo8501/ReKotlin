package po.exposify.scope.sequence.builder

import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.data.monitor.HealthMonitor
import po.misc.functions.containers.DeferredContainer
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.containers.LambdaHolder
import po.misc.types.TaggedType
import po.misc.types.TypeData
import po.misc.types.interfaces.TagTypedClass

enum class ChunkTag {
    PickByIdChunk,
    SelectChunk,
    UpdateChunk,
    UpdateListChunk
}

enum class ChunkType {
    InputType,
    OutputType,
}


sealed class ExecutionChunkBase<DTO, D>() : CTX
        where DTO : ModelDTO, D : DataModel
{

    override val identity: CTXIdentity<out CTX> = asIdentity()

    abstract val chunkType : ChunkType
    val withInputValueLambda: LambdaHolder<D> = LambdaHolder(this)

    val healthMonitor: HealthMonitor<ExecutionChunkBase<DTO, D>> = HealthMonitor(this)

    val switchContainers: MutableMap<TypeData<*>, SwitchChunkContainer<*, *, *, DTO, D, *>> = mutableMapOf()

    abstract fun configure()

    fun <FD: DataModel> registerSwitchContainer(
        container: SwitchChunkContainer<*, *, *, DTO, D, *>,
        foreignDataType: TypeData<FD>,
    ): SwitchChunkContainer<*, *, *, DTO, D, *> {
        switchContainers.put(foreignDataType, container)
        return container
    }

}


sealed class SingleResultChunks<DTO, D>(
    val configurationBlock: SingleResultChunks<DTO, D>.() -> Unit
):ExecutionChunkBase<DTO, D>()
        where DTO : ModelDTO, D : DataModel
{

    internal val configContainer: LambdaHolder<SingleResultChunks<DTO, D>> = LambdaHolder(this)

    var activeResult: ResultSingle<DTO, D, *>? = null
    val resultContainer: DeferredContainer<ResultSingle<DTO, D, *>> = DeferredContainer(this)
    val withResultContainer: LambdaHolder<ResultSingle<DTO, D, *>> = LambdaHolder(this)

    init {
        configContainer.registerProvider(configurationBlock)
    }


//    fun <F: ModelDTO, FD: DataModel> createSwitchContainer(
//        dto: CommonDTO<DTO, D, *>,
//        foreignClass: DTOClass<F, FD, *>
//    ): SwitchChunkContainer<DTO, D, *, F, FD, *> {
//        val executionProvider = createDTOProvider(dto, foreignClass)
//        val switchContainer = SwitchChunkContainer(executionProvider)
//        val dataType = foreignClass.dataType.toTypeData()
//        switchContainers.put(dataType, switchContainer)
//        return switchContainer
//    }

    fun computeResult():ResultSingle<DTO, D, *>{
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


class PickByIdChunk<DTO, D>(
    override val tagType: TaggedType<PickByIdChunk<DTO, D>, ChunkTag>,
    configBlock: SingleResultChunks<DTO, D>.() -> Unit,
): SingleResultChunks<DTO, D>(configBlock), TagTypedClass<PickByIdChunk<DTO, D>, ChunkTag>
        where DTO : ModelDTO, D : DataModel
{
    override val chunkType: ChunkType = ChunkType.OutputType
    override val contextName: String get() = "PickByIdChunk2"

    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D>  create(
            configBlock: SingleResultChunks<DTO, D>.() -> Unit
        ):PickByIdChunk<DTO, D> where DTO : ModelDTO, D : DataModel{
            val tagType = TaggedType.create<PickByIdChunk<DTO, D>, ChunkTag>(ChunkTag.PickByIdChunk)
            return PickByIdChunk(tagType, configBlock)
        }
    }
}


class UpdateChunk<DTO, D>(
    override val tagType: TaggedType<UpdateChunk<DTO, D>, ChunkTag>,
    configBlock: SingleResultChunks<DTO, D>.() -> Unit,
):SingleResultChunks<DTO, D>(configBlock), TagTypedClass<UpdateChunk<DTO, D>, ChunkTag>
        where DTO: ModelDTO, D : DataModel
{
    override val chunkType: ChunkType = ChunkType.InputType
    override val contextName: String get() = "UpdateChunk"

    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D> create(
            configBlock:  SingleResultChunks<DTO, D>.() -> Unit
        ):UpdateChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
            val tagType = TaggedType.create<UpdateChunk<DTO, D>, ChunkTag>(ChunkTag.UpdateChunk)
            return UpdateChunk(tagType, configBlock)
        }
    }
}


sealed class ListResultChunks<DTO, D>(
    val configurationBlock: ListResultChunks<DTO, D>.() -> Unit
):ExecutionChunkBase<DTO, D>(), CTX
        where DTO : ModelDTO, D : DataModel
{
    protected var activeResult: ResultList<DTO, D, *>? = null
    internal val configContainer: LambdaHolder<ListResultChunks<DTO, D>> = LambdaHolder(this)
    val resultContainer: DeferredContainer<ResultList<DTO, D, *>> = DeferredContainer(this)
    val withResultContainer: LambdaHolder<ResultList<DTO, D, *>> = LambdaHolder(this)



    init {
        configContainer.registerProvider(configurationBlock)
    }

    fun computeResult():ResultList<DTO, D, *>{
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
    override val tagType: TaggedType<SelectChunk<DTO, D>, ChunkTag>,
    configBlock: ListResultChunks<DTO, D>.() -> Unit,
):ListResultChunks<DTO, D>(configBlock), TagTypedClass<SelectChunk<DTO, D>, ChunkTag>
        where DTO: ModelDTO, D : DataModel
{



    override val chunkType: ChunkType = ChunkType.OutputType
    override val contextName: String get() = "SelectChunk"

    init {
        resultContainer.provideReceiver(Unit)
    }

    companion object{
        fun <DTO, D> create(
            configBlock:  ListResultChunks<DTO, D>.() -> Unit
        ):SelectChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
            val tagType = TaggedType.create<SelectChunk<DTO, D>, ChunkTag>(ChunkTag.SelectChunk)
            return SelectChunk(tagType, configBlock)
        }
    }
}

class UpdateListChunk<DTO, D>(
    override val tagType: TaggedType<UpdateListChunk<DTO, D>, ChunkTag>,
    configBlock: ListResultChunks<DTO, D>.() -> Unit,
):ListResultChunks<DTO, D>(configBlock), TagTypedClass<UpdateListChunk<DTO, D>, ChunkTag>
        where DTO: ModelDTO, D : DataModel
{

    override val chunkType: ChunkType = ChunkType.InputType
    override val contextName: String get() = "UpdateListChunk"

    init {
        resultContainer.provideReceiver(Unit)
    }

    companion object{
        fun <DTO, D> create(
            configBlock:  ListResultChunks<DTO, D>.() -> Unit
        ):UpdateListChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
            val tagType = TaggedType.create<UpdateListChunk<DTO, D>, ChunkTag>(ChunkTag.UpdateListChunk)
            return UpdateListChunk(tagType, configBlock)
        }
    }
}