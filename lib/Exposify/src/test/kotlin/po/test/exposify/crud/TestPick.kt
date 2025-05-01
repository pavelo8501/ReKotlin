package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.TestUserEntity
import po.test.exposify.setup.dtos.TestUser
import po.test.exposify.setup.dtos.TestUserDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestPick : DatabaseTest(){

    @Test
    fun `user updates and pick`() = runTest {
        val user = TestUser(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        var pickedDTO : TestUserDTO? = null

        startTestConnection()?.run {
            service(TestUserDTO, TableCreateMode.CREATE) {
                val userDataModel =  update(user).getData()
                 pickedDTO = pick<TestUserEntity>(userDataModel.id).getDTO() as TestUserDTO
            }
        }?:throw Exception("Connection not available")

        val userDTO =  assertNotNull(pickedDTO, "Picked DTO is null")
        assertNotEquals(0,  userDTO.id, "UserDTO failed to update")
        assertAll("Asserting picked data model",
            { assertNotEquals(0, userDTO.dataModel.id, "Failed to update") },
            { assertEquals(user.login, userDTO.dataModel.login, "Input model/picked model login mismatch. Expecting ${user.login}") }
        )
    }
}