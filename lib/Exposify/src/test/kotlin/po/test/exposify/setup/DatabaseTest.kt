package po.test.exposify.setup

import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import po.auth.AuthSessionManager
import po.exposify.DatabaseManager
import po.exposify.common.classes.DBManagerHooks
import po.exposify.common.classes.dbHooks
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.exceptions.InitException
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionSettings
import po.misc.types.getOrThrow

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class DatabaseTest() {

    companion object {
        @JvmStatic
        @Container
        val postgres = PostgreSQLContainer("postgres:15")

        @JvmStatic
        val connection: ConnectionClass? = null
    }

    fun startTestConnection(muteContainer: Boolean = true, context:  (ConnectionClass.() -> Unit)) {
        System.setProperty("org.testcontainers.reuse.enable", "true")

        if (muteContainer) {
            System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
        }
        val connectionHooks  = dbHooks{
            beforeConnection {
                postgres.start()
                println("Trying to connect...")
                ConnectionInfo(
                    jdbcUrl = postgres.jdbcUrl,
                    dbName = postgres.databaseName,
                    user = postgres.username,
                    pwd = postgres.password,
                    driver = postgres.driverClassName
                )
            }
            newConnection {

            }
            existentConnection { println("Reusing: ${it.name}") }
        }

        val connection = DatabaseManager.openConnection(null, ConnectionSettings(retries = 5), hooks = connectionHooks )
        connection.context()
    }

    suspend fun startTestConnectionAsync(muteContainer: Boolean = true, context:  (ConnectionClass.() -> Unit)) {
        System.setProperty("org.testcontainers.reuse.enable", "true")
        if (muteContainer) {
            System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
        }
        val connectionHooks  = dbHooks {
            beforeConnection {
                postgres.start()
                ConnectionInfo(
                    jdbcUrl = postgres.jdbcUrl,
                    dbName = postgres.databaseName,
                    user = postgres.username,
                    pwd = postgres.password,
                    driver = postgres.driverClassName)
            }
            newConnection {
                postgres.start()
            }
            existentConnection { println("Reusing: ${it.name}") }
        }
        val connection = DatabaseManager.openConnectionAsync(null, ConnectionSettings(retries = 5), hooks = connectionHooks )
        connection.context()
    }
}