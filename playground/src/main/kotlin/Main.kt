package po.playground


import io.github.cdimascio.dotenv.dotenv
import po.db.data_service.models.ConnectionModel
import po.playground.projects.data_service.startDataService


fun main() {
    val dotenv = dotenv()
    val driver = dotenv["MYSQL_DRIVER"]
    val host = dotenv["MYSQL_HOST"]
    val port = dotenv["MYSQL_PORT"]
    val dbName = dotenv["MYSQL_DATABASE"]
    val username = dotenv["MYSQL_USER"]
    val password = dotenv["MYSQL_PASSWORD"]

    startDataService(ConnectionModel(host, dbName, username, password, port))

}