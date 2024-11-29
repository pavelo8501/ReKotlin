package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.binder.PropertyBinding


class ModelDTOConfig<DATA_MODEL, ENTITY> (
    val dtoModel : DTOClass<DATA_MODEL, ENTITY>,
    val entityModel : LongEntityClass<ENTITY>
) where DATA_MODEL : DTOMarker, ENTITY : LongEntity {

    val propertyBinder = DTOPropertyBinder<DATA_MODEL, ENTITY>()

    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) =
        propertyBinder.setProperties(props.toList())

}