package po.db.data_service.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.*
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.TableCreateMode

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
    val connectionClass :  ConnectionClass
) {

    fun <DTO, ENTITY  >ConnectionContext.service(
        rootDtoModel : DTOClass<ENTITY>,
        serviceCreateOption : TableCreateMode? = null,
        context: ServiceContext<ENTITY>.()->Unit,
    ) where DTO : CommonDTO,   ENTITY : LongEntity {
        try {
            ServiceClass(connection, rootDtoModel, serviceCreateOption).let {
                connectionClass.addService(it)
                it.launch(context)
            }
        }catch (exception: Exception){
            println(exception.message)
            throw exception
        }
    }
}