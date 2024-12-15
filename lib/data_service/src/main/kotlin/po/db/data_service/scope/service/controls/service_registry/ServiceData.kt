package po.db.data_service.scope.service.controls.service_registry

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KClass

class ServiceDataBuilder<DATA_MODEL : DataModel, ENTITY : LongEntity> {
    var rootDTOModel: KClass<CommonDTO<DATA_MODEL, ENTITY>>? = null
    var entityModel: KClass<ENTITY>? = null
    val subEntityModels = mutableListOf<SubEntityModel<*, *>>()

    inline fun <reified SUB_ENTITY : Any, reified SUB_DTO : Any> subEntityModel(
        init: SubEntityModelBuilder<SUB_ENTITY, SUB_DTO>.() -> Unit
    ) {
        val builder = SubEntityModelBuilder<SUB_ENTITY, SUB_DTO>()
        builder.init()
        subEntityModels.add(builder.build())
    }

    fun build(): ServiceData<DATA_MODEL, ENTITY> {
        requireNotNull(rootDTOModel) { "ServiceData must have a rootDTOModel" }
        requireNotNull(entityModel) { "ServiceData must have an entityModel" }
        return ServiceData(rootDTOModel!!, entityModel!!, subEntityModels)
    }
}