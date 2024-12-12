package po.db.data_service.services.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.constructors.ConstructorBlueprint
import po.db.data_service.dto.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.structure.ServiceContext


data class ServiceMetadata<DATA_MODEL : DataModel, ENTITY : LongEntity>(
    val key: ServiceUniqueKey,
    val service: ServiceContext<DATA_MODEL, ENTITY>,
    private val entityBlueprint : ConstructorBlueprint<DATA_MODEL>,
   // val dtoClass: KClass<DATA_MODEL>,
   // val entityClass:  KClass<LongEntityClass<ENTITY>>
){
    private val dtoEntityBlueprints = mutableMapOf<String, ConstructorBlueprint<DATA_MODEL>>()

    init {
        addBlueprint(entityBlueprint)
    }

    fun addBlueprint(entityBlueprint: ConstructorBlueprint<DATA_MODEL>){
        dtoEntityBlueprints.putIfAbsent(entityBlueprint.className, entityBlueprint)
    }



}

class ServiceRegistry {

    private val serviceRegistry = mutableMapOf<ServiceUniqueKey, ServiceMetadata<*, *>>()

    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> registerService(
        key: ServiceUniqueKey,
        service: ServiceContext<DATA_MODEL, ENTITY>,
        dtoEntityBlueprint : ConstructorBlueprint<DATA_MODEL>,
       // dtoClass: KClass<DATA_MODEL>,
    ): ServiceMetadata<DATA_MODEL, ENTITY> {
        val metadata = ServiceMetadata(key, service, dtoEntityBlueprint)
        serviceRegistry.putIfAbsent(key,metadata).let {
            if (it != null) {
                throw InitializationException("Service with the given unique key ${key.serviceName} already exists", ExceptionCodes.ALREADY_EXISTS )
            }
        }
        return metadata
    }

    fun <DATA_MODEL : DataModel,  ENTITY : LongEntity>addDTOBlueprint(key: ServiceUniqueKey,  blueprint : ConstructorBlueprint<DATA_MODEL>){
        getServiceMeta<DATA_MODEL, ENTITY>(key)?.addBlueprint(blueprint) ?:
            throw InitializationException("Unable to add blueprint to the service name ${key.serviceName}. No service registered with this key",
                ExceptionCodes.KEY_NOT_FOUND
            )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <DATA_MODEL : DataModel, ENTITY : LongEntity> getServiceMeta(key: ServiceUniqueKey):ServiceMetadata<DATA_MODEL, ENTITY>?{
        val metadata = serviceRegistry[key] ?: return null
        return metadata as ServiceMetadata<DATA_MODEL,ENTITY>
    }

    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> getService(
        key: ServiceUniqueKey,
//        dtoClass: KClass<DATA_MODEL>,
//        entityClass: KClass<ENTITY>
    ): ServiceContext<DATA_MODEL, ENTITY>? {
        // val metadata = serviceRegistry[key] ?: return null
        // if (metadata.dtoClass == dtoClass && metadata.entityClass == entityClass) {
        return getServiceMeta<DATA_MODEL, ENTITY>(key)?.service

//        throw TypeMismatchException(
//           // "Service type mismatch. Expected DTO: $dtoClass, ENTITY: $entityClass but found DTO: ${metadata.dtoClass}, ENTITY: ${metadata.entityClass}"
//            "Service type mismatch. Expected DTO: $dtoClass, ENTITY: $entityClass but found DTO: ${metadata.dtoClass}"
//        )
    }
}