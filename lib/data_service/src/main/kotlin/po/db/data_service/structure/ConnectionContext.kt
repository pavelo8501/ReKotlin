package po.db.data_service.structure

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dao.EntityDAO
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.DTOMarker
import po.db.data_service.services.models.ServiceUniqueKey
import po.db.data_service.transportation.ServiceCreateOptions
import po.db.data_service.transportation.ServiceRouter

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
) {

    val serviceRouter = ServiceRouter(connectionName, connection)

    inline fun <reified DATA_MODEL, ENTITY> ConnectionContext.initializeService(
        name: String,
        dtoModel: AbstractDTOModel<DATA_MODEL>,
        entityModel: EntityDAO<ENTITY>,
        createOptions: ServiceCreateOptions<DATA_MODEL, ENTITY>? = null,
        service: ServiceContext<DATA_MODEL, ENTITY>.() -> Unit
    ) where DATA_MODEL : DTOMarker, ENTITY : LongEntity {

        val dtoModelClass = dtoModel::class
        serviceRouter.initializeRoute<DATA_MODEL, ENTITY>( ServiceUniqueKey(name, dtoModelClass), dtoModel, entityModel).let {
            createOptions?.service = it
         //   dtoCompanion.initialSetup(typeInfo<T>(),createOptions)
            service.invoke(it)
        }
    }

    inline fun <reified  DATA_MODEL : DTOMarker>ConnectionContext.runTest(
        dataModelObject : AbstractDTOModel<DATA_MODEL>
    ) {

    }

}