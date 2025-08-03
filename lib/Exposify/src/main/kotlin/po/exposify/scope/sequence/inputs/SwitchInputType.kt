package po.exposify.scope.sequence.inputs

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.SequenceDescriptor
import po.misc.functions.containers.DeferredContainer
import po.misc.types.TypeData
import po.misc.types.castListOrManaged
import po.misc.types.castOrManaged


sealed interface CommonInputType<T: Any> {

}

sealed class InputBase<DTO: ModelDTO, T: Any>(
    val descriptor: SequenceDescriptor<DTO, *>,
): CommonInputType<T>{
    abstract val value:T
}


class ParameterInput<DTO: ModelDTO>(
    override val value: Long,
    ownDescriptor: SequenceDescriptor<DTO, *>,
):InputBase<DTO, Long>(ownDescriptor)


class DataInput<DTO: ModelDTO, D: DataModel>(
    override val value: D,
    ownDescriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, D>(ownDescriptor) {

    fun <D: DataModel> getValue(typeData: TypeData<D>):D{
        return value.castOrManaged(typeData.kClass, this)
    }
}

class ListDataInput<DTO: ModelDTO, D: DataModel>(
    override val value: List<D>,
    ownDescriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO, List<D> >(ownDescriptor) {

    fun <D: DataModel> getValue(typeData: TypeData<D>): List<D> {
        return value.castListOrManaged(typeData.kClass, this)
    }
}

class QueryInput<DTO: ModelDTO, D: DataModel>(
    override val value: DeferredContainer<WhereQuery<*>>,
    ownDescriptor: SequenceDescriptor<DTO, D>,
):InputBase<DTO,  DeferredContainer<WhereQuery<*>>>(ownDescriptor) {

    fun <E: LongEntity> getValue(typeData: TypeData<E>): DeferredContainer<WhereQuery<E>> {
        return  value.castOrManaged(this)
    }
}
