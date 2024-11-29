package po.db.data_service.services.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DataModel
import po.db.data_service.exceptions.TypeMismatchException
import po.db.data_service.structure.ServiceContext
import kotlin.reflect.KClass


data class ServiceMetadata<DATA_MODEL : DataModel, ENTITY : LongEntity>(
    val key: ServiceUniqueKey,
    val service: ServiceContext<DATA_MODEL, ENTITY>,
    val dtoClass: KClass<DATA_MODEL>,
   // val entityClass:  KClass<LongEntityClass<ENTITY>>
)

class ServiceRegistry {
    private val serviceRegistry = mutableMapOf<ServiceUniqueKey, ServiceMetadata<*, *>>()

    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> registerService(
        key: ServiceUniqueKey,
        service: ServiceContext<DATA_MODEL, ENTITY>,
        dtoClass: KClass<DATA_MODEL>,
     //   entityClass: KClass<LongEntityClass<ENTITY>>
    ) {
        val metadata = ServiceMetadata(key, service, dtoClass)
     //   val metadata = ServiceMetadata(key, service, dtoClass, entityClass)
        serviceRegistry[key] = metadata
    }

    @Suppress("UNCHECKED_CAST")
    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> getService(
        key: ServiceUniqueKey,
        dtoClass: KClass<DATA_MODEL>,
        entityClass: KClass<ENTITY>
    ): ServiceContext<DATA_MODEL, ENTITY>? {
        val metadata = serviceRegistry[key] ?: return null
       // if (metadata.dtoClass == dtoClass && metadata.entityClass == entityClass) {
        if (metadata.dtoClass == dtoClass) {
            return metadata.service as ServiceContext<DATA_MODEL, ENTITY>
        }
        throw TypeMismatchException(
           // "Service type mismatch. Expected DTO: $dtoClass, ENTITY: $entityClass but found DTO: ${metadata.dtoClass}, ENTITY: ${metadata.entityClass}"
            "Service type mismatch. Expected DTO: $dtoClass, ENTITY: $entityClass but found DTO: ${metadata.dtoClass}"
        )
    }
}