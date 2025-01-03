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

data class ChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    val parentDTOModel: DTOClass<DATA,ENTITY>,
    val childDTOModel: DTOClass<CHILD_DATA,CHILD_ENTITY>,
    val byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
    val referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    val type: OrdinanceType,
) : BindingContainer<DATA, ENTITY, CHILD_DATA,CHILD_ENTITY>(parentDTOModel,childDTOModel)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{

    val dtoRepository = listOf<CommonDTO<DATA>>()

    fun createChild(parent : CommonDTO<DATA>,  dataModel : DATA, daoFactory: DaoFactory):CommonDTO<DATA>?{
//       return  childDTOModel.create(dataModel).let {dto->
//            daoFactory.new<CHILD,DATA>(childDTOModel){
//                referencedOnProperty.set(it, parent.entityDAO as PARENT)
//               // dto.updateDTO(it)
//            }
//            dto
//        }
        return null
    }

//    fun loadChild(entityDao : PARENT):List<CommonDTO<DATA>> {
//        val result = mutableListOf<CommonDTO<DATA>>()
//      //  val entityDao = parent.getEntityDAO<PARENT>()
//        val childEntities = byProperty.get(entityDao)
//        childEntities.forEach {childEntity ->
//            val childDto =  childDTOModel.create(childEntity)
//            result.add(childDto)
//        }
//        return result
//    }
}

sealed class BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parentDTOModel: DTOClass<DATA,ENTITY>,
    childDTOModel: DTOClass<CHILD_DATA,CHILD_ENTITY>,
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity {

}

class RelationshipBinder<DATA, ENTITY> (
    val thisDTOModel: DTOClass<DATA, ENTITY>
)  where ENTITY : LongEntity, DATA: DataModel {


    private val childBindings = mutableMapOf<OrdinanceType, ChildContainer<DATA, ENTITY,*,*>>()

    fun getBindingList():List<ChildContainer<DATA,ENTITY,* ,*>>{
        return childBindings.values.toList()
    }

    fun <CHILD_DATA, CHILD_ENTITY> addChildBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    ) where CHILD_ENTITY : LongEntity, CHILD_DATA : DataModel{
        if(!childModel.initialized){
            childModel.initialization()
        }
        val type =  OrdinanceType.ONE_TO_MANY
        childBindings.putIfAbsent(
            type,
            ChildContainer<DATA, ENTITY,CHILD_DATA, CHILD_ENTITY>(thisDTOModel, childModel, byProperty, referencedOnProperty,  type)
        )
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
