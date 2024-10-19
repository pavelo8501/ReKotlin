package po.playground


import io.github.cdimascio.dotenv.dotenv
import po.db.data_service.models.ConnectionModel
import po.playground.projects.data_service.startDataService


import po.api.rest_service.server.ApiServer
import po.playground.projects.rest_service.startApiServer


fun main() {
     val dotenv = dotenv()
//    val driver = dotenv["MYSQL_DRIVER"]
//    val dbHost = dotenv["MYSQL_HOST"]
//    val dbPort = dotenv["MYSQL_PORT"]
//    val dbName = dotenv["MYSQL_DATABASE"]
//    val dbUsername = dotenv["MYSQL_USER"]
//    val dbPassword = dotenv["MYSQL_PASSWORD"]
//
//    startDataService(ConnectionModel(dbHost, dbName, dbUsername, dbPassword, dbPort))


    val host = dotenv["SERVER_HOST"]
    val port = dotenv["SERVER_PORT"].toInt()

    startApiServer(host, port)

    val a = 10

}