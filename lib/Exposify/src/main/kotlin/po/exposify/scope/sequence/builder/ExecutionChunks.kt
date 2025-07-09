package po.exposify.scope.sequence.builder

import po.exposify.dto.RootDTO
import po.exposify.dto.components.RootExecutionContext
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.lognotify.anotations.LogOnFault
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.CallbackPayload
import po.misc.callbacks.builders.callbackManager
import po.misc.callbacks.builders.registerPayload
import po.misc.collections.StaticTypeKey
import po.misc.data.monitor.HealthMonitor
import po.misc.exceptions.throwManaged
import po.misc.functions.containers.DeferredResultContainer
import po.misc.functions.containers.PromiseResultContainer
import po.misc.functions.hooks.ReactiveComponent
import po.misc.functions.hooks.ReactiveHooks
import po.misc.interfaces.CTX
import po.misc.interfaces.CtxId
import po.misc.reflection.anotations.ManagedProperty
import po.misc.types.TaggedType
import po.misc.types.interfaces.TagTypedClass

enum class ChunkType {
    InsertChunk,
    PickByIdChunk
}

enum class ChunkEvent {
    Initialized,
    InputReceived,
    ResultEvaluation,
    TriggerEvaluation,
    ResultReceived
}

sealed class ExecutionChunkBase<DTO, D, P>(
    private val rootClass: RootDTO<DTO, D, *>,
    val inputContainer: PromiseResultContainer<P>
): CTX, ReactiveComponent<P>  where DTO: ModelDTO, D: DataModel, P: Any {

    @ManagedProperty
    abstract var executionContext: RootExecutionContext<DTO, D, *>

    @ManagedProperty
    abstract var actionBlockLambda: ExecutionChunkBase<DTO, D, P>.() -> ResultBase<DTO, D>

    @ManagedProperty
    protected var resultEvaluationLambda: ((P) -> ResultBase<DTO, D>)? = null

    private var activeResultBacking: ResultBase<DTO, D>? = null
    protected val activeResult: ResultBase<DTO, D>
        get() {
            healthMonitor.ifWillCrash("activeResultBacking") { activeResultBacking == null }
            return activeResultBacking.getOrOperations("ActiveResultBacking", this)
        }

    @ManagedProperty
    val resultLambdaSet: Boolean get() = resultEvaluationLambda != null

    //internal var inputContainer:PromiseResultContainer<P>? = null
    lateinit var parameterTypeKey: StaticTypeKey<P>

    override val hooks: ReactiveHooks<P> = ReactiveHooks<P>()

    @ManagedProperty
    override val isResolved: Boolean get() = activeResultBacking != null

    private var inputParameter: P? = null
    override val currentValue: P? get() = inputParameter
    internal fun provideInputParameter(parameter: P): P {
        inputParameter = parameter
        return parameter
    }

    internal fun provideResult(result: ResultBase<DTO, D>): ResultBase<DTO, D> {
        activeResultBacking = result
        return result
    }

    @ManagedProperty
    override val isLambdaProvided: Boolean get() = true

    val healthMonitor: HealthMonitor<ExecutionChunkBase<DTO, D, P>> = HealthMonitor(this)


    val onInitialized: CallbackPayload<ChunkEvent, ExecutionChunkBase<DTO, D, P>> =
        CallbackPayload.createPayload(ChunkEvent.Initialized)

    val notifier: CallbackManager<ChunkEvent> = callbackManager(
        { registerPayload(onInitialized) },
    )

    fun returnResult(): ResultBase<DTO, D> {
        healthMonitor.input("returnResult", "", "$activeResult")
        return activeResult
    }

    fun updateActionBlock(block: ExecutionChunkBase<DTO, D, P>.() -> ResultBase<DTO, D>) {
        healthMonitor.input("updateActionBlock", "block", "$block")
        actionBlockLambda = block
    }

    private fun resolveResult(): ResultBase<DTO, D> {
        TODO("Not yet")
    }

    internal fun saveInputContainer(container: PromiseResultContainer<P>) {
        healthMonitor.input("SaveInputContainer", "container", "${container.ctx}")
        // inputContainer = container
        //onInitialized.triggerForAll(this)
    }

    internal fun getOrResolve(): ResultBase<DTO, D> {
        healthMonitor.input("getOrResolve", "activeResultBacking", "$activeResultBacking")
        return activeResultBacking ?: resolveResult()
    }

    internal fun provideContext(context: RootExecutionContext<DTO, D, *>) {
        healthMonitor.input("provideContext", "this changed context. New context", "$executionContext")
        println("$this changed context. New context${context}")
        executionContext = context
    }

    private fun resultComputation() {
        healthMonitor.input("resultComputation", "inputContainer", "${inputContainer.value}")
        inputParameter = inputContainer.value
        inputParameter?.let {
            healthMonitor.ifWillCrash("resultEvaluationLambda") { resultEvaluationLambda == null }
            resultEvaluationLambda?.invoke(it)
        }
    }

    fun provideParameterTypeKey(typeKey: StaticTypeKey<P>) {
        healthMonitor.input("provideParameterTypeKey", "inputContainer", "$typeKey")
        parameterTypeKey = typeKey
    }

    fun provideInput(parameter: P) {
        healthMonitor.input("provideInput", "parameter", "$parameter")
        inputParameter = parameter
    }

    /**
     * Register a lambda to be called after evaluation of actionBlock and before the result is returned.
     * Typically used for registering or propagating the result elsewhere.
     */
    fun onResultRequested(resultBlock: (P) -> ResultBase<DTO, D>) {
        healthMonitor.input("onResultRequested", "resultBlock", "$resultBlock")
        resultEvaluationLambda = resultBlock
    }

    fun computeResult() {
        healthMonitor.input("computeResult", "activeResultBacking", "$activeResultBacking")
        if (activeResultBacking == null && inputParameter == null) {
            healthMonitor.action("inputContainer(DeferredResult) ", "Request Value") {
                provideInputParameter(inputContainer.value)
            }
            inputParameter?.let { inputParameter ->
                healthMonitor.action("computeResult", "resultEvaluationLambda") {
                    resultEvaluationLambda?.invoke(inputParameter)?.let { result ->
                        provideResult(result)
                    }
                }
            }
        } else {
            healthMonitor.action("actionBlockLambda", "invoked") {
                actionBlockLambda.invoke(this)
            }
        }
    }
}

class InsertChunk<DTO, D, P>(
    override val tagType: TaggedType<InsertChunk<DTO, D, P>, ChunkType>,
    dtoClass: RootDTO<DTO, D, *>,
    inputContainer: PromiseResultContainer<P>,
    override var executionContext: RootExecutionContext<DTO, D, *>,
    override var actionBlockLambda: ExecutionChunkBase<DTO, D, P>.() -> ResultBase<DTO, D>
): ExecutionChunkBase<DTO, D, P>(dtoClass, inputContainer), TagTypedClass<InsertChunk<DTO, D, P>, ChunkType> where DTO: ModelDTO, D : DataModel, P:D{


    override val contextName: String get() = "InsertChunk"

    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D, P> create(
            dtoClas:RootDTO<DTO, D, *>,
            inputContainer: PromiseResultContainer<P>,
            executionContext: RootExecutionContext<DTO, D, *>,
            lambda: ExecutionChunkBase<DTO, D, P>.() -> ResultBase<DTO, D>
        ):InsertChunk<DTO, D, P> where DTO: ModelDTO, D : DataModel, P:D{
            val tagType = TaggedType.create<InsertChunk<DTO, D, P>, ChunkType>(ChunkType.InsertChunk)
            return InsertChunk(tagType, dtoClas,inputContainer,  executionContext, lambda)
        }
    }
}

class PickByIdChunk<DTO, D>(
    override val tagType: TaggedType<PickByIdChunk<DTO, D>, ChunkType>,
    dtoClass: RootDTO<DTO, D, *>,
    inputContainer: PromiseResultContainer<Long>,
    override var executionContext: RootExecutionContext<DTO, D, *>,
    override var actionBlockLambda: ExecutionChunkBase<DTO, D, Long>.() -> ResultBase<DTO, D>
): ExecutionChunkBase<DTO, D, Long>(dtoClass, inputContainer), TagTypedClass<PickByIdChunk<DTO, D>, ChunkType>
        where DTO: ModelDTO, D : DataModel
{

    override val contextName: String get() {
      return "PickByIdChunk"
    }

    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D>  create(
            dtoClass:RootDTO<DTO, D, *>,
            inputContainer: PromiseResultContainer<Long>,
            executionContext: RootExecutionContext<DTO, D, *>,
            lambda: ExecutionChunkBase<DTO, D, Long>.() -> ResultBase<DTO, D>
        ):PickByIdChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
            val tagType = TaggedType.create<PickByIdChunk<DTO, D>, ChunkType>(ChunkType.PickByIdChunk)
            return PickByIdChunk(tagType, dtoClass, inputContainer,  executionContext, lambda)
        }
    }
}



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
//data class UpdateChunk<DTO, D, E>(
//    val dtoClass: RootDTO<DTO, D, E>,
//    override val lambda: () -> ResultSingle<DTO, D, E>
//):ExecutionChunkBase<DTO, D, E, D>(dtoClass) where DTO: ModelDTO, D : DataModel, E: LongEntity{
//
//    override fun execute(parameter: D): ResultBase<DTO, D, E> {
//        TODO("Not yet implemented")
//    }
//
//    companion object{
//        fun <DTO, D, E> create(
//            dtoClass:RootDTO<DTO, D, E>,
//            lambda: () -> ResultSingle<DTO, D, E>
//        ):UpdateChunk<DTO, D, E> where DTO: ModelDTO, D : DataModel, E: LongEntity{
//            return UpdateChunk(dtoClass, lambda)
//        }
//    }
//}
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
