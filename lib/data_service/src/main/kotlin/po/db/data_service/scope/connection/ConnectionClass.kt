package po.db.data_service.scope.connection

import org.jetbrains.exposed.sql.Database
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.dto.DTOClassV2
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.scope.service.ServiceClass

class ConnectionClass(val connectionInfo: ConnectionInfo) {

    var services = mutableMapOf<String,ServiceClass>()

    init {
        connectionInfo.connection
    }

    fun addService(service : ServiceClass){
        services.putIfAbsent(service.name, service)
    }

}