package po.db.data_service.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.*
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceContextV2

class ConnectionContext(
    var connectionName: String,
    val connection: Database,
    val connectionClass :  ConnectionClass
) {

    fun <DTO, ENTITY  >ConnectionContext.service(
        rootDtoModel : DTOClass<ENTITY>,
        context: ServiceContextV2<ENTITY>.()->Unit,
    ) where DTO : CommonDTO,   ENTITY : LongEntity {
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