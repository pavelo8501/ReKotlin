package po.playground

import io.github.cdimascio.dotenv.dotenv
import po.api.ws_service.WebSocketServer
import po.playground.projects.routes.routes


fun main() {
    val dotenv = dotenv()
    val dbHost = dotenv["MYSQL_HOST"]
    val dbPort = dotenv["MYSQL_PORT"]
    val dbName = dotenv["MYSQL_DATABASE"]
    val dbUsername = dotenv["MYSQL_USER"]
    val dbPassword = dotenv["MYSQL_PASSWORD"]

  //  startDataService(ConnectionInfo(dbHost, dbName, dbUsername, dbPassword, dbPort))

    val wsServer = WebSocketServer{
        routes()
    }
   // wsServer.configureWSHost("127.0.0.1", 8080)

    wsServer.start(host =  "127.0.0.1", port =  8080)

}

