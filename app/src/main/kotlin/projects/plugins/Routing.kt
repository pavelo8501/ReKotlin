package po.playground.projects.routes

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import po.api.ws_service.service.routing.apiWebSocket
import po.playground.projects.data_service.dto.PartnerDataModel


fun Application.routes(){

    routing {
        apiWebSocket("/ws/partners") {
            apiWebSocketMethod<PartnerDataModel>("partners"){
                receiveApiRequest={
                    println("Request received in user module ${it.module}")
                 //   val partnerResp = PartnerDataModel("SomeName2", 123433)
                  //  val response = it.toResponse(partnerResp)
                   // sendApiResponse(response)
                }
            }
        }
    }

}

