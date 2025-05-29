package po.test.exposify.dto

import org.junit.jupiter.api.Test
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.UserDTO

class TestDTOConfiguration : DatabaseTest() {



    @Test
    fun `Dto configuration routine executed completely`(){


        startTestConnection {
            service(UserDTO){

            }
        }
    }

}