package po.exposify.scope.sequence.builder

import po.exposify.common.classes.ExposifyDebugger
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.RootDTO
import po.exposify.dto.components.RootExecutionContext
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.TasksManaged
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass

class SequenceChunkContainer<DTO, D>(
    val execContext: RootExecutionContext<DTO, D, *>,
    val providedId: Long = 0
): TasksManaged, IdentifiableClass where DTO: ModelDTO, D: DataModel {

    override val identity: ClassIdentity = ClassIdentity.Companion.create("SequenceChunkContainer", execContext.contextName, providedId)

    private val executionChunks: MutableList<ExecutionChunkBase<DTO, D, *>> = mutableListOf()
    val chunks:List<ExecutionChunkBase<DTO, D, *>> get () = executionChunks.toList()
    val chunkCollectionSize: Int get() = executionChunks.size

    val debugger: ExposifyDebugger<SequenceChunkContainer<DTO, D>, ContextData> = exposifyDebugger(this, ContextData.Companion){
        ContextData(this, it.message)
    }
    var lastResult: ResultSingle<DTO, D, *>? = null

    fun registerChunk(chunk: ExecutionChunkBase<DTO, D, *>){
        executionChunks.add(chunk)
    }

    fun registerChunks(chunks: List<ExecutionChunkBase<DTO, D, *>>){
        chunks.forEach {

            executionChunks.add(it)
        }
    }

}