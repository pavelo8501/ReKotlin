package po.exposify.scope.sequence.launcher

import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SingleResultMarker
import po.misc.collections.StaticTypeKey
import po.misc.interfaces.CTX
import po.misc.types.TypeData


sealed class SequenceDescriptorBase<DTO, D, P>(
   val  dtoBaseClass: DTOBase<DTO, D, *>
): CTX  where DTO: ModelDTO, D: DataModel, P:Any{
    abstract override val contextName: String
    abstract val parameterType: TypeData<P>

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

sealed class RootDescriptorBase<DTO, D, P>(
    dtoBaseClass: DTOBase<DTO, D, *>
): SequenceDescriptorBase<DTO, D, P>(dtoBaseClass),CTX
        where DTO: ModelDTO, D: DataModel, P:Any
{

}

sealed class SwitchDescriptorBase<DTO, D, P>(
    dtoBaseClass: DTOBase<DTO, D, *>
): SequenceDescriptorBase<DTO, D, P>(dtoBaseClass),  CTX  where DTO: ModelDTO, D: DataModel, P:Any{

}


class LongSingeDescriptor<DTO, D>(
   dtoClass: RootDTO<DTO, D, *>,
   val descriptor: SingleResultMarker
): RootDescriptorBase<DTO, D, Long>(dtoClass) where DTO: ModelDTO, D: DataModel
{
    override val contextName: String get() = "IdLaunchDescriptor"
    override val parameterType: TypeData<Long> =  TypeData.create<Long>()
    var resultBacking: ResultSingle<DTO, D, *>? = null
}

class ParametrizedSingeDescriptor<DTO, D>(
    val dtoClass: RootDTO<DTO, D, *>,
    val descriptor: SingleResultMarker
): RootDescriptorBase<DTO, D, D>(dtoClass), CTX where DTO: ModelDTO, D: DataModel{

    override val contextName: String get() = "ParametrizedSinge"
    override val parameterType:TypeData<D> get() = dtoClass.dataType.toTypeData()
    private  var resultBacking: ResultSingle<DTO, D, *>? = null

    companion object{
        fun < DTO: ModelDTO, D: DataModel, P:D> parametrizedSinge(
            dtoClass: RootDTO<DTO, D, *>,
            resultMarker: SingleResultMarker,
        ):ParametrizedSingeDescriptor<DTO, D>{
           return ParametrizedSingeDescriptor(dtoClass, resultMarker)
        }
    }
}


class SwitchSinge<DTO, D>(
    val dtoClass: DTOClass<DTO, D, *>,
    val descriptor: SingleResultMarker
): SwitchDescriptorBase<DTO, D, Long>(dtoClass), CTX where DTO: ModelDTO, D: DataModel{

    override val contextName: String get() = "ParametrizedSinge"
    override val parameterType:TypeData<Long> get() =  TypeData.create<Long>()
}

class ParametrizedSwitchSinge<DTO, D>(
    val dtoClass: DTOClass<DTO, D, *>,
    val descriptor: SingleResultMarker
): SwitchDescriptorBase<DTO, D, D>(dtoClass), CTX where DTO: ModelDTO, D: DataModel{

    override val contextName: String get() = "ParametrizedSinge"
    override val parameterType: TypeData<D> get() = dtoClass.dataType.toTypeData()


}



