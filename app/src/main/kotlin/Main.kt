package po.playground

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import po.playground.projects.data_service.dto.PartnerDataModel
import po.restwraptor.RestServer
import po.restwraptor.models.request.ApiRequest
import java.io.File
import java.nio.file.Paths


fun main() {
    val dotenv = dotenv()
    val dbHost = dotenv["MYSQL_HOST"]
    val dbPort = dotenv["MYSQL_PORT"]
    val dbName = dotenv["MYSQL_DATABASE"]
    val dbUsername = dotenv["MYSQL_USER"]
    val dbPassword = dotenv["MYSQL_PASSWORD"]

  //  startDataService(ConnectionInfo(dbHost, dbName, dbUsername, dbPassword, dbPort))
   // wsServer.configureWSHost("127.0.0.1", 8080)

    val keysPath = Paths.get("").toAbsolutePath().toString()+ File.separator + "keys" + File.separator .toString()

    val restServer = RestServer{
        setupApplication{
            routing {
                post("/api/partners") {
                    val partner =  call.receive<ApiRequest<PartnerDataModel>>()
                    println(partner)
                }
            }
        }
    }
    restServer.start(host =  "127.0.0.1", port =  8080, wait = true)
}

