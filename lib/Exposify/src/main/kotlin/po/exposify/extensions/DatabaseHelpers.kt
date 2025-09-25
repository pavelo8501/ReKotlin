package po.exposify.extensions

import po.exposify.DatabaseManager
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.service.ServiceContext
import po.misc.context.Identifiable
import po.misc.types.safeCast


fun <R> withDefaultConnection(block: ConnectionClass.()-> R):R {
    val connectionClass = DatabaseManager.connections.firstOrNull()
    return if (connectionClass != null) {
        connectionClass.block()
    } else {
        throw InitException(DatabaseManager, "No active connections registered", ExceptionCode.NOT_INITIALIZED)
    }
}


fun <DTO: ModelDTO, D: DataModel, R> DTOBase<DTO, D, *>.withServiceContext(block: ServiceContext<DTO, D, *>.()->R):R{
    val context = serviceClass.serviceContext.safeCast<ServiceContext<DTO, D, *>>()
    if(context != null){
       return context.block()
    }else{
        throw InitException(DatabaseManager, "No active context found for type $identifiedByName", ExceptionCode.NOT_INITIALIZED)
    }
}

inline fun <reified D, DTO: ModelDTO> D.respondOnUpdate(
    connectionClass: ConnectionClass,
    noinline callback: (DTO)-> Unit
) where D: DataModel, D: Identifiable<D>{
    val manager = DatabaseManager
    manager.notifyOnUpdated(this.identity, connectionClass, callback)
}

