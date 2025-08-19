package po.test.exposify.setup

import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import po.exposify.DatabaseManager
import po.exposify.common.classes.dbHooks
import po.exposify.extensions.getOrInit
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionSettings
import po.lognotify.TasksManaged
import po.misc.context.Identifiable
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class DatabaseTest(): TasksManaged{


    companion object {
        @JvmStatic
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
    }

    val connectionClass: ConnectionClass =  startTestConnection()

    fun persistUser(user: User): UserDTO{
        var userDTO: UserDTO? = null
        withConnection {
            service(UserDTO){
                userDTO = insert(user).dto
            }
        }
        return userDTO.getOrInit(this)
    }

    fun withConnection(block: ConnectionClass.()-> Unit){
        block.invoke(connectionClass)
    }


    fun startTestConnection(muteContainer: Boolean = true):ConnectionClass {
        val existentConnection = DatabaseManager.connections.firstOrNull()
        if (existentConnection == null) {
            System.setProperty("org.testcontainers.reuse.enable", "true")
            if (muteContainer) {
                System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
            }
            val connectionHooks = dbHooks {
                beforeConnection {
                    if (postgres.isRunning) {
                        println("Stopping running container ${postgres.containerName} before new launch")
                        DatabaseManager.closeAllConnections()
                        postgres.stop()
                    }
                    postgres.start()
                    ConnectionInfo(
                        jdbcUrl = postgres.jdbcUrl,
                        dbName = postgres.databaseName,
                        user = postgres.username,
                        pwd = postgres.password,
                        driver = postgres.driverClassName
                    )
                }
                existentConnection { println("Reusing: ${it.completeName}") }
            }
            return DatabaseManager.openConnection(null, ConnectionSettings(retries = 5), hooks = connectionHooks)
        } else {
            return existentConnection
        }
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