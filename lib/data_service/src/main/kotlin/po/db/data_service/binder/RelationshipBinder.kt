package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KProperty1


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

data class ChildUpdate(
    val bindingKey:String,
    val ordinance : OrdinanceType,
    val update: (dao:LongEntity)->Unit
)

data class ChildContainer<PARENT : LongEntity, CHILD : LongEntity>(
    val dtoModelClass: DTOClass<PARENT>,
    val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    val type: OrdinanceType,
) : BindingContainer<PARENT, CHILD>() {
    private  val childDataModelRepository  = mutableListOf<DataModel>()
    fun setDataModelRepository(childDataModelList : MutableList<DataModel>){
        childDataModelRepository.addAll(childDataModelList)
    }
}

sealed class BindingContainer<PARENT : LongEntity, CHILD : LongEntity>() {

}

class RelationshipBinder<ENTITY> (
    private val parentDTOModel: DTOClass<ENTITY>
)  where ENTITY : LongEntity {

    private val childBindings = mutableMapOf<String, ChildContainer<ENTITY, *>>()
    fun loadChildren(entity: ENTITY, childDaoModelClassName: String) {

//        childBindings.forEach { binding ->
//            @Suppress("UNCHECKED_CAST")
//            val container = (binding as BindingContainer.TypedBinding<ENTITY, CHILD_ENTITY>).container
//            val childEntities = (container.byProperty).get(entity)
//        }

        val binding = childBindings[childDaoModelClassName] as ChildContainer
        val childEntities = (binding.byProperty).get(entity)
        childEntities.forEach { childEntity ->
            println("Child entity ID: ${childEntity.id}")
        }
    }

    fun getChildUpdateList():List<ChildUpdate>{
        childBindings.keys.forEach {
           // ChildUpdate(it, childBindings[it].type, () )
        }

        val result = emptyList<ChildUpdate>()
        return result
    }

    fun <CHILD> addChildBinding(
        childDtoModel: DTOClass<CHILD>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD>>,
        type: OrdinanceType,
        childDataModelList : MutableList<DataModel>? = null
    ) where CHILD : LongEntity  {
        val container = ChildContainer(parentDTOModel, byProperty, type)
        childDataModelList?.let {
            container.setDataModelRepository(it)
        }
        childBindings.putIfAbsent(childDtoModel.className, container)
    }
}
