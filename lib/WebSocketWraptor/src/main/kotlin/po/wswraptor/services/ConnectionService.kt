package po.wswraptor.services

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import kotlinx.coroutines.flow.MutableSharedFlow
import po.wswraptor.models.request.WSRequest
import po.wswraptor.routing.ApiWebSocketClass
import po.wswraptor.routing.ApiWebSocketMethodClass
import java.util.Collections


data class Connection(
    val session : DefaultWebSocketServerSession,
    val path: String,
    val wsApiSocketClass : ApiWebSocketClass,
){
    var resourceName: String = ""

    var requestSubject : MutableSharedFlow<WSRequest<Any>>? = null

    init {
        val regex = ".*/([^/]+)$".toRegex()
        val matchResult = regex.find(path)
        this.resourceName = matchResult?.groupValues?.get(1)?.lowercase()?:""
    }

    fun getWSMethod(method: String): ApiWebSocketMethodClass?{
        method.lowercase()
        val found =  wsApiSocketClass.getApiMethod(method)
        return  found
    }
}

class ConnectionService {
    val connections = Collections.synchronizedList<Connection>(ArrayList())

    var session: WebSocketServerSession? = null

    fun setCurrentActiveSession(session : DefaultWebSocketServerSession) {
        this.session = session
    }

    fun addConnection(connection:  Connection): Connection{
        val existingConnection = connections.firstOrNull { it.session == connection.session }
        if(existingConnection != null){
            connection.wsApiSocketClass.getApiMethods().forEach {
                existingConnection.wsApiSocketClass.addWebSocketMethod(it)
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

    fun forwardApiRequest(path: String, method: String, request: WSRequest<Any>){
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

    fun bindToApiRequestFlow(connection : Connection):MutableSharedFlow<WSRequest<Any>>{
        connection.requestSubject = apiRequestSubject
        return connection.requestSubject!!
    }

    fun getCurrentConnection(): Connection?{
        return connections.firstOrNull { it.session == session }
    }

    val apiRequestSubject  = MutableSharedFlow<WSRequest<Any>>()
    suspend fun sendRequest(request: WSRequest<Any>){
        apiRequestSubject.emit(request)
    }

}