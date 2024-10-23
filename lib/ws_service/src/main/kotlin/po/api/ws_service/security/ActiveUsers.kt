package api.ws_service.service.security

import io.ktor.websocket.WebSocketSession
import po.api.rest_service.common.SecureUserContext


class ApiUser(val id: Long, override val username: String ):SecureUserContext{


    private var _token : String? = null
    val sessions: MutableList<WebSocketSession> = mutableListOf()
    override val roles: List<String> = listOf("user")
    override fun toPayload(): String {
        return "{\"id\":$id,\"username\":\"$username\"}"
    }

    fun setToken(token: String){
        this._token = token
    }

}


class ActiveUsers() {

    private val users = mutableListOf<ApiUser>()

    fun addUser(user: ApiUser){
        users.add(user)
    }


}