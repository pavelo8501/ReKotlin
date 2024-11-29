package po.db.data_service.structure

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dao.EntityDAO
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DTOMarker
import po.db.data_service.services.models.ServiceRegistry
import po.db.data_service.services.models.ServiceUniqueKey
import po.db.data_service.transportation.ServiceCreateOptions
import po.db.data_service.transportation.ServiceRouter
import kotlin.reflect.KClass

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
) {

    private val serviceRegistry = ServiceRegistry()
    val serviceRouter = ServiceRouter(connectionName, connection, serviceRegistry)


    inline fun <reified DATA_MODEL, ENTITY> ConnectionContext.initializeService(
        name: String,
        dtoModel :    DTOClass<DATA_MODEL, ENTITY>,
        daoModel :    LongEntityClass<ENTITY>,
        createOptions: ServiceCreateOptions<DATA_MODEL, ENTITY>? = null,
        service: ServiceContext<DATA_MODEL, ENTITY>.() -> Unit
    ) where DATA_MODEL : DTOMarker, ENTITY : LongEntity {

        val dataModelClass =  DATA_MODEL::class
        val testy = dtoModel
        val test2 = daoModel

        serviceRouter.createService(name, dtoModel, daoModel).let{ serviceContext->

            serviceRouter.initializeRoute(ServiceUniqueKey(name, dataModelClass), serviceContext, dataModelClass ).let {
                service.invoke(it)
            }
        }

    }

    inline fun <reified  DATA_MODEL : DTOMarker, ENTITY: LongEntity>ConnectionContext.runTest(
        dtoModel : AbstractDTOModel<DATA_MODEL, ENTITY>
    ) {
        val aaa = dtoModel
    }
}