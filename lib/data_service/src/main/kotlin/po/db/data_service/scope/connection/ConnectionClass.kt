package po.db.data_service.scope.connection

import po.db.data_service.controls.ConnectionInfo
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