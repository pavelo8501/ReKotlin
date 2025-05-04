package po.test.exposify.dto

import org.junit.Test
import po.auth.extensions.generatePassword
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDTOTracker: DatabaseTest() {

    @Test
    fun `information updated and stored`(){

        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        val connection = startTestConnection()
        var userDTO : UserDTO? = null
        connection?.service(UserDTO) {
            userDTO = update(user).getDTO() as UserDTO
        }

        assertNotNull(userDTO)
        val tracker = userDTO.dtoTracker
        tracker.printTrace()
        assertTrue("No records present") { tracker.records.count() > 0 }

    }

}