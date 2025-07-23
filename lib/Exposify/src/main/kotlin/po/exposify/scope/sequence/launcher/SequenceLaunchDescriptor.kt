package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.ListResultMarker
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SingleResultMarker
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.types.TypeData


sealed class SequenceDescriptorBase<DTO, D, E>(
   val  dtoBaseClass: DTOBase<DTO, D, E>
): CTX  where DTO: ModelDTO, D: DataModel, E: LongEntity{
    abstract override val contextName: String

    override val identity: CTXIdentity<out CTX> = asIdentity()

    val parameterType: TypeData<Long> = TypeData.create<Long>()
    val inputType: TypeData<D> get() = dtoBaseClass.dataType

    var containerBacking: SequenceChunkContainer<DTO, D, E>? = null
    val container:SequenceChunkContainer<DTO, D, E> get() = containerBacking.getOrOperations(this)

    fun  registerChunkContainer(
        sequenceContainer: SequenceChunkContainer<DTO, D, E>,
    ){
        println("RegisterChunkContainer")
        println(sequenceContainer.toString())
        sequenceContainer.chunks.forEach { it.healthMonitor.print() }
        containerBacking = sequenceContainer
    }
}

sealed class RootDescriptorBase<DTO, D, E>(
   val  dtoClass: RootDTO<DTO, D, E>
): SequenceDescriptorBase<DTO, D, E>(dtoClass),CTX
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
}

class SingleDescriptor<DTO, D, E>(
    dtoClass: RootDTO<DTO, D, E>,
    val marker: SingleResultMarker
): RootDescriptorBase<DTO, D, E>(dtoClass) where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val contextName: String get() = "SingleDescriptor"

}
class ListDescriptor<DTO, D, E>(
    dtoClass: RootDTO<DTO, D, E>,
    val marker: ListResultMarker
): RootDescriptorBase<DTO, D, E>(dtoClass) where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val contextName: String get() = "ListDescriptor"

}


sealed class SwitchDescriptorBase<DTO, D, E, F>(
   val dtoClass: DTOClass<DTO, D, E>,
   val rootDescriptor:SequenceDescriptorBase<F, *, *>
): SequenceDescriptorBase<DTO, D, E>(dtoClass)
        where DTO: ModelDTO, D: DataModel, E:LongEntity, F : ModelDTO
{

}

class SwitchSingeDescriptor<DTO, D, E, F>(
    dtoClass: DTOClass<DTO, D, E>,
    rootDescriptor:SequenceDescriptorBase<F, *, *>,
    val marker: SingleResultMarker
): SwitchDescriptorBase<DTO, D, E, F>(dtoClass, rootDescriptor), CTX where DTO: ModelDTO, D: DataModel, E: LongEntity, F : ModelDTO{

   override val contextName: String get() = "SwitchDescriptorSinge"

}

class SwitchListDescriptor<DTO, D, E, F>(
    dtoClass: DTOClass<DTO, D, E>,
    rootDescriptor:SequenceDescriptorBase<F, *, *>,
    val marker: ListResultMarker
): SwitchDescriptorBase<DTO, D, E, F>(dtoClass, rootDescriptor), CTX where DTO: ModelDTO, D: DataModel, E: LongEntity, F : ModelDTO{

    override val contextName: String get() = "SwitchListDescriptor"


}



//class ParametrizedSwitchSinge<DTO, D, E>(
//    val dtoClass: DTOClass<DTO, D, E>,
//    val marker: SingleResultMarker
//): SwitchDescriptorBase<DTO, D, E, D>(dtoClass), CTX where DTO: ModelDTO, D: DataModel, E : LongEntity{
//
//    override val contextName: String get() = "ParametrizedSinge"
//    val inputType: TypeData<D> get() = dtoClass.dataType.toTypeData()
//}


//class LongSingeDescriptor<DTO, D, E>(
//   dtoClass: RootDTO<DTO, D, E>,
//   val marker: SingleResultMarker
//): RootDescriptorBase<DTO, D, E>(dtoClass) where DTO: ModelDTO, D: DataModel, E : LongEntity
//{
//    override val contextName: String get() = "IdLaunchDescriptor"
//
//}
//
//class ParametrizedSingeDescriptor<DTO, D, E>(
//    dtoClass: RootDTO<DTO, D, E>,
//    val marker: SingleResultMarker
//): RootDescriptorBase<DTO, D, E>(dtoClass), CTX where DTO: ModelDTO, D: DataModel, E : LongEntity{
//
//    override val contextName: String get() = "ParametrizedSinge"
//    val inputType:TypeData<D> get() = dtoClass.dataType.toTypeData()
//}


