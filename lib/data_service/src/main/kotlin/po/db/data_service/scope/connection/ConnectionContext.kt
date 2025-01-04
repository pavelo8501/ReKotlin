package po.db.data_service.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.TableCreateMode

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
    val connectionClass :  ConnectionClass
) {

    fun <DATA, ENTITY>ConnectionContext.service(
        rootDtoModel : DTOClass<DATA,ENTITY>,
        serviceCreateOption : TableCreateMode? = null,
        context: ServiceContext<DATA,ENTITY>.()->Unit,
    ) where DATA : DataModel,   ENTITY : LongEntity {
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