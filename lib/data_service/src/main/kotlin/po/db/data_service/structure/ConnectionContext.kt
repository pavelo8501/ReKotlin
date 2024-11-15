package po.db.data_service.structure

import io.ktor.util.reflect.typeInfo
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.ModelDTOContext
import po.db.data_service.transportation.ServiceCreateOptions
import po.db.data_service.transportation.ServiceRouter

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
) {

    val serviceRouter = ServiceRouter(connectionName, connection)

    inline fun <reified T: ModelDTOContext, E: LongEntity> ConnectionContext.initializeService(
        name: String,
        dtoCompanion: DTOClass<T,E>,
        createOptions: ServiceCreateOptions<T,E>? = null,
        service: ServiceContext<T,E>.() -> Unit
    ){
        serviceRouter.initializeRoute<T,E>(name, dtoCompanion).let {
            createOptions?.service = it
            dtoCompanion.initialSetup(typeInfo<T>(),createOptions)
            service.invoke(it)
        }
    }
}