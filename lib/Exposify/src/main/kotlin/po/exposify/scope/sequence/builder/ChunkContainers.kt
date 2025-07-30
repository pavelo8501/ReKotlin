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

sealed class ChunkContainer<DTO, D>(

)  where DTO: ModelDTO, D: DataModel{

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
class SequenceChunkContainer<DTO, D>(
    val execContext: RootExecutionContext<DTO, D, *>,
    val providedId: Long = 0
):ChunkContainer<DTO, D>(), TasksManaged where DTO: ModelDTO, D: DataModel{

    override val identity = asIdentity(providedId)


    internal val debugger = exposifyDebugger(this, ContextData.Companion) { ContextData(it.message) }

    val listTypeHandler:  ListTypeHandler<DTO, D> = ListTypeHandler()
    val singleTypeHandler: SingleTypeHandler<DTO, D> = SingleTypeHandler()

    fun <F: ModelDTO, FD : DataModel> singleSwitchContainers(
        typeData: TypeData<FD>,
    ): List<SwitchChunkContainer<F, FD, DTO, D>> {
        val singleChunksWithSwitch = singleResultChunks.mapNotNull { it.switchContainers[typeData] }
        return singleChunksWithSwitch.filterIsInstance<SwitchChunkContainer<F, FD, DTO, D>>()

        //val singleChunksWithSwitch = singleResultChunks.filter { it.switchContainers.isNotEmpty() }

    }
}


@SwitchDSL
class SwitchChunkContainer<DTO, D, F, FD>(
    val descriptor:  SwitchDescriptorBase<DTO, D, F>,
    val parentDTO: CommonDTO<F, FD, *>,
    val hostingChunk: ExecutionChunkBase<F, FD>,
): ChunkContainer<DTO, D>(), TasksManaged
        where DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel
{
    override val identity = asSubIdentity(this, descriptor.dtoClass)

    internal val debugger = exposifyDebugger(this, ContextData.Companion){ ContextData(it.message) }

    val singleTypeSwitchHandler: SingleTypeSwitchHandler<DTO, D, F, FD> = SingleTypeSwitchHandler(descriptor, parentDTO)

}

