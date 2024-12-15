package po.db.data_service.scope.service.controls.service_registry

import kotlin.reflect.KClass

data class ServiceRegistryItem<DATA_MODEL : Any, ENTITY : Any>(
    val key: ServiceUniqueKey,
    val metadata: ServiceMetadata<DATA_MODEL, ENTITY>
)

data class ServiceMetadata<DATA_MODEL : Any, ENTITY : Any>(
    val key: ServiceUniqueKey,
    val service: ServiceData<DATA_MODEL, ENTITY>
)

data class ServiceData<DATA_MODEL : Any, ENTITY : Any>(
    val rootDTOModel: KClass<DATA_MODEL>,
    val entityModel: KClass<ENTITY>,
    val subEntityModels: List<SubEntityModel<*, *>>
)

data class SubEntityModel<SUB_ENTITY : Any, SUB_DTO : Any>(
    val entityModel: KClass<SUB_ENTITY>,
    val dtoModel: KClass<SUB_DTO>
)

data class ServiceUniqueKey(val key: String)