package po.exposify.scope.sequence.inputs

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.ListDescriptor
import po.exposify.scope.sequence.builder.SequenceDescriptor
import po.exposify.scope.sequence.builder.SingleDescriptor
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

sealed class InputBase<DTO: ModelDTO, T: Any>(
    val descriptor: SequenceDescriptor<DTO, *>,
):CommonInputType<T>{
    abstract val value:T
}


class ParameterInput<DTO: ModelDTO>(
    override val value: Long,
    ownDescriptor: SequenceDescriptor<DTO, *>,
):InputBase<DTO, Long>(ownDescriptor){
    override val inputType: InputType = InputType.Single
}


fun <DTO: ModelDTO> AuthorizedSession.withInput(input: Long,  descriptor:SequenceDescriptor<DTO, *>):ParameterInput<DTO>{
   return ParameterInput(input, descriptor)
}


class DataInput<DTO: ModelDTO, D: DataModel>(
    override val value: D,
    ownDescriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, D>(ownDescriptor){

    override val inputType: InputType = InputType.Single

    fun <D: DataModel> getValue(typeData: TypeData<D>):D{
        return value.castOrManaged(typeData.kClass, this)
    }
}

fun <DTO: ModelDTO, D: DataModel> AuthorizedSession.withInput(input: D,  descriptor:SequenceDescriptor<DTO, D>):DataInput<DTO, D>{
    return DataInput(input, descriptor)
}

class ListDataInput<DTO: ModelDTO, D: DataModel>(
    override val value: List<D>,
    ownDescriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, List<D> >(ownDescriptor){

    override val inputType: InputType = InputType.List

    fun <D: DataModel> getValue(typeData: TypeData<D>): List<D> {
        return value.castListOrManaged(typeData.kClass, this)
    }
}

fun <DTO: ModelDTO, D: DataModel> AuthorizedSession.withInput(input:  List<D>,  descriptor:SequenceDescriptor<DTO, D>):ListDataInput<DTO, D>{
    return ListDataInput(input, descriptor)
}

class QueryInput<DTO: ModelDTO, D: DataModel>(
    override val value: DeferredContainer<WhereQuery<*>>,
    ownDescriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO,  DeferredContainer<WhereQuery<*>>>(ownDescriptor){

    override var inputType: InputType = InputType.Single

    fun <E: LongEntity> getValue(typeData: TypeData<E>): DeferredContainer<WhereQuery<E>> {
        return  value.castOrManaged(this)
    }
}

fun <DTO: ModelDTO, D: DataModel> AuthorizedSession.withInput(
    input: DeferredContainer<WhereQuery<*>>,
    descriptor: SingleDescriptor<DTO, D>
):QueryInput<DTO, D> {
    return QueryInput(input, descriptor)
}

fun <DTO: ModelDTO, D: DataModel> AuthorizedSession.withInput(
    input: DeferredContainer<WhereQuery<*>>,
    descriptor: ListDescriptor<DTO, D>
):QueryInput<DTO, D> {
    return QueryInput(input, descriptor).apply { inputType = InputType.List }
}
