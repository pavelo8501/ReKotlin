package po.db.data_service.scope.connection

import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.*
import po.db.data_service.models.CommonDTOV2
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceContextV2

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
    val connectionClass :  ConnectionClass
) {

    fun <DTO : CommonDTOV2 >ConnectionContext.service(
        rootDtoModel : DTOClass,
        context: ServiceContextV2.()->Unit,
    ){
        try {
            ServiceClass(connection, rootDtoModel).let {
                connectionClass.addService(it)
                it.launch(context)
            }
        }catch (exception: Exception){
            println(exception.message)
            throw exception
        }
    }
}