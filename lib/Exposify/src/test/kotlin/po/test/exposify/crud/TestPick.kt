package po.test.exposify.crud

import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import org.junit.jupiter.api.Test
import po.exposify.scope.service.models.TableCreateMode
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestPick : DatabaseTest(){

    @Test
    fun `user updates and pick`(){
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        var pickedDTO : UserDTO? = null

        withConnection{
            service(UserDTO, TableCreateMode.Create) {

                val userDataModel =  update(user).getDataForced()
                 pickedDTO = pickById(userDataModel.id).dto
            }
        }

        val userDTO =  assertNotNull(pickedDTO, "Picked DTO is null")
        assertNotEquals(0,  userDTO.id, "UserDTO failed to update")
        assertAll("Asserting picked data model",
            { assertNotEquals(0, userDTO.id, "Failed to update") },
            { assertEquals(user.login, userDTO.login, "Input model/picked model login mismatch. Expecting ${user.login}") }
        )
    }
}