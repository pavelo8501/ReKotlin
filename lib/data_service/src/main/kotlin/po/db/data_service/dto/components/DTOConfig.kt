package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.ChildContainer
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


class DTOConfig<DATA, ENTITY>(
   val parent : DTOClass<DATA,ENTITY>
) where  ENTITY : LongEntity, DATA: DataModel{

    var entityClass: KClass<ENTITY>? = null
    var dataModelClass: KClass<DATA>? = null

    var entityModel:LongEntityClass<ENTITY>? = null

    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()
    var relationBinder : RelationshipBinder<DATA, ENTITY, *, *>? = null

    var dataModelConstructor : (() -> DataModel)? = null
        private set


    fun propertyBindings(vararg props: PropertyBinding<DATA, ENTITY, *>) =  propertyBinder.setProperties(props.toList())

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBindings(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
        sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>,
        body: (ChildContainer<DATA,ENTITY,CHILD_DATA,CHILD_ENTITY>.()-> Unit)? = null
    ){
       RelationshipBinder<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent).let {
           it.addChildBinding(childModel, byProperty, referencedOnProperty, sourceProperty, body)
           relationBinder = it
       }

    }

    fun updateProperties(dto: EntityDTO<DATA, ENTITY>, daoEntity : ENTITY){
        propertyBinder.update(dto.injectedDataModel,daoEntity, UpdateMode.ENTITY_TO_MODEL)
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

}

