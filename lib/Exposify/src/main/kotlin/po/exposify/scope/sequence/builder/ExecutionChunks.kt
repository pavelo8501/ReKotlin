package po.exposify.scope.sequence.builder

import po.exposify.dto.components.RootExecutionContext
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.data.monitor.HealthMonitor
import po.misc.functions.containers.DeferredContainer
import po.misc.functions.containers.DeferredInputContainer
import po.misc.functions.containers.LambdaContainer
import po.misc.interfaces.CTX
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

enum class ChunkLambdaType {
    WithInputValue,
}


sealed class ExecutionChunkBase<DTO, D, P>(
): CTX where DTO: ModelDTO, D: DataModel, P: Any {

    @PublishedApi
    internal val configContainer: LambdaContainer<ExecutionChunkBase<DTO, D, P>> = LambdaContainer(this)

    var activeResult:ResultBase<DTO, D>? = null
    @PublishedApi
    internal val resultContainer: DeferredInputContainer<P, ResultBase<DTO, D>> = DeferredInputContainer(this)

    @PublishedApi
    internal val inputContainer: DeferredContainer<P> = DeferredContainer(this)
    val currentValue: P? get() = inputContainer.currentValue

    @PublishedApi
    internal val withInputContainer: LambdaContainer<P> = LambdaContainer(this)

    val healthMonitor: HealthMonitor<ExecutionChunkBase<DTO, D, P>> = HealthMonitor(this)

    abstract val configurationBlock: ExecutionChunkBase<DTO, D, P>.() -> Unit
    fun configure() {
        configContainer.resolve(this)
    }

    fun computeResult():ResultBase<DTO, D>{
        val parameter = inputContainer.resolve()
        if(withInputContainer.isLambdaProvided){
            withInputContainer.resolve(parameter)
        }
      //  resultContainer.provideReceiver(parameter)
        val result = resultContainer.resolve(parameter)
        activeResult = result
        return result
    }

}

class InsertChunk<DTO, D, P>(
    override val tagType: TaggedType<InsertChunk<DTO, D, P>, ChunkType>,
    override val configurationBlock: ExecutionChunkBase<DTO, D, P>.() -> Unit,
): ExecutionChunkBase<DTO, D, P>(), TagTypedClass<InsertChunk<DTO, D, P>, ChunkType> where DTO: ModelDTO, D : DataModel, P:D{


    override val contextName: String get() = "InsertChunk"

    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D, P> create(
            configBlock:  ExecutionChunkBase<DTO, D, P>.() -> Unit
        ):InsertChunk<DTO, D, P> where DTO: ModelDTO, D : DataModel, P:D{
            val tagType = TaggedType.create<InsertChunk<DTO, D, P>, ChunkType>(ChunkType.InsertChunk)
            return InsertChunk(tagType, configBlock)
        }
    }
}

class PickByIdChunk<DTO, D>(
    override val tagType: TaggedType<PickByIdChunk<DTO, D>, ChunkType>,
    override val configurationBlock: ExecutionChunkBase<DTO, D, Long>.() -> Unit,
): ExecutionChunkBase<DTO, D, Long>(), TagTypedClass<PickByIdChunk<DTO, D>, ChunkType>
        where DTO: ModelDTO, D : DataModel {

    override val contextName: String get() {
      return "PickByIdChunk"
    }

    override fun toString(): String {
        return tagType.normalizedSimpleString()
    }

    companion object{
        fun <DTO, D>  create(
          //  context:RootExecutionContext<DTO, D, *>,
            configBlock: ExecutionChunkBase<DTO, D, Long>.() -> Unit
        ):PickByIdChunk<DTO, D> where DTO: ModelDTO, D : DataModel{
            val tagType = TaggedType.create<PickByIdChunk<DTO, D>, ChunkType>(ChunkType.PickByIdChunk)
            return PickByIdChunk(tagType, configBlock)
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
