package po.test.exposify.setup

import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import po.exposify.DatabaseManager
import po.exposify.common.classes.dbHooks
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionSettings
import po.misc.context.Identifiable

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class DatabaseTest(){



    companion object {
        @JvmStatic
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
    }

    var connectionClass: ConnectionClass? = null

    init {
        connectionClass =  startTestConnection()
    }


    fun withConnection(block: ConnectionClass.()-> Unit){
        connectionClass?.let {
            block.invoke(it)
        }?:run {
            throw IllegalStateException("connectionClass is null in abstract class DatabaseTest")
        }
    }


    fun startTestConnection(muteContainer: Boolean = true):ConnectionClass{
        System.setProperty("org.testcontainers.reuse.enable", "true")
        if (muteContainer) {
            System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
        }
        val connectionHooks  = dbHooks{
            beforeConnection {
                if(postgres.isRunning){
                    println("Stopping running container ${postgres.containerName} before new launch")
                    DatabaseManager.closeAllConnections()
                    postgres.stop()
                }
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
                println("Opening new connection")
            }
            existentConnection { println("Reusing: ${it.completeName}") }
        }
        val connection = DatabaseManager.openConnection(null, ConnectionSettings(retries = 5), hooks = connectionHooks )
        return connection
    }

    suspend fun startTestConnectionAsync(muteContainer: Boolean = true) {
        System.setProperty("org.testcontainers.reuse.enable", "true")
        if (muteContainer) {
            System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
            DatabaseManager.closeAllConnections()
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
            existentConnection { println("Reusing: ${it.completeName}") }
        }
        val connection = DatabaseManager.openConnectionAsync(null, ConnectionSettings(retries = 5), hooks = connectionHooks )
        connection
    }
}