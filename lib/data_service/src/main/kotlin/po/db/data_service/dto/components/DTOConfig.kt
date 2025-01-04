package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.*
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


class DTOConfig<DATA, ENTITY>(
    parent : DTOClass<DATA,ENTITY>
) where  ENTITY : LongEntity, DATA: DataModel  {

    var entityClass: KClass<ENTITY>? = null
    var dataModelClass: KClass<DATA>? = null

    var entityModel:LongEntityClass<ENTITY>? = null

    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()
    val relationBinder : RelationshipBinder<DATA,ENTITY> = RelationshipBinder(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set


    fun propertyBindings(vararg props: PropertyBinding<DATA, ENTITY, *>) =  propertyBinder.setProperties(props.toList())
    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBindings(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>
    )
    = relationBinder.addChildBinding(childModel,byProperty,referencedOnProperty)


    fun updateProperties(dto: CommonDTO<DATA>, daoEntity : ENTITY){
        propertyBinder.update(dto.injectedDataModel,daoEntity, UpdateMode.ENTITY_TO_MODEL)
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

    
}

