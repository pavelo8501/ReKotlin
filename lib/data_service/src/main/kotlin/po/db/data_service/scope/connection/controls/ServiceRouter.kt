package po.db.data_service.scope.connection.controls

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.DTOClass
import po.db.data_service.scope.connection.ConnectionContext
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.controls.service_registry.ServiceUniqueKey

enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

data class ServiceCreateOptions<DATA_MODEL, ENTITY>(
    val createTable: TableCreateMode = TableCreateMode.CREATE,
) where DATA_MODEL : DataModel, ENTITY : LongEntity
{
    var service: ServiceContext<DATA_MODEL, ENTITY>? = null
}

class ServiceRouter<DATA_MODEL : DataModel, ENTITY : LongEntity>(
    private val connectionName: String,
    private val dbConnection: Database,
) {


    init {

    }

    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> createService(
        serviceName:String,
        dtoModel : DTOClass,
        connectionContext: ConnectionContext
    ) : ServiceContext<DATA_MODEL, ENTITY> {
        return ServiceContext(serviceName, dtoModel, dbConnection)
    }

    fun <DATA_MODEL : DataModel, ENTITY: LongEntity>initializeRoute(
        serviceUniqueKey: ServiceUniqueKey,
        service  : ServiceContext<DATA_MODEL, ENTITY>,
    ): ServiceContext<DATA_MODEL, ENTITY> {
//        serviceRegistry.registerService(serviceUniqueKey, service).let {meta->
//            service.setServiceMetadata(meta)
//        }
        return service
    }
}




