package po.exposify.test


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import po.auth.AuthSessionManager
import po.exposify.DatabaseManager
import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.ConnectionContext


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class DatabaseTest {

    companion object {
        @JvmStatic
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
    }
    var connectionContext : ConnectionContext? = null

    private fun initExposify(): ConnectionContext{
        val connection = DatabaseManager.openConnectionSync(
            ConnectionInfo(
                jdbcUrl= postgres.jdbcUrl,
                dbName= postgres.databaseName,
                user = postgres.username,
                pwd = postgres.password,
                driver= postgres.driverClassName),
            sessionManager = AuthSessionManager)
        connection?.let {
            return it
        }?: throw Exception("DatabaseTest initExposify failed")
    }

    @BeforeAll
    fun setupExposify(){
        connectionContext = initExposify()
    }


    @BeforeEach
    fun beforeEach() {

    }

    @AfterEach
    fun cleanupDatabase() {

    }
}