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
    val dtoModelClass: DTOClass,
    val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    val type: OrdinanceType,
    val parentClass: Class<PARENT>
)

sealed class BindingContainer {
    data class TypedBinding<PARENT : LongEntity, CHILD : LongEntity>(
        val container: ChildContainer<PARENT, CHILD>
    ) : BindingContainer()
}

class RelationshipBinder(
    val parentDTOModel: DTOClass
) {
    var bindingKeys = mutableListOf<String>()
        private set

    val childBindings = mutableMapOf<String, BindingContainer>()

    fun loadChildren(entity: LongEntity, key: String) {
        val binding = childBindings[key] as? BindingContainer.TypedBinding<*, *> ?: return

        val container = binding.container
        if (!container.parentClass.isInstance(entity)) {
            throw IllegalArgumentException("Entity type mismatch for key: $key. Expected: ${container.parentClass}, Found: ${entity::class.java}")
        }

        val parentEntity = container.parentClass.cast(entity)

        @Suppress("UNCHECKED_CAST")
        val childEntities = (container.byProperty as KProperty1<LongEntity, SizedIterable<LongEntity>>).get(parentEntity)

        childEntities.forEach { childEntity ->
            println("Child entity ID: ${childEntity.id}")
        }
    }

    inline fun <reified PARENT, reified CHILD> addChildBinding(
        parentDto: DTOClass,
        childDtoModel: DTOClass,
        byProperty: KProperty1<out LongEntity, SizedIterable<CHILD>>,
        type: OrdinanceType,
    ) where PARENT : LongEntity,  CHILD : LongEntity  {

       // val container = ChildContainer(parentDto, byProperty, type, parentDTOModel.daoEntity(1)::class.java)
      //  val typedBinding = BindingContainer.TypedBinding(container)

      //  childBindings.putIfAbsent(childDtoModel.className, typedBinding)
        bindingKeys.add(childDtoModel.className)
    }
}


//class RelationshipBinder  {
//    var bindingKeys = mutableListOf<String>()
//        private set
//    var childBindings = mutableMapOf<String, ChildContainer>()
//        private set
//
//    inline fun <reified P : LongEntity> loadChildren(
//        entity: P,
//        daoModel: LongEntityClass<P>,
//        key: String? = null
//    ) {
//        // If a specific key is provided, load only that binding
//        val keysToProcess = key?.let { listOf(it) } ?: bindingKeys
//        keysToProcess.forEach { bindingKey ->
//            val container = childBindings[bindingKey] ?: return@forEach
//            val byProperty = container.byProperty
//
//            if (container.dtoModelClass.daoModel == daoModel) {
//                // Now we can safely call byProperty.get(entity)
//
//                val childEntities = byProperty.get()
//                childEntities.forEach { childEntity ->
//                    println("Child entity ID: ${childEntity.id}")
//                }
//            } else {
//                println("Entity type or DAO model mismatch for key: $bindingKey")
//            }
//
//        }
//
//    }
//
//    fun loadChildren(entity: LongEntity, key:String?=null){
//
//        // If a specific key is provided, load only that binding
//        val keysToProcess = key?.let { listOf(it) } ?: bindingKeys
//
//        keysToProcess.forEach { bindingKey ->
//            val container = childBindings[bindingKey] ?: return@forEach
//            val byProperty = container.byProperty
//
//            val childEntities = byProperty.get(entity)
//            childEntities.forEach { childEntity ->
//                println("Child entity ID: ${childEntity.id}")
//            }
//        }
//
//        if(key == null){
//            bindingKeys.forEach {
////                childBindings[it]?.byProperty.
////                childBindings[it]?.byProperty?.forEach { child ->
////                    println(child.id)
////                }
//            }
//        }
//    }
//
//    fun addChildBinding(dtoClass: DTOClassV2, byProperty : KProperty1<out LongEntity, SizedIterable<LongEntity>>, type: OrdinanceType) {
//        ChildContainer(dtoClass, byProperty, type).let {
//            this.childBindings.putIfAbsent(dtoClass.className, it)
//            bindingKeys.add(dtoClass.className)
//        }
//    }
//}
