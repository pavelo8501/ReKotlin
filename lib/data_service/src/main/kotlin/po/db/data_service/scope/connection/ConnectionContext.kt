package po.db.data_service.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.*
import po.db.data_service.scope.service.controls.ServiceRegistry
import po.db.data_service.scope.service.models.ServiceUniqueKey
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.connection.controls.ServiceCreateOptions
import po.db.data_service.scope.connection.controls.ServiceRouter
import po.db.data_service.scope.service.ServiceContext

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
) {

    private val serviceRegistry = ServiceRegistry()
    val serviceRouter = ServiceRouter(connectionName, connection, serviceRegistry)

    /**
     * Service initialization function
     */
    inline fun  <DATA_MODEL,ENTITY> ConnectionContext.service(
        name: String,
        rootDtoModel :  DTOClass<DATA_MODEL, ENTITY>,
        createOptions: ServiceCreateOptions<DATA_MODEL, ENTITY>? = null,
        service: ServiceContext<DATA_MODEL, ENTITY>.() -> Unit
    ) where DATA_MODEL : DataModel, ENTITY : LongEntity {

        serviceRouter.createService(name, rootDtoModel, this).let{ serviceContext->
            serviceRouter.initializeRoute(ServiceUniqueKey(name), serviceContext).let {
                service.invoke(it)
            }
        }
    }
}