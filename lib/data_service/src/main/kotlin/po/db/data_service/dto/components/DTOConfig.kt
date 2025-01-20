package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KClass


class DTOConfig<DATA, ENTITY>(
   val parent : DTOClass<DATA,ENTITY>
) where  ENTITY : LongEntity, DATA: DataModel{

    var entityClass: KClass<ENTITY>? = null
    var dataModelClass: KClass<DATA>? = null

    var entityModel:LongEntityClass<ENTITY>? = null

    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()
    var relationBinder  = RelationshipBinder<DATA, ENTITY>(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    fun propertyBindings(vararg props: PropertyBinding<DATA, ENTITY, *>) =  propertyBinder.setProperties(props.toList())

   inline fun childBindings(
       block: RelationshipBinder<DATA, ENTITY>.()-> Unit
    ){
        relationBinder.block()
    }


    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

}

