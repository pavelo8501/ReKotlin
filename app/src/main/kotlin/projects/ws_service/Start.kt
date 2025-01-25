package po.playground.projects.ws_service



import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


import java.io.File
import kotlin.io.readText


//@Serializable
//data class TestPartner(
//    val name: String?  = null,
//    val vat: Int? = null
//):WSApiRequestDataInterface
//
//@Serializable
//data class TestDepartment(
//    val name: String,
//    val street: String,
//    val hq: Boolean
//):WSApiRequestDataInterface
//
//@Serializable
//data class TestContact(
//    val name: String,
//    val surname: String,
//    val age: Int,
//):WSApiRequestDataInterface
//
//@Serializable
//data class WsUser(
//    override val username: String,
//    override val roles : List<String> = listOf<String>("user")
//) :  SecureUserContext {
//    override fun toPayload(): String {
//       return Json.encodeToString<WsUser>(this)
//    }
//}


fun startWebSocketServer(host: String, port: Int) {

//    val currentDir = File("").absolutePath
//    RestServer.apiConfig.setAuthKeys(
//        publicKey  =  File(currentDir+File.separator+"keys"+File.separator+"ktor.spki").readText(),
//        privateKey =  File(currentDir+File.separator+"keys"+File.separator+"ktor.pk8").readText()
//    )
//
//    val wsServer = WebSocketServer(){
//
//            routing {
//
//            apiWebSocket("/ws/partners") {
//
//                apiWebSocketMethod<TestPartner>("partners"){
//                    receiveApiRequest={
//                        println("Request received in user module ${it.module}")
//                        val partnerResp = TestPartner("SomeName2", 123433)
//                        val response = it.toResponse(partnerResp)
//                        sendApiResponse(response)
//                    }
//                }
//            }
//        }
//    }
//    wsServer.configureWSHost(host, 8080)
//    wsServer.onLoginRequest = {
//        it.username
//        it.password
//
//        WsUser("someUsername", listOf("user"))
//    }
//
//    wsServer.start(host = "127.0.0.1", port =  8800)
}

