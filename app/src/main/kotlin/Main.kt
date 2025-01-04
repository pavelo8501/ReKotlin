package po.playground

import io.github.cdimascio.dotenv.dotenv
import po.db.data_service.controls.ConnectionInfo
import po.playground.projects.data_service.startDataService

fun main() {
    val dotenv = dotenv()
    val dbHost = dotenv["MYSQL_HOST"]
    val dbPort = dotenv["MYSQL_PORT"]
    val dbName = dotenv["MYSQL_DATABASE"]
    val dbUsername = dotenv["MYSQL_USER"]
    val dbPassword = dotenv["MYSQL_PASSWORD"]

    startDataService(ConnectionInfo(dbHost, dbName, dbUsername, dbPassword, dbPort))


}