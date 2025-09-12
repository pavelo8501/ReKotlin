package po.exposify.scope.sequence.inputs

import po.auth.sessions.models.AuthorizedSession
import po.auth.sessions.models.SessionBase
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.SequenceDescriptor
import po.exposify.scope.sequence.builder.SwitchDescriptorBase



sealed interface SwitchSingleInput<DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel, T: Any>: CommonInputType<T> {
    val dtoClass: DTOBase<DTO, D, *>
    val descriptor: SwitchDescriptorBase<DTO, D, F, FD>
    override val inputType: InputType get() =  InputType.Single
}

sealed interface SwitchListInput<DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel, T: Any>: CommonInputType<T> {
    val dtoClass: DTOBase<DTO, D, *>
    val descriptor: SwitchDescriptorBase<DTO, D, F, FD>
    override val inputType: InputType get() =  InputType.List
}

sealed class SwitchInputBase<DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel, T: Any>(
    override val descriptor: SwitchDescriptorBase<DTO, D, F, FD>,
    val session: SessionBase
):CommonInputType<T>, SwitchListInput<DTO, D, F, FD, T> {
    abstract val value:T
}


class SelectAllInput<DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel>(

    descriptor: SwitchDescriptorBase<DTO, D, F, FD>,
    session: SessionBase
):SwitchInputBase<DTO, D,  F, FD, Unit>(descriptor, session), SwitchListInput<DTO, D, F, FD, Unit>{

    override val value: Unit = Unit
    override val dtoClass: DTOBase<DTO, D, *> = descriptor.dtoClass
    override var inputType: InputType = InputType.List
}


fun <DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel> SessionBase.withSwitchInput(
    descriptor: SwitchDescriptorBase<DTO, D, F, FD>
):SelectAllInput<DTO, D, F, FD> {
    return SelectAllInput(descriptor, this).apply { inputType = InputType.List }
}