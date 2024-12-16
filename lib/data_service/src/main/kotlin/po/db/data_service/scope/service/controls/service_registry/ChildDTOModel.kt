package po.db.data_service.scope.service.controls.service_registry

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KClass

class ChildDTODataBuilder<CHILD_DATA_MODEL, CHILD_ENTITY> where CHILD_DATA_MODEL : DataModel, CHILD_ENTITY : LongEntity   {
    private var dtoModel: KClass<CommonDTO<CHILD_DATA_MODEL, CHILD_ENTITY>> ? = null
    private var dataModelClass: KClass<CHILD_DATA_MODEL>? = null
    private var entityModel: LongEntityClass<CHILD_ENTITY>? = null

    fun setDTOModel(dtoModel: KClass<CommonDTO<CHILD_DATA_MODEL, CHILD_ENTITY>>) = apply {
        this.dtoModel = dtoModel
    }

    fun setDataModel(dataModel: KClass<CHILD_DATA_MODEL>) = apply {
        this.dataModelClass = dataModel
    }

    fun setEntityModel(entityModel: LongEntityClass<CHILD_ENTITY>) = apply {
        this.entityModel = entityModel
    }

    fun build(): ChildDTOData<CHILD_DATA_MODEL, CHILD_ENTITY> {
        requireNotNull(dtoModel) { "ChildDTOModel must have a dtoModel" }
        requireNotNull(entityModel) { "ChildDTOModel must have an entityModel" }
        requireNotNull(dataModelClass) { "ChildDTOModel must have an dataModelClass" }
        return ChildDTOData(dtoModel!!, entityModel!!, dataModelClass!!)
    }
}