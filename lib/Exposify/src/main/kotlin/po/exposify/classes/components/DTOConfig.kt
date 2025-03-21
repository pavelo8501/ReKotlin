package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.binder.PropertyBinder
import po.exposify.binder.PropertyBindingOption
import po.exposify.binder.RelationshipBinder
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
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

    fun propertyBindings(vararg props: PropertyBindingOption<DATA, ENTITY, *> ): Unit =  propertyBinder.setProperties(props.toList())

   inline fun childBindings(
       block: RelationshipBinder<DATA, ENTITY>.()-> Unit
    ){
        relationBinder.block()
    }


    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

}

