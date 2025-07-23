package po.exposify.scope.sequence.builder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.RootExecutionContext
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.anotations.SequenceDSL
import po.exposify.scope.sequence.anotations.SwitchDSL
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.exposify.scope.sequence.launcher.SwitchDescriptorBase
import po.lognotify.TasksManaged
import po.misc.context.asIdentity
import po.misc.context.asSubIdentity
import po.misc.types.TypeData

sealed class ChunkContainer<DTO, D, E>(

)  where DTO: ModelDTO, D: DataModel, E: LongEntity{

    private val executionChunks: MutableList<ExecutionChunkBase<DTO, D>> = mutableListOf()
    val chunks:List<ExecutionChunkBase<DTO, D>> get () = executionChunks.toList()
    val singleResultChunks:List<SingleResultChunks<DTO, D>> get () = executionChunks.filterIsInstance<SingleResultChunks<DTO, D>>()
    val listResultChunks:List<ListResultChunks<DTO, D>> get () = executionChunks.filterIsInstance<ListResultChunks<DTO, D>>()

    val chunkCollectionSize: Int get() = executionChunks.size

    fun registerChunk(chunk: ExecutionChunkBase<DTO, D>){
        executionChunks.add(chunk)
    }

    fun registerChunks(chunks: List<ExecutionChunkBase<DTO, D>>){
        chunks.forEach {
            executionChunks.add(it)
        }
    }
}

@SequenceDSL
class SequenceChunkContainer<DTO, D, E>(
    val execContext: RootExecutionContext<DTO, D, E>,
    val providedId: Long = 0
):ChunkContainer<DTO, D, E>(), TasksManaged where DTO: ModelDTO, D: DataModel, E: LongEntity {

    override val identity = asIdentity(providedId)


    internal val debugger = exposifyDebugger(this, ContextData.Companion) { ContextData(it.message) }

    val listTypeHandler:  ListTypeHandler<DTO, D, E> = ListTypeHandler()
    val singleTypeHandler: SingleTypeHandler<DTO, D, E> = SingleTypeHandler()

    fun <F: ModelDTO, FD : DataModel, FE: LongEntity> singleSwitchContainers(
        typeData: TypeData<FD>,
    ): List<SwitchChunkContainer<F, FD, FE, DTO, D, E>> {
        val singleChunksWithSwitch = singleResultChunks.mapNotNull { it.switchContainers[typeData] }
        return singleChunksWithSwitch.filterIsInstance<SwitchChunkContainer<F, FD, FE, DTO, D, E>>()

        //val singleChunksWithSwitch = singleResultChunks.filter { it.switchContainers.isNotEmpty() }

    }
}


@SwitchDSL
class SwitchChunkContainer<DTO, D, E, F, FD, FE>(
    val descriptor:  SwitchDescriptorBase<DTO, D, E, F>,
    val parentDTO: CommonDTO<F, FD, FE>,
    val hostingChunk: ExecutionChunkBase<F, FD>,
): ChunkContainer<DTO, D, E>(), TasksManaged
        where DTO: ModelDTO, D: DataModel, E : LongEntity, F: ModelDTO, FD: DataModel, FE : LongEntity
{
    override val identity = asSubIdentity(this, descriptor.dtoClass)

    internal val debugger = exposifyDebugger(this, ContextData.Companion){ ContextData(it.message) }

    val singleTypeSwitchHandler: SingleTypeSwitchHandler<DTO, D, E, F, FD, FE> = SingleTypeSwitchHandler(descriptor, parentDTO)

}

