package po.db.data_service.scope.service.controls

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.constructors.ClassBlueprint
import po.db.data_service.models.CommonDTO
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.service.ServiceContext
import kotlin.reflect.KClass


data class ServiceMetadataDepr<DATA_MODEL, ENTITY>(
   // val key: ServiceUniqueKey,
    val service: ServiceContext<DATA_MODEL, ENTITY>,
) where DATA_MODEL : DataModel, ENTITY : LongEntity {

    private val modelBlueprints = mutableMapOf<KClass<DATA_MODEL>, ClassBlueprint<DATA_MODEL>>()
    private val dtoBlueprints = mutableMapOf<KClass<out CommonDTO<DATA_MODEL, ENTITY>>, ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>>()

    fun addDtoBlueprint(classDefinition : KClass<CommonDTO<DATA_MODEL, ENTITY>>, entityBlueprint: ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>) {
        dtoBlueprints.putIfAbsent(classDefinition, entityBlueprint)
    }

    fun addModelBlueprint(classDefinition : KClass<DATA_MODEL>, modelBlueprint: ClassBlueprint<DATA_MODEL>) {
        modelBlueprints.putIfAbsent(classDefinition, modelBlueprint)
    }

    fun getDTOBlueprint(classDefinition: KClass<CommonDTO<DATA_MODEL, ENTITY>>): ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>? {
        this.dtoBlueprints[classDefinition]?.let {
            return it
        }
        return null
    }
    fun getModelBlueprint(classDefinition: KClass<DATA_MODEL>): ClassBlueprint<DATA_MODEL>? {
        this.modelBlueprints[classDefinition]?.let {
            return it
        }
        return null
    }
}

class ServiceRegistryDepr {

   // private val serviceRegistry = mutableMapOf<ServiceUniqueKey, ServiceMetadata<*, *>>()

//    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> registerService(
//        key: ServiceUniqueKey,
//        service: ServiceContext<DATA_MODEL, ENTITY>,
//    ): ServiceMetadata<DATA_MODEL, ENTITY> {
//        val metadata = ServiceMetadata(key, service)
//        serviceRegistry.putIfAbsent(key,metadata).let {
//            if (it == null) {
//                return metadata
//            }
//        }
//        throw InitializationException("Service with the given unique key ${key.serviceName} already exists", ExceptionCodes.ALREADY_EXISTS )
//    }

//    @Suppress("UNCHECKED_CAST")
//    private fun <DATA_MODEL : DataModel, ENTITY : LongEntity> getServiceMeta(key: ServiceUniqueKey): ServiceMetadata<DATA_MODEL, ENTITY>?{
//        val metadata = serviceRegistry[key] ?: return null
//        return metadata as ServiceMetadata<DATA_MODEL, ENTITY>
//    }

//    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> getService(
//        key: ServiceUniqueKey,
//    ): ServiceContext<DATA_MODEL, ENTITY>? {
//        // val metadata = serviceRegistry[key] ?: return null
//        // if (metadata.dtoClass == dtoClass && metadata.entityClass == entityClass) {
//        return getServiceMeta<DATA_MODEL, ENTITY>(key)?.service
//
////        throw TypeMismatchException(
////           // "Service type mismatch. Expected DTO: $dtoClass, ENTITY: $entityClass but found DTO: ${metadata.dtoClass}, ENTITY: ${metadata.entityClass}"
////            "Service type mismatch. Expected DTO: $dtoClass, ENTITY: $entityClass but found DTO: ${metadata.dtoClass}"
////        )
//    }
}