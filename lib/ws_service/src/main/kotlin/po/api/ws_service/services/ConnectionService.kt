package po.api.ws_service.services

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.subscribe
import po.api.ws_service.service.models.ApiRequestDataType
import po.api.ws_service.service.models.WSApiRequest
import po.api.ws_service.service.routing.ApiWebSocketClass
import po.api.ws_service.service.routing.ApiWebSocketMethodClass
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference


data class Connection(
    val session : DefaultWebSocketServerSession,
    val path: String,
    val protocol: String?,
    val wsApiSocketClass : ApiWebSocketClass,
){
    var resourceName: String = ""

    var requestSubject : MutableSharedFlow<WSApiRequest<ApiRequestDataType>>? = null

    init {
        val regex = ".*/([^/]+)$".toRegex()
        val matchResult = regex.find(path)
        this.resourceName = matchResult?.groupValues?.get(1)?.lowercase()?:""
    }

    fun getWSMethod(method: String): ApiWebSocketMethodClass?{
        method.lowercase()
       val found =  wsApiSocketClass.apiMethods.firstOrNull { it.method == method  }

        return  found
    }
}

object ConnectionService {
    val connections = Collections.synchronizedList<Connection>(ArrayList())

    var session: WebSocketServerSession? = null

    fun setCurrentActiveSession(session : DefaultWebSocketServerSession) {
        this.session = session
    }

   fun addConnection(connection:  Connection): Connection{
        val existingConnection = connections.firstOrNull { it.session == connection.session }
        if(existingConnection != null){
            connection.wsApiSocketClass.apiMethods.forEach {
                existingConnection.wsApiSocketClass.apiMethods.add(it)
            }
        }
        connections.add(connection)
        this.setCurrentActiveSession(connection.session)
        return connection
    }
    fun addWebSocketMethod(method: ApiWebSocketMethodClass, session: WebSocketServerSession){
        val connection = connections.firstOrNull { it.session == session }
        if(connection == null){
            throw Exception("Connection not found for session")
        }
        connection.wsApiSocketClass.addWebSocketMethod(method)

    }

    fun getApiConnection(method: String): Connection?{
       return connections.firstOrNull{
            it.resourceName == method
        }
    }

    fun getWSMethod(resourceName : String, method: String): ApiWebSocketMethodClass?{
        resourceName.lowercase()
        method.lowercase()
        val wsMethod = connections.firstOrNull { it.resourceName == resourceName }?.getWSMethod(method)
        return wsMethod
    }

    fun getWSMethod(method: String): ApiWebSocketMethodClass?{
        method.lowercase()
        val connection =   getCurrentConnection()
        return  connection?.getWSMethod(method)
    }

    fun forwardApiRequest(path: String, method: String, request: WSApiRequest<ApiRequestDataType>){
        val method = getWSMethod(path, method)
        if(method == null){
            throw Exception("Recepient host not found on path: $path with the method name: $method ")
        }
        method.forwardResponse(request)
    }

    fun updateSessionList(updatedList: List<WebSocketServerSession>){
//        updatedList.forEach {
//            sessions.add(it)
//        }
    }

    fun bindToApiRequestFlow(connection : Connection):MutableSharedFlow<WSApiRequest<ApiRequestDataType>>{
        connection.requestSubject = apiRequestSubject
        return connection.requestSubject!!
    }

    fun getCurrentConnection(): Connection?{
        return connections.firstOrNull { it.session == session }
    }

    val apiRequestSubject  = MutableSharedFlow<WSApiRequest<ApiRequestDataType>>()
    suspend fun sendRequest(request: WSApiRequest<ApiRequestDataType>){
        apiRequestSubject.emit(request)
    }

}