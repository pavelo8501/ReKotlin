package po.exposify.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import po.auth.AuthSessionManager
import po.exposify.DatabaseManager
import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.ConnectionContext2
import po.exposify.scope.service.enums.TableCreateMode
import po.exposify.test.setup.TestPageDTO

abstract class DatabaseTest {


    private fun initExposify(block : ConnectionContext2.()-> Unit){

        val conncontext = DatabaseManager.openConnectionSync(
            ConnectionInfo(
                host = "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;",
                dbName= "test_db",
                user = "sa",
                pwd = "",
                driver="org.h2.Driver"),
            sessionManager = AuthSessionManager)
        conncontext?.block()
    }


    @BeforeEach
    fun setupDatabase() {

       initExposify(){
           service(TestPageDTO, TableCreateMode.FORCE_RECREATE) {


           }
       }
    }

    @AfterEach
    fun cleanupDatabase() {

    }
}