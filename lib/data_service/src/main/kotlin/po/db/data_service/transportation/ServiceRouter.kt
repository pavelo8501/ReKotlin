package po.db.data_service.transportation

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import po.db.data_service.constructors.ClassBlueprint
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DataModel
import po.db.data_service.services.models.ServiceRegistry
import po.db.data_service.services.models.ServiceUniqueKey
import po.db.data_service.structure.ServiceContext

enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

data class ServiceCreateOptions<DATA_MODEL, ENTITY>(
    val createTable: TableCreateMode = TableCreateMode.CREATE,
) where DATA_MODEL : DataModel, ENTITY : LongEntity
{
    var service: ServiceContext<DATA_MODEL, ENTITY> ? = null
}


class ServiceRouter(
    private val connectionName: String,
    private val connection: Database,
    private val serviceRegistry :ServiceRegistry
) {
    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> createService(
        serviceName:String,
        dtoModel : DTOClass<DATA_MODEL, ENTITY>,
        entityModel : LongEntityClass<ENTITY> ) : ServiceContext<DATA_MODEL, ENTITY>{
        return ServiceContext(serviceName, connection, dtoModel, entityModel )
    }

    private fun <DATA_MODEL, ENTITY> getOrCreateService(
        routeKey: ServiceUniqueKey,
        dtoModel: DTOClass<DATA_MODEL, ENTITY>,
        entityModel : LongEntityClass<ENTITY>,
    ) : ServiceContext<DATA_MODEL, ENTITY >  where DATA_MODEL : DataModel, ENTITY : LongEntity{

        createService<DATA_MODEL,ENTITY>(routeKey.serviceName, dtoModel, entityModel ).let {
           // serviceRegistry.registerService(routeKey, it, dataModelClass, entityModelClass)
            return it
        }
    }

    fun <DATA_MODEL : DataModel, ENTITY: LongEntity >initializeRoute(
        serviceUniqueKey: ServiceUniqueKey,
        service  : ServiceContext<DATA_MODEL, ENTITY>,
        rootDataModelBlueprint: ClassBlueprint<DATA_MODEL>,
    ): ServiceContext<DATA_MODEL, ENTITY> {
        serviceRegistry.registerService(serviceUniqueKey, service, rootDataModelBlueprint).let {
            service.initialize(it)
        }
        service.dataModelClass = rootDataModelBlueprint.clazz

        return service
    }
}


