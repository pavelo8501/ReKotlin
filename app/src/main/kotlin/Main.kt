package po.playground


import kotlinx.coroutines.runBlocking
import po.exposify.scope.connection.models.ConnectionInfo
import po.playground.projects.data_service.startDataService


fun main() {

    runBlocking {
        startDataService(ConnectionInfo(
            "127.0.0.1",
            "playground_postgres",
            "postgres",
            "localpassword",
            "5432",
            "org.postgresql.Driver",))
    }

}

