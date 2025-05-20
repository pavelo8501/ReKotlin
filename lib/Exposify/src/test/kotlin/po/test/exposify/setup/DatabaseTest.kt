package po.test.exposify.setup

import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import po.auth.AuthSessionManager
import po.exposify.DatabaseManager
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.exceptions.InitException
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionSettings
import po.misc.types.getOrThrow

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class DatabaseTest(val connectionInfo: ConnectionInfo?= null) {

    companion object {
        @JvmStatic
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
    }

    fun startTestConnection(muteContainer: Boolean = true, context:  (ConnectionClass.() -> Unit)) {
        System.setProperty("org.testcontainers.reuse.enable", "true")

       val connection = if(connectionInfo == null){
            if (muteContainer) {
                System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
            }
            postgres.start()
            DatabaseManager.openConnection(
                ConnectionInfo(
                    jdbcUrl = postgres.jdbcUrl,
                    dbName = postgres.databaseName,
                    user = postgres.username,
                    pwd = postgres.password,
                    driver = postgres.driverClassName
                ),
                ConnectionSettings()
            )
        }else{
            DatabaseManager.openConnection(connectionInfo, ConnectionSettings(5))
        }
        connection.context()
    }


    fun startTestConnectionSync(muteContainer: Boolean = true, context: (ConnectionClass.() -> Unit)) {
        System.setProperty("org.testcontainers.reuse.enable", "true")

        if (muteContainer) {
            System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
        }
        postgres.start()
       val connection =  DatabaseManager.openConnection(
            ConnectionInfo(
                jdbcUrl = postgres.jdbcUrl,
                dbName = postgres.databaseName,
                user = postgres.username,
                pwd = postgres.password,
                driver = postgres.driverClassName
            ),
            ConnectionSettings()
        )
        connection.context()
    }
}