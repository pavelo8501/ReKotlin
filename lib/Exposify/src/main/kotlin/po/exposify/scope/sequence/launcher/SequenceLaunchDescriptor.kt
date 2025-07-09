package po.exposify.scope.sequence.launcher

import po.exposify.dto.RootDTO
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.ExecutionChunkBase
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SingleResultMarker
import po.exposify.scope.sequence.models.SequenceParameter
import po.misc.collections.StaticTypeKey
import po.misc.interfaces.CTX


sealed class SequenceDescriptorBase<DTO, D, P>(
   val  dtoClass: RootDTO<DTO, D, *>
): CTX  where DTO: ModelDTO, D: DataModel, P:Any{
    abstract override val contextName: String
    abstract val staticKey: StaticTypeKey<P>


    var containerBacking: SequenceChunkContainer<DTO, D>? = null
    val container:SequenceChunkContainer<DTO, D> get() = containerBacking.getOrOperations(this)


    fun  registerChunkContainer(
        sequenceContainer: SequenceChunkContainer<DTO, D>,
    ){
        println("RegisterChunkContainer")
        println(sequenceContainer.toString())
        sequenceContainer.chunks.forEach { it.healthMonitor.print() }
        containerBacking = sequenceContainer
    }
}

class IdLaunchDescriptor<DTO, D>(
   dtoClass: RootDTO<DTO, D, *>,
   val descriptor: SingleResultMarker
): SequenceDescriptorBase<DTO, D, Long>(dtoClass) where DTO: ModelDTO, D: DataModel
{
    override val contextName: String get() = "IdLaunchDescriptor"
    override val staticKey: StaticTypeKey<Long> = StaticTypeKey.createTypeKey(Long::class)
    var resultBacking: ResultSingle<DTO, D, *>? = null
}

class ParametrizedSinge<DTO, D, P>(
    dtoClass: RootDTO<DTO, D, *>,
    val descriptor: SingleResultMarker
): SequenceDescriptorBase<DTO, D, D>(dtoClass), CTX where DTO: ModelDTO, D: DataModel, P:D {

    override val contextName: String get() = "ParametrizedSinge"
    override val staticKey:StaticTypeKey<D> get() = dtoClass.dataType.toStaticTypeKey()
    private  var resultBacking: ResultSingle<DTO, D, *>? = null

    companion object{
        fun < DTO: ModelDTO, D: DataModel, P:D> ParametrizedSinge(
            dtoClass: RootDTO<DTO, D, *>,
            resultMarker: SingleResultMarker,
        ):ParametrizedSinge<DTO, D, P>{
           return ParametrizedSinge(dtoClass, resultMarker)
        }
    }
}

