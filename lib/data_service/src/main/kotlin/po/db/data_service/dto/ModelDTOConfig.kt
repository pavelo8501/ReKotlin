package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.binder.PropertyBinding


class ModelDTOConfig<DATA_MODEL, ENTITY>() where DATA_MODEL : DataModel, ENTITY : LongEntity{

    private val propertyBinder = DTOPropertyBinder<DATA_MODEL, ENTITY>()
    var dataModelConstructor : (() -> DATA_MODEL)? = null
        private set
    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) =
        propertyBinder.setProperties(props.toList())

    fun setProperties(propertyList: List<PropertyBinding<DATA_MODEL, ENTITY, *>>) =
        propertyBinder.setProperties(propertyList)

    fun setDataModelConstructor(dataModelConstructor: () -> DATA_MODEL){
        this.dataModelConstructor = dataModelConstructor
    }
}