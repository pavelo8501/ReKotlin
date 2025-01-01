package po.db.data_service.scope.connection.controls

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.DTOClass
import po.db.data_service.scope.connection.ConnectionContext
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.TableCreateMode
import po.db.data_service.scope.service.controls.service_registry.ServiceUniqueKey



data class ServiceCreateOptions<DATA, ENTITY>(
    val createTable: TableCreateMode = TableCreateMode.CREATE,
) where DATA : DataModel, ENTITY : LongEntity
{
    var service: ServiceContext<DATA,ENTITY>? = null
}

class ServiceRouter<DATA, EMTITY>(
    private val connectionName: String,
    private val dbConnection: Database,
) where  DATA: DataModel,  EMTITY : LongEntity {

    init {

    }
    fun <DATA_MODEL : DataModel, ENTITY : LongEntity> createService(
        serviceName:String,
        dtoModel : DTOClass<DATA,ENTITY>,
        connectionContext: ConnectionContext
    ) : ServiceContext<DATA,ENTITY> {
        return ServiceContext<DATA,ENTITY>(dbConnection, dtoModel)
    }

    fun <ENTITY: LongEntity>initializeRoute(
        serviceUniqueKey: ServiceUniqueKey,
        service  : ServiceContext<DATA,ENTITY>,
    ): ServiceContext<DATA,ENTITY>{
//        serviceRegistry.registerService(serviceUniqueKey, service).let {meta->
//            service.setServiceMetadata(meta)
//        }
        return service
    }
}




