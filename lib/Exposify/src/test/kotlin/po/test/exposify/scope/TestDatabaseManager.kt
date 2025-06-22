package po.test.exposify.scope

import org.junit.jupiter.api.Test
import po.exposify.scope.sequence.extensions.sequence
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO

class TestDatabaseManager : DatabaseTest() {

    @Test
    fun `Open connection blocking with retries`(){
        withConnection{
            service(PageDTO){
                 truncate()

                sequence(PageDTO.UPDATE){
                    select()
                }
            }
        }
    }
}
