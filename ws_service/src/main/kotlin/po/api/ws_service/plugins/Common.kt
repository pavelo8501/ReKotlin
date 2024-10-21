package po.api.ws_service.plugins

import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import po.api.ws_service.models.ServiceResponse

suspend fun WebSocketServerSession.sendSystemMessage(message: String = "Connection established successfully", isError: Boolean = false) {
    val response = if (isError) {
        ServiceResponse().setErrorMessage(message, 500)
    } else {
        ServiceResponse(message)
    }
    val json = response.toJson()
    send(Frame.Text(json))
}

suspend fun WebSocketServerSession.sendCloseReason(reason: String) {
    sendSystemMessage(reason, isError = true)
    close(CloseReason(CloseReason.Codes.NORMAL, reason))
}