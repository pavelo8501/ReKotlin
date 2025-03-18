package po.playground


import kotlinx.coroutines.runBlocking
import po.exposify.controls.ConnectionInfo
import po.playground.projects.data_service.startDataService


fun main() {

    runBlocking {
        startDataService(ConnectionInfo(
            "127.0.0.1",
            "medprof_postgres",
            "django-api",
            "django-api_usrPWD12",
            "5432",
            "org.postgresql.Driver",))
    }

}

