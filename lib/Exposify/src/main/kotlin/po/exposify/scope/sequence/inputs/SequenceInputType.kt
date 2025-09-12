package po.exposify.scope.sequence.inputs

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.auth.sessions.models.SessionBase
import po.exposify.dto.DTOBase
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.ListDescriptor
import po.exposify.scope.sequence.builder.SequenceDescriptor
import po.exposify.scope.sequence.builder.SingleDescriptor
import po.exposify.scope.sequence.builder.SwitchDescriptorBase
import po.misc.functions.containers.DeferredContainer
import po.misc.types.TypeData
import po.misc.types.castListOrManaged
import po.misc.types.castOrManaged


enum class InputType{
    Single,
    List
}


sealed interface CommonInputType<T: Any> {
    val inputType:InputType
}



sealed interface SingleInputType<DTO: ModelDTO, D: DataModel,  T: Any>: CommonInputType<T> {
    val dtoClass: DTOBase<DTO, D, *>
    val descriptor: SequenceDescriptor<DTO, *>
    override val inputType: InputType get() =  InputType.Single
}

sealed interface ListInputType<DTO: ModelDTO, D: DataModel,  T: Any>: CommonInputType<T> {
    val dtoClass: DTOBase<DTO, D, *>
    val descriptor: SequenceDescriptor<DTO, *>
    override val inputType: InputType get() =  InputType.List
}

sealed class InputBase<DTO: ModelDTO, D: DataModel, T: Any>(
    override val descriptor: SequenceDescriptor<DTO, D>,
):CommonInputType<T>, ListInputType<DTO, D, T> {
    abstract val value:T
}

class ParameterInput<DTO: ModelDTO, D: DataModel>(
    override val value: Long,
    descriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, D, Long>(descriptor), SingleInputType<DTO, D,  Long>{

    override val dtoClass: DTOBase<DTO, D, *> = descriptor.dtoClass
    override val inputType: InputType = InputType.Single
}


fun <DTO: ModelDTO, D: DataModel> SessionBase.withInput(descriptor:SequenceDescriptor<DTO, D>, input: Long):ParameterInput<DTO, D>{
   return ParameterInput(input, descriptor)
}


class DataInput<DTO: ModelDTO, D: DataModel>(
    override val value: D,
    descriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, D, D>(descriptor), SingleInputType<DTO, D, D>{

    override val dtoClass: DTOBase<DTO, D, *> = descriptor.dtoClass
    override val inputType: InputType = InputType.Single

    fun <D: DataModel> getValue(typeData: TypeData<D>):D{
        return value.castOrManaged(typeData.kClass, this)
    }
}

fun <DTO: ModelDTO, D: DataModel> SessionBase.withInput(descriptor:SequenceDescriptor<DTO, D>, input: D):DataInput<DTO, D>{
    return DataInput(input, descriptor)
}

class ListDataInput<DTO: ModelDTO, D: DataModel>(
    override val value: List<D>,
    descriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, D, List<D> >(descriptor){

    override val inputType: InputType = InputType.List
    override val dtoClass: DTOBase<DTO, D, *> = descriptor.dtoClass

    fun <D: DataModel> getValue(typeData: TypeData<D>): List<D> {
        return value.castListOrManaged(typeData.kClass, this)
    }
}

fun <DTO: ModelDTO, D: DataModel> SessionBase.withInput(
    descriptor:SequenceDescriptor<DTO, D>,
    input: List<D>
):ListDataInput<DTO, D>{
    return ListDataInput(input, descriptor)
}

class QueryInput<DTO: ModelDTO, D: DataModel>(
    override val value: DeferredContainer<WhereQuery<*>>,
    descriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, D,  DeferredContainer<WhereQuery<*>>>(descriptor){

    override val dtoClass: DTOBase<DTO, D, *>  = descriptor.dtoClass
    override var inputType: InputType = InputType.Single

    fun <E: LongEntity> getValue(typeData: TypeData<E>): DeferredContainer<WhereQuery<E>> {
        return  value.castOrManaged(this)
    }
}

fun <DTO: ModelDTO, D: DataModel> SessionBase.withInput(
    input: DeferredContainer<WhereQuery<*>>,
    descriptor: SingleDescriptor<DTO, D>
):QueryInput<DTO, D> {
    return QueryInput(input, descriptor)
}

fun <DTO: ModelDTO, D: DataModel> SessionBase.withInput(
    input: DeferredContainer<WhereQuery<*>>,
    descriptor: ListDescriptor<DTO, D>
):QueryInput<DTO, D> {
    return QueryInput(input, descriptor).apply { inputType = InputType.List }
}

