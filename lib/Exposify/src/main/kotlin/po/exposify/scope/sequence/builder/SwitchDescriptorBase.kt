package po.exposify.scope.sequence.builder

import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.scope.sequence.builder.SequenceDescriptor
import po.misc.containers.BackingContainer
import po.misc.containers.backingContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.common.ExceptionFallback

sealed class SwitchDescriptorBase<DTO, D, F, FD>(
    override val dtoClass: DTOClass<DTO, D, *>,
    val parentDTOClass: DTOBase<F, FD, *>,
) : SequenceDescriptor<DTO, D>
    where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
    override val containerBacking: BackingContainer<ChunkContainer<DTO, D>> = backingContainerOf()

    fun registerChunkContainer(sequenceContainer: SwitchChunkContainer<DTO, D, F, FD>) =
        containerBacking.provideValue(sequenceContainer)
}

class SwitchSingeDescriptor<DTO, D, F, FD>(
    dtoClass: DTOClass<DTO, D, *>,
    parentDTOClass: DTOBase<F, FD, *>
) : SwitchDescriptorBase<DTO, D, F, FD>(dtoClass, parentDTOClass) where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
    override val identity: CTXIdentity<SwitchSingeDescriptor<DTO, D, F, FD>> = asIdentity()
}

class SwitchListDescriptor<DTO, D, F, FD>(
    dtoClass: DTOClass<DTO, D, *>,
    parentDTOClass: DTOBase<F, FD, *>,
) : SwitchDescriptorBase<DTO, D, F, FD>(dtoClass, parentDTOClass) where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
    override val identity: CTXIdentity<SwitchListDescriptor<DTO, D, F, FD>> = asIdentity()

    val errorFallback =

        ExceptionFallback {
            val noContainerMsg = "No predefined execution for $this"
            OperationsException(noContainerMsg, ExceptionCode.Sequence_Setup_Failure, this)
        }

}
