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

data class ChildContainer<PARENT : LongEntity, CHILD : LongEntity, DATA : DataModel>(
    val parentDTOModel: DTOClass<DATA,PARENT>,
    val childDTOModel: DTOClass<DATA,CHILD>,
    val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    val referencedOnProperty: KMutableProperty1<CHILD, PARENT>,
    val type: OrdinanceType,
) : BindingContainer<PARENT, CHILD>() {

    val dtoRepository = listOf<CommonDTO<DATA>>()

    fun createChild(parent : CommonDTO<DATA>,  dataModel : DATA, daoFactory: DaoFactory):CommonDTO<DATA>{
       return  childDTOModel.create(dataModel).let {dto->
            daoFactory.new<CHILD,DATA>(childDTOModel){
                referencedOnProperty.set(it, parent.entityDAO as PARENT)
               // dto.updateDTO(it)
            }
            dto
        }
    }

    fun loadChild(entityDao : PARENT):List<CommonDTO<DATA>> {
        val result = mutableListOf<CommonDTO<DATA>>()
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

class RelationshipBinder<PARENT, DATA> (
    val parentDTOModel: DTOClass<DATA,PARENT>
)  where PARENT : LongEntity, DATA: DataModel {

    private val childBindings = mutableMapOf<OrdinanceType, ChildContainer<PARENT, *, DATA>>()

    fun getBindingList():List<ChildContainer<PARENT, *, DATA>>{
        return childBindings.values.toList()
    }

    fun <CHILD> addChildBinding(
        childDtoModel: DTOClass<DATA,CHILD>,
        byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
        referencedOnProperty: KMutableProperty1<CHILD, PARENT>,
        type: OrdinanceType,
        body:  DTOClass<DATA,CHILD>.()-> Unit
    ):ChildContainer<PARENT, CHILD, DATA> where CHILD : LongEntity{
        val container = ChildContainer<PARENT, CHILD, DATA>(parentDTOModel,childDtoModel, byProperty, referencedOnProperty,  type)
        childBindings.putIfAbsent(type, container)
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
