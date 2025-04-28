package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.TestUser
import po.test.exposify.setup.dtos.TestUserDTO
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestUpdate : DatabaseTest() {

    @Test
    fun `user updates`() = runTest {
        val user = TestUser(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        var userDataModel: TestUser? = null
        startTestConnection()?.run {
            service(TestUserDTO, TableCreateMode.CREATE){
               userDataModel = update(user).getData()
            }
        }?:throw Exception("Connection not available")

        val updatedUser = assertNotNull(userDataModel, "Updated data model is null")
        assertNotNull(updatedUser, "User Update failure")
        assertAll("User Properties Updates",
            { assertEquals(user.login, updatedUser.login, "Login property update failure. Should be: ${user.login}") },
            { assertNotEquals("password", updatedUser.hashedPassword, "Login property update failure. Should be: ${user.login}") }
            )
    }
}