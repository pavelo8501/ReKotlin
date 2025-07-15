package po.exposify.scope.sequence.builder

import po.exposify.common.classes.ExposifyDebugger
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.components.DTOExecutionContext
import po.exposify.dto.components.ExecutionContext
import po.exposify.dto.components.RootExecutionContext
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.anotations.SequenceDSL
import po.exposify.scope.sequence.anotations.SwitchDSL
import po.lognotify.TasksManaged
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.types.TypeData

sealed class ChunkContainer<CHUNK_DTO, CHUNK_DATA>(
    private val context: ExecutionContext<*, *, *>,
)  where CHUNK_DTO: ModelDTO, CHUNK_DATA: DataModel{

    private val executionChunks: MutableList<ExecutionChunkBase<CHUNK_DTO, CHUNK_DATA, *>> = mutableListOf()
    val chunks:List<ExecutionChunkBase<CHUNK_DTO, CHUNK_DATA, *>> get () = executionChunks.toList()
    val singleResultChunks:List<SingleResultChunks<CHUNK_DTO, CHUNK_DATA, *>> get () = executionChunks.filterIsInstance<SingleResultChunks<CHUNK_DTO, CHUNK_DATA, *>>()

    val chunkCollectionSize: Int get() = executionChunks.size

    fun registerChunk(chunk: ExecutionChunkBase<CHUNK_DTO, CHUNK_DATA, *>){
        executionChunks.add(chunk)
    }

    fun registerChunks(chunks: List<ExecutionChunkBase<CHUNK_DTO, CHUNK_DATA, *>>){
        chunks.forEach {
            executionChunks.add(it)
        }
    }
}

@SequenceDSL
class SequenceChunkContainer<DTO, D>(
    val execContext: RootExecutionContext<DTO, D, *>,
    val providedId: Long = 0
):ChunkContainer<DTO, D>(execContext), TasksManaged, IdentifiableClass where DTO: ModelDTO, D: DataModel {

    override val identity: ClassIdentity =
        ClassIdentity.Companion.create("SequenceChunkContainer", execContext.contextName, providedId)
    val debugger: ExposifyDebugger<SequenceChunkContainer<DTO, D>, ContextData> =
        exposifyDebugger(this, ContextData.Companion) {
            ContextData(this, it.message)
        }

    fun <F : ModelDTO, FD : DataModel> findFirstSwitchContainer(
        typeData: TypeData<FD>,
        singleResult: Boolean
    ): SwitchChunkContainer<DTO, D, *, *>?
    {
        val container = if (singleResult) {
            val result = singleResultChunks.mapNotNull { it.switchContainers[typeData] }
            result.firstOrNull()
        } else {
            TODO("Not yet implemented")
        }
        return container
    }
}


@SwitchDSL
class SwitchChunkContainer<DTO, D, F, FD>(
    val execContext: DTOExecutionContext<DTO, D, *, F, FD, *>,
    val providedId: Long = 0
): ChunkContainer<F, FD>(execContext.selfAsBase), TasksManaged, IdentifiableClass
        where DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel
{
    override val identity: ClassIdentity = ClassIdentity.Companion.create("SwitchChunkContainer", execContext.contextName, providedId)
    val debugger: ExposifyDebugger<SwitchChunkContainer<DTO, D, F, FD>, ContextData> = exposifyDebugger(this, ContextData.Companion){
        ContextData(this, it.message)
    }
}

