package po.test.exposify.setup

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.platform.commons.logging.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import po.auth.AuthSessionManager
import po.exposify.DatabaseManager
import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.ConnectionContext
import po.lognotify.extensions.newTask
import po.lognotify.extensions.newTaskAsync

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class DatabaseTest {

    companion object {
        @JvmStatic
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
    }

    fun startTestConnection(muteContainer: Boolean = true):ConnectionContext? {
        System.setProperty("org.testcontainers.reuse.enable", "true")

        if(muteContainer){
            System.setProperty("org.slf4j.simpleLogger.log.org.testcontainers", "ERROR")
        }
        postgres.start()
        return  DatabaseManager.openConnectionSync(
               ConnectionInfo(
                   jdbcUrl = postgres.jdbcUrl,
                   dbName = postgres.databaseName,
                   user = postgres.username,
                   pwd = postgres.password,
                   driver = postgres.driverClassName
               ),
               sessionManager = AuthSessionManager
           )
        }
    }