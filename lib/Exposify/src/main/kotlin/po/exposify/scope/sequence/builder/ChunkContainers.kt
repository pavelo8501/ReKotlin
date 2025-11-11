package po.exposify.scope.sequence.builder

import po.exposify.common.classes.ExposifyDebugger
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.anotations.SequenceDSL
import po.exposify.scope.sequence.anotations.SwitchDSL
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.ListTypeSwitchHandler
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.lognotify.TasksManaged
import po.misc.containers.backing.BackingContainer
import po.misc.containers.backing.backingContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.types.castListOrManaged

sealed class ChunkContainer<DTO, D>(
    internal val ownDescriptor: SequenceDescriptor<DTO, D>,
    internal val rootDescriptor: RootDescriptorBase<*, *>,
) : TasksManaged where DTO : ModelDTO, D : DataModel {
    private val executionChunks: MutableList<ChunkBase<DTO, D, *>> = mutableListOf()

    val chunks: List<ChunkBase<DTO, D, *>> get() = executionChunks.toList()
    val singleResultChunks: List<SingleResultChunks<DTO, D>> get() = executionChunks.castListOrManaged(this)
    val listResultChunks: List<ListResultChunks<DTO, D>> get() = executionChunks.castListOrManaged(this)
    val chunkCollectionSize: Int get() = executionChunks.size

    internal val debugger: ExposifyDebugger<ChunkContainer<DTO, D>, ContextData> =
        exposifyDebugger(this, ContextData) { ContextData(it.message) }

    fun registerChunk(chunk: ChunkBase<DTO, D, *>) {
        executionChunks.add(chunk)
    }

    fun registerChunks(chunks: List<ChunkBase<DTO, D,*>>) {
        chunks.forEach {
            registerChunk(it)
        }
    }

    override fun toString(): String = identity.detailedDump.toString()
}

@SequenceDSL
class SequenceChunkContainer<DTO, D>(
    val descriptor: RootDescriptorBase<DTO, D>,
) : ChunkContainer<DTO, D>(descriptor, descriptor) where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<SequenceChunkContainer<DTO, D>> get() =  asSubIdentity(descriptor)

    val listTypeHandler: ListTypeHandler<DTO, D> = ListTypeHandler(descriptor)
    val singleTypeHandler: SingleTypeHandler<DTO, D> = SingleTypeHandler(descriptor)
}

@SwitchDSL
class SwitchChunkContainer<DTO, D, F, FD>(
    val descriptor: SwitchDescriptorBase<DTO, D, F, FD>,
    val parentContainer: ChunkContainer<F, FD>,
) : ChunkContainer<DTO, D>(descriptor, parentContainer.rootDescriptor),
    TasksManaged
    where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
    override val identity: CTXIdentity<SwitchChunkContainer<DTO, D, F, FD>> = asSubIdentity(descriptor)
    val resultBacking: BackingContainer<ResultSingle<F, FD>> = backingContainerOf()

    val listHandler: ListTypeSwitchHandler<DTO, D, F, FD> = ListTypeSwitchHandler(descriptor)
    val singleHandler: SingleTypeSwitchHandler<DTO, D, F, FD> = SingleTypeSwitchHandler(descriptor)

    init {
        resultBacking.onValueSet { change ->
            println("change")
            println(this)
            println(change)
        }
    }

}
