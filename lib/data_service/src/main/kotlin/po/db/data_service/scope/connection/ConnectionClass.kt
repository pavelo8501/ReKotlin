package po.db.data_service.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.scope.service.ServiceClass

class ConnectionClass(val connectionInfo: ConnectionInfo) {
    var services = mutableMapOf<String,ServiceClass<*,*>>()

    init {
        connectionInfo.connection
    }

    fun addService(service : ServiceClass<*,*>){
        services.putIfAbsent(service.name, service)
    }

    fun getService(name  : String): ServiceClass<*,*>?{
        this.services.keys.firstOrNull{it == name}?.let {
            return services[it]
        }
        return null
    }


}