package po.db.data_service.transportation

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DTOMarker
import po.db.data_service.services.models.ServiceRegistry
import po.db.data_service.services.models.ServiceUniqueKey
import po.db.data_service.structure.ServiceContext
import kotlin.reflect.KClass


enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

data class ServiceCreateOptions<DATA_MODEL, ENTITY>(
    val createTable: TableCreateMode = TableCreateMode.CREATE,
) where DATA_MODEL : DTOMarker, ENTITY : LongEntity
{
    var service: ServiceContext<DATA_MODEL, ENTITY> ? = null
}


class ServiceRouter(
    private val connectionName: String,
    private val connection: Database,
    private val serviceRegistry :ServiceRegistry
)
//where DATA_MODEL : DTOMarker
{

    //private val services :  MutableMap<String, ServiceContext<*,*>> = mutableMapOf()

    fun <DATA_MODEL : DTOMarker, ENTITY : LongEntity> createService(
        serviceName:String,
        dtoModel : DTOClass<DATA_MODEL, ENTITY>,
        entityModel : LongEntityClass<ENTITY> ) : ServiceContext<DATA_MODEL, ENTITY>{

        return ServiceContext(serviceName, connection, dtoModel, entityModel )
    }

    private fun <DATA_MODEL, ENTITY> getOrCreateService(
        routeKey: ServiceUniqueKey,
        dtoModel: DTOClass<DATA_MODEL, ENTITY>,
        entityModel : LongEntityClass<ENTITY>,
    ) : ServiceContext<DATA_MODEL, ENTITY >  where DATA_MODEL : DTOMarker, ENTITY : LongEntity{

        createService<DATA_MODEL,ENTITY>(routeKey.serviceName, dtoModel, entityModel ).let {
           // serviceRegistry.registerService(routeKey, it, dataModelClass, entityModelClass)
            return it
        }
    }

    fun <DATA_MODEL : DTOMarker, ENTITY: LongEntity >initializeRoute(
        serviceUniqueKey: ServiceUniqueKey,
        service  : ServiceContext<DATA_MODEL, ENTITY>,
        dataModelClass: KClass<DATA_MODEL>,
     //   entityModelClass: KClass<LongEntityClass <ENTITY>>
    ): ServiceContext<DATA_MODEL, ENTITY> {

        serviceRegistry.registerService(serviceUniqueKey, service, dataModelClass)
        return service
    }


//    fun <DATA_MODEL : DTOMarker, ENTITY: LongEntity >initializeRoute(
//        serviceUniqueKey: ServiceUniqueKey,
//        dtoModel : AbstractDTOModel<DATA_MODEL>,
//        entityModel : ENTITY
//    ): ServiceContext<DATA_MODEL, ENTITY> {
//
//        getOrCreateService(serviceUniqueKey, dtoModel,  entityModel).let {
//            return it
//        }
//    }
}


