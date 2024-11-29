package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOEntityMarker
import po.db.data_service.dto.DataModel
import kotlin.reflect.KMutableProperty1


class PropertyBinding<DATA_MODEL, ENTITY, TYPE>(
    val name : String,
    private val dtoProperty : KMutableProperty1<DATA_MODEL, TYPE>,
    private val entityProperty : KMutableProperty1<ENTITY, TYPE>) where  DATA_MODEL : DataModel, ENTITY : LongEntity
{

    fun update(dtoModel: DATA_MODEL, entityModel: ENTITY, force: Boolean = false): Boolean {
        val dtoValue = dtoProperty.get(dtoModel)
        val entityValue = entityProperty.get(entityModel)
        if (!force && dtoValue == entityValue) return false
        entityProperty.set(entityModel, dtoValue)
        return true
    }
}

class DTOPropertyBinder <DATA_MODEL, ENTITY>(
    vararg  props : PropertyBinding<DATA_MODEL, ENTITY, *> = emptyArray() )
        where DATA_MODEL : DataModel, ENTITY : LongEntity {

    private var propertyList = props.toList()

    fun setProperties(properties: List<PropertyBinding<DATA_MODEL, ENTITY, *>>){
        propertyList = properties
    }

    fun properties(vararg  props : PropertyBinding<DATA_MODEL, ENTITY, * >){
        setProperties(props.toList())
    }


    fun updateProperties(dataModel: DATA_MODEL, entityModel: ENTITY, force: Boolean = false) {
        propertyList.forEach { it.update(dataModel, entityModel, force) }
    }
}


//class DTOBinder <DATA, ENTITY, TYPE>( vararg  props : PropertyBinding<DATA, ENTITY, TYPE >)
//        where DATA : ModelDTOContext, ENTITY : LongEntity{
//    private val properties = props.toList()
//    fun updateProperties(dataModel: DATA, entityModel: ENTITY, force: Boolean = false) {
//        properties.forEach { it.update(dataModel, entityModel, force) }
//    }
//}


//class DTOBinderClass<T : ModelDTOContext, E : LongEntity>(vararg  props : BindPropertyClass<T, E, *>):  CommonBinder<T, E>{
//    override var modelDTO   : T? = null
//    override var entityDAO  : E? = null
//    override var properties : List<BindPropertyClass<T, E, *>> = emptyList()
//    init {
//        this.properties = props.toList()
//        properties.forEach{
//            it.binder = this
//        }
//    }
//    fun setModelObject(model : T){
//        this.modelDTO = model
//    }
//    override fun updateProperties(entity: E, force: Boolean): E {
//        entityDAO = entity
//        properties.forEach {
//            it.update(force)
//        }
//        return entity
//    }
//}