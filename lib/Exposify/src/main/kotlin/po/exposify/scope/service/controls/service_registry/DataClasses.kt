package po.exposify.scope.service.controls.service_registry

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.dto.CommonDTO
import kotlin.reflect.KClass

data class ServiceRegistryItem<DATA_MODEL, ENTITY>(
    val key: ServiceUniqueKey,
    val metadata: ServiceMetadata<DATA_MODEL, ENTITY>
) where DATA_MODEL : DataModel, ENTITY : LongEntity

data class ServiceMetadata<DATA_MODEL, ENTITY>(
    val key: ServiceUniqueKey,
    val service: ServiceData<DATA_MODEL, ENTITY>
) where DATA_MODEL : DataModel, ENTITY : LongEntity

data class ServiceData<DATA_MODEL, ENTITY>(
    val rootDTOModelData: DTOData<DATA_MODEL, ENTITY>,
    val childDTOModelsData: List<ChildDTOData<*,*>>,
) where  DATA_MODEL : DataModel, ENTITY : LongEntity

data class DTOData<DATA, ENTITY>(
    val dtoModelClass: KClass<CommonDTO<DATA, ENTITY>>,
    val dataModelClass: KClass<DATA>,
    val daoEntityModel: LongEntityClass<ENTITY>,
) where  DATA : DataModel, ENTITY : LongEntity {
   // val dtoModelClass: KClass<out CommonDTO<DATA_MODEL, ENTITY>> get() = dtoModel::class
}

data class ChildDTOData<CHILD_DATA, CHILD_ENTITY>(
    val dtoModelClass: KClass<CommonDTO<CHILD_DATA, CHILD_ENTITY>>,
    val dataModelClass: KClass<CHILD_DATA>,
    val daoEntityModel: LongEntityClass<CHILD_ENTITY>,
) where  CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{
   // val dtoModelClass: KClass<out CommonDTO<CHILD_DATA_MODEL, CHILD_ENTITY>> get() = dtoModel::class
}

data class ServiceUniqueKey(val key: String)