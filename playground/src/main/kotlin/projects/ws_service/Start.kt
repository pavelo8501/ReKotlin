package po.playground.projects.ws_service

import po.api.ws_service.WebSocketServer

fun startWebSocketServer(host: String, port: Int) {

    val wsServer =  WebSocketServer(){

    }
    wsServer.configureWSHost(host, 8080)
    wsServer.start(true)

}