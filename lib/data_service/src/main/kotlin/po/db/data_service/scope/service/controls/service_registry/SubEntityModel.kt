package po.db.data_service.scope.service.controls.service_registry

import kotlin.reflect.KClass

class SubEntityModelBuilder<SUB_ENTITY : Any, SUB_DTO : Any> {
    private var entityModel: KClass<SUB_ENTITY>? = null
    private var dtoModel: KClass<SUB_DTO>? = null

    fun setEntityModel(entityModel: KClass<SUB_ENTITY>) = apply {
        this.entityModel = entityModel
    }

    fun setDTOModel(dtoModel: KClass<SUB_DTO>) = apply {
        this.dtoModel = dtoModel
    }

    fun build(): SubEntityModel<SUB_ENTITY, SUB_DTO> {
        requireNotNull(entityModel) { "SubEntityModel must have an entityModel" }
        requireNotNull(dtoModel) { "SubEntityModel must have a dtoModel" }
        return SubEntityModel(entityModel!!, dtoModel!!)
    }
}