package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import kotlin.reflect.KProperty1


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

data class ChildContainer<PARENT : LongEntity, CHILD : LongEntity>(
    val dtoModelClass: DTOClass<PARENT>,
    val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    val type: OrdinanceType,
)

sealed class BindingContainer {
    data class TypedBinding<PARENT : LongEntity, CHILD : LongEntity>(
        val container: ChildContainer<PARENT, CHILD>
    ) : BindingContainer()
}

class RelationshipBinder<ENTITY> (
    val parentDTOModel: DTOClass<ENTITY>
)  where ENTITY : LongEntity {
    var bindingKeys = mutableListOf<String>()
        private set

    val childBindings = mutableMapOf<String, BindingContainer>()
    fun loadChildren(entity: ENTITY, childDaoModelClassName: String) {

//        childBindings.forEach { binding ->
//            @Suppress("UNCHECKED_CAST")
//            val container = (binding as BindingContainer.TypedBinding<ENTITY, CHILD_ENTITY>).container
//            val childEntities = (container.byProperty).get(entity)
//        }

        @Suppress("UNCHECKED_CAST")
        val binding = childBindings[childDaoModelClassName] as BindingContainer.TypedBinding<ENTITY, *>
        val container = binding.container
      //  val parentEntity = container.parentClass.cast(entity)

        val childEntities = (container.byProperty).get(entity)

        childEntities.forEach { childEntity ->
            println("Child entity ID: ${childEntity.id}")
        }
    }

    fun <CHILD> addChildBinding(
        childDtoModel: DTOClass<CHILD>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD>>,
        type: OrdinanceType,
    ) where CHILD : LongEntity  {
        val container = ChildContainer(parentDTOModel, byProperty, type)
        val typedBinding = BindingContainer.TypedBinding(container)
        childBindings.putIfAbsent(childDtoModel.className, typedBinding)
        bindingKeys.add(childDtoModel.className)
    }
}
