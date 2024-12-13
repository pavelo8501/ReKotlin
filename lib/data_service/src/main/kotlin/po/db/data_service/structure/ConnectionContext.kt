package po.db.data_service.structure

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.CommonDTO
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DataModel
import po.db.data_service.services.models.ServiceRegistry
import po.db.data_service.services.models.ServiceUniqueKey
import po.db.data_service.transportation.ServiceCreateOptions
import po.db.data_service.transportation.ServiceRouter

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
) {

    private val serviceRegistry = ServiceRegistry()

    val serviceRouter = ServiceRouter(connectionName, connection, serviceRegistry)

    /**
     * Service initialization function
     */
    inline fun <reified DATA_MODEL , ENTITY> ConnectionContext.service(
        name: String,
        dtoModel :  DTOClass<DATA_MODEL, ENTITY>,
        daoModel :   LongEntityClass<ENTITY>,
        createOptions: ServiceCreateOptions<DATA_MODEL, ENTITY>? = null,
        service: ServiceContext<DATA_MODEL, ENTITY>.() -> Unit
    ) where DATA_MODEL : DataModel, ENTITY : LongEntity {

        val dataModelClass =  DATA_MODEL::class
       // val dtoModelClass = dtoModel::class
        val blueprint = ConstructorBuilder.getConstructorBlueprint(dataModelClass)

        serviceRouter.createService(name, dtoModel, daoModel).let{ serviceContext->
            serviceRouter.initializeRoute(ServiceUniqueKey(name), serviceContext, blueprint ).let {
                service.invoke(it)
            }
        }
    }

    inline fun <reified  DATA_MODEL : DataModel, ENTITY: LongEntity>ConnectionContext.runTest(
        dtoModel : CommonDTO<DATA_MODEL, ENTITY>
    ) {
        val aaa = dtoModel
    }
}