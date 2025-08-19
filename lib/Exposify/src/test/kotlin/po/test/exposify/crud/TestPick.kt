package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import org.junit.jupiter.api.Test
import po.exposify.dto.components.query.deferredQuery
import po.exposify.dto.components.query.whereQuery
import po.exposify.scope.launchers.pick
import po.exposify.scope.service.models.TableCreateMode
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.Pages
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.mocks.mockedSession
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestPick : DatabaseTest(){

    override val identity: CTXIdentity<TestPick> = asIdentity()

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
                val userDataModel =  update(user).dataUnsafe
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

    @Test
    fun `user pick statement work as expected`()= runTest{

        with(mockedSession){
            val query = deferredQuery(PageDTO){ equals(Pages.name, "ss")}
            pick(PageDTO, query)
        }
    }

}