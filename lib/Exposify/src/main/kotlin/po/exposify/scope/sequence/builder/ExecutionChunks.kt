package po.exposify.scope.sequence.builder

import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.createDTOProvider
import po.exposify.dto.components.result.ResultBase
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
    InsertChunk,
    PickByIdChunk,
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

    @PublishedApi

  //  internal val configContainer: LambdaContainer<ExecutionChunkBase<DTO, D, P>> = LambdaContainer(this)
   // abstract val configurationBlock: ExecutionChunkBase<DTO, D, P>.() -> Unit
    //abstract var activeResult:ResultBase<DTO, D>?


    internal val resultContainer: LazyExecutionContainer<P, ResultBase<DTO, D>> = LazyExecutionContainer(this)
    internal val withResultContainer: LambdaContainer<ResultSingle<DTO, D, *>> = LambdaContainer(this)

    @PublishedApi
    internal val inputContainer: DeferredContainer<P> = DeferredContainer(this)
    val currentValue: P? get() = inputContainer.currentValue

    val withInputContainer: LambdaContainer<P> = LambdaContainer(this)

   // protected abstract val withResultContainer: LambdaContainer<ResultBase<DTO, D>>

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

    internal val switchLambdaContainer: LazyContainerWithReceiver<SwitchChunkContainer<DTO, D, *, *>, SequenceParameter<*>, LazyExecutionContainer<*, ResultBase<*, *>>>
        = LazyContainerWithReceiver(this)

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
        activeResult = result as ResultSingle<DTO, D, *>
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


//    fun computeResult():ResultSingle<DTO, D, *>{
//        val parameter = preResultComputation()
//        val result = resultContainer.resolve(parameter)
//        activeResult = result as ResultSingle<DTO, D, *>
//        if(withResultContainer.isLambdaProvided){
//            withResultContainer.resolve(result)
//        }
//        return result
//    }

//    override fun <F: ModelDTO, FD: DataModel> createSwitchContainer(
//        dto: CommonDTO<DTO, D, *>,
//        foreignClass: DTOClass<F, FD, *>
//    ): SwitchChunkContainer<DTO, D, F, FD> {
//
//        val executionProvider = createDTOProvider(dto, foreignClass)
//        val switchContainer = SwitchChunkContainer(executionProvider)
//        val dataType = dto.dataType.toTypeData()
//        switchContainers.put(dataType, switchContainer)
//        return switchContainer
//    }

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



//class InsertChunk<DTO, D>(
//    override val tagType: TaggedType<InsertChunk<DTO, D>, ChunkType>,
//    val configBlock: SingleResultChunk<DTO, D, D>.() -> Unit,
//): ExecutionChunkBase<DTO, D, D>(), SingleResultChunk<DTO, D, D>, TagTypedClass<InsertChunk<DTO, D>, ChunkType>
//        where DTO: ModelDTO, D : DataModel
//{
//
//    override var activeResult: ResultSingle<DTO, D, *>? = null
//    override val contextName: String get() = "InsertChunk"
//
//    override val configurationBlock: ResultChunk<DTO, D, D>.() -> Unit get(){
//       return configBlock as ResultChunk<DTO, D, D>.() -> Unit
//    }
//
//    init {
//        configContainer.registerProvider(configurationBlock)
//    }
//
//    fun computeResult():ResultSingle<DTO, D, *>{
//        val parameter = preResultComputation()
//        val result = resultContainer.resolve(parameter)
//        activeResult = result as ResultSingle<DTO, D, *>
//        if(withResultContainer.isLambdaProvided){
//            withResultContainer.resolve(result)
//        }
//        return result
//    }
//
//    val switchContainers: MutableMap<TypeData<CommonDTO<*, *, *>>, SwitchChunkContainer<DTO, D, *, *>> = mutableMapOf()
//
//    override fun <F: ModelDTO, FD: DataModel> createSwitchContainer(
//        dto: CommonDTO<DTO, D, *>,
//        foreignClass: DTOClass<F, FD, *>
//    ): SwitchChunkContainer<DTO, D, F, FD> {
//        val executionProvider = createDTOProvider(dto, foreignClass)
//        val switchContainer = SwitchChunkContainer(executionProvider)
//        val commonType = dto.commonType.castOrOperations<TypeData<CommonDTO<*, *, *>>>(exceptionPayload)
//        switchContainers.put(commonType, switchContainer)
//        return switchContainer
//    }
//
//    override fun toString(): String {
//        return tagType.normalizedSimpleString()
//    }
//
//    companion object{
//        fun <DTO, D> create(
//            configBlock:  SingleResultChunk<DTO, D, D>.() -> Unit
//        ):InsertChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
//            val tagType = TaggedType.create<InsertChunk<DTO, D>, ChunkType>(ChunkType.InsertChunk)
//            return InsertChunk(tagType, configBlock)
//        }
//    }
//}


//
//data class SelectChunk<DTO, D, E>(
//    val dtoClass: RootDTO<DTO, D, E>,
//    override val lambda: () -> ResultList<DTO, D, E>
//):ExecutionChunkBase<DTO, D, E, D>(dtoClass) where DTO: ModelDTO, D : DataModel, E: LongEntity{
//
//    override fun execute(parameter: D): ResultBase<DTO, D, E> {
//        TODO("Not yet implemented")
//    }
//
//    companion object{
//        fun <DTO, D, E> create(
//            dtoClass:RootDTO<DTO, D, E>,
//            lambda: () -> ResultList<DTO, D, E>
//        ):SelectChunk<DTO, D, E> where DTO: ModelDTO, D : DataModel, E: LongEntity{
//            return SelectChunk(dtoClass, lambda)
//        }
//    }
//}
//
//data class InsertListChunk<DTO, D, E>(
//    val dtoClass: RootDTO<DTO, D, E>,
//    override val lambda: () -> ResultList<DTO, D, E>
//):ExecutionChunkBase<DTO, D, E, List<D>>(dtoClass) where DTO: ModelDTO, D : DataModel, E: LongEntity{
//
//    override fun execute(parameter: List<D>): ResultBase<DTO, D, E> {
//        TODO("Not yet implemented")
//    }
//
//    companion object{
//        fun <DTO, D, E> create(
//            dtoClass:RootDTO<DTO, D, E>,
//            lambda: () -> ResultList<DTO, D, E>
//        ):InsertListChunk<DTO, D, E> where DTO: ModelDTO, D : DataModel, E: LongEntity{
//            return InsertListChunk(dtoClass, lambda)
//        }
//    }
//}
//
//
//data class PickByIdChunk<DTO, D, E>(
//    val dtoClass: RootDTO<DTO, D, E>,
//    val execCtx:RootExecCTX<DTO, D, E>,
//     val lambda: RootExecCTX<DTO, D, E>.(Long) -> ResultSingle<DTO, D, E>
//) where DTO: ModelDTO, D : DataModel, E: LongEntity{
//
//    fun execute(parameter: Long): ResultBase<DTO, D, E> {
//        TODO("Not yet implemented")
//    }
//
//    companion object{
//        fun <DTO, D, E>  create(
//            dtoClass:RootDTO<DTO, D, E>,
//            execCtx: RootExecCTX<DTO, D, E>,
//            lambda: RootExecCTX<DTO, D, E>.(Long) -> ResultSingle<DTO, D, E>
//        ):PickByIdChunk<DTO, D, E> where DTO: ModelDTO, D : DataModel, E: LongEntity{
//            return PickByIdChunk(dtoClass,execCtx,  lambda)
//        }
//    }
//}
//
//data class PickChunk<DTO, D, E, T>(
//    val dtoClass: RootDTO<DTO, D, E>,
//    override val lambda: () -> ResultSingle<DTO, D, E>
//):ExecutionChunkBase<DTO, D, E, T>(dtoClass) where DTO: ModelDTO, D : DataModel, E: LongEntity, T: IdTable<Long>{
//
//    override fun execute(parameter: T): ResultSingle<DTO, D, E> {
//        TODO("Not yet implemented")
//    }
//
//    companion object{
//        fun <DTO, D, E, T> create(
//            dtoClass:RootDTO<DTO, D, E>,
//            lambda: () -> ResultSingle<DTO, D, E>
//        ):PickChunk<DTO, D, E, T> where DTO: ModelDTO, D : DataModel, E: LongEntity,  T: IdTable<Long>{
//            return PickChunk(dtoClass, lambda)
//        }
//    }
//}
//
//

//
//data class UpdateListChunk<DTO, D, E>(
//    val dtoClass: RootDTO<DTO, D, E>,
//    override val lambda: () -> ResultList<DTO, D, E>
//):ExecutionChunkBase<DTO, D, E, List<D>>(dtoClass) where DTO: ModelDTO, D : DataModel, E: LongEntity{
//
//    override fun execute(parameter: List<D>): ResultBase<DTO, D, E> {
//
//        TODO("Not yet implemented")
//    }
//
//    companion object{
//        fun <DTO, D, E> create(
//            dtoClass:RootDTO<DTO, D, E>,
//            lambda: () -> ResultList<DTO, D, E>
//        ):UpdateListChunk<DTO, D, E> where DTO: ModelDTO, D : DataModel, E: LongEntity{
//            return UpdateListChunk(dtoClass, lambda)
//        }
//    }
//}
