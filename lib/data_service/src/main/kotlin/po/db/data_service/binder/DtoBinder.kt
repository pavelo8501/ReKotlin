package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOMarker
import kotlin.reflect.KMutableProperty1


class PropertyBinding<DATA_MODEL, ENTITY, TYPE>(
    val name : String,
    val dtoProperty : KMutableProperty1<DATA_MODEL, TYPE>,
    val entityProperty : KMutableProperty1<ENTITY, TYPE>) where  DATA_MODEL : DTOMarker, ENTITY : LongEntity
{

    fun update(dtoModel: DATA_MODEL, entityModel: ENTITY, force: Boolean = false): Boolean {
        val dtoValue = dtoProperty.get(dtoModel)
        val entityValue = entityProperty.get(entityModel)
        if (!force && dtoValue == entityValue) return false
        entityProperty.set(entityModel, dtoValue)
        return true
    }
}



class DataTransferObjectsPropertyBinder <DATA_MODEL, ENTITY, TYPE>(
    vararg  props : PropertyBinding<DATA_MODEL, ENTITY, TYPE >)
        where DATA_MODEL : DTOMarker, ENTITY : LongEntity{

    private val properties = props.toList()

    fun setProperties(vararg  props : PropertyBinding<DATA_MODEL, ENTITY, TYPE >){

    }

    fun updateProperties(dataModel: DATA_MODEL, entityModel: ENTITY, force: Boolean = false) {
        properties.forEach { it.update(dataModel, entityModel, force) }
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