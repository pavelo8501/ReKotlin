package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.models.DaoFactory
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

data class ChildContainer<PARENT : LongEntity, CHILD : LongEntity>(
    val parentDTOModel: DTOClass<PARENT>,
    val childDTOModel: DTOClass<CHILD>,
    val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    val referencedOnProperty : KMutableProperty1<CHILD, PARENT>,
    val type: OrdinanceType,
) : BindingContainer<PARENT, CHILD>() {

    val dtoRepository = listOf<CommonDTO>()

    fun createChild(parent : CommonDTO,  dataModel : DataModel, daoFactory: DaoFactory):CommonDTO{
       return  childDTOModel.create(dataModel).let {dto->
            daoFactory.new<CHILD>(childDTOModel){
                referencedOnProperty.set(it, parent.getEntityDAO())
                dto.updateDAO(it)
            }
            dto
        }
    }

    fun loadChild(entityDao : PARENT):List<CommonDTO> {
        val result = mutableListOf<CommonDTO>()
      //  val entityDao = parent.getEntityDAO<PARENT>()
        val childEntities = byProperty.get(entityDao)
        childEntities.forEach {childEntity ->
            val childDto =  childDTOModel.create(childEntity)
            result.add(childDto)
        }
        return result
    }
}

sealed class BindingContainer<PARENT : LongEntity, CHILD : LongEntity>() {
}

class RelationshipBinder<ENTITY> (
    private val parentDTOModel: DTOClass<ENTITY>
)  where ENTITY : LongEntity {

    private val childBindings = mutableMapOf<String, ChildContainer<ENTITY, *>>()

    fun getBindingList():List<ChildContainer<ENTITY, *>>{
        return childBindings.values.toList()
    }

    fun <CHILD> addChildBinding(
        childDtoModel: DTOClass<CHILD>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD>>,
        referencedOnProperty: KMutableProperty1<CHILD, ENTITY>,
        type: OrdinanceType
    ):ChildContainer<ENTITY, CHILD> where CHILD : LongEntity{
        val container = ChildContainer<ENTITY, CHILD>(parentDTOModel,childDtoModel, byProperty, referencedOnProperty,  type)
        childBindings.putIfAbsent(childDtoModel.className, container)
        return container
    }

    fun getDependantTables():List<IdTable<Long>>{
        val result = mutableListOf<IdTable<Long>>()
        childBindings.values.forEach {container ->
            result.add(container.childDTOModel.daoModel.table)
            container.childDTOModel.getAssociatedTables()
        }
        return result
    }

}
