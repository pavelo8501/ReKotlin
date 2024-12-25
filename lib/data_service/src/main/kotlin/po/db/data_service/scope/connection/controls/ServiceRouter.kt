package po.db.data_service.scope.connection.controls

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.DTOClass
import po.db.data_service.scope.connection.ConnectionContext
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.service.ServiceContextV2
import po.db.data_service.scope.service.controls.service_registry.ServiceUniqueKey

enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

data class ServiceCreateOptions<DATA_MODEL, ENTITY>(
    val createTable: TableCreateMode = TableCreateMode.CREATE,
) where DATA_MODEL : DataModel, ENTITY : LongEntity
{
    var service: ServiceContextV2<ENTITY>? = null
}

class ServiceRouter<DATA_MODEL : DataModel, ENTITY : LongEntity>(
    private val connectionName: String,
    private val dbConnection: Database,
) {


    init {

    }

    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> createService(
        serviceName:String,
        dtoModel : DTOClass<ENTITY>,
        connectionContext: ConnectionContext
    ) : ServiceContextV2<ENTITY> {
        return ServiceContextV2<ENTITY>(dbConnection, dtoModel)
    }

    fun <DATA_MODEL : DataModel, ENTITY: LongEntity>initializeRoute(
        serviceUniqueKey: ServiceUniqueKey,
        service  : ServiceContextV2<ENTITY>,
    ): ServiceContextV2<ENTITY>{
//        serviceRegistry.registerService(serviceUniqueKey, service).let {meta->
//            service.setServiceMetadata(meta)
//        }
        return service
    }
}




