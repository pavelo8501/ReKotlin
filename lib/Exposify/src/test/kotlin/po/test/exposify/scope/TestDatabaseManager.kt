package po.test.exposify.scope

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO

class TestDatabaseManager : DatabaseTest() {


    override val identity: CTXIdentity<TestDatabaseManager> = asIdentity()

    @Test
    fun `Open connection blocking with retries`(){
        withConnection{
            service(PageDTO){
                truncate()
                select()
            }
        }
    }
}
