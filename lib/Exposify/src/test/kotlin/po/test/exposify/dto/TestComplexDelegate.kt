package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.TestPageDTO
import po.test.exposify.setup.dtos.TestUser
import po.test.exposify.setup.dtos.TestUserDTO
import po.test.exposify.setup.pageModels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestComplexDelegate : DatabaseTest() {

    @Test
    fun `idReferenced property binding`() = runTest {
        var user = TestUser(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        var updatedDTO : TestPageDTO? = null
        var pickedDTO : TestPageDTO? = null
        startTestConnection()?.run {
            service(TestUserDTO, TableCreateMode.CREATE) {
                user =  update(user).getData()
            }
            service(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
                val page = pageModels(pageCount = 1, updatedBy = user.id,).firstOrNull()
                updatedDTO = update(page!!).getDTO() as TestPageDTO
                pickedDTO = pick(updatedDTO.id).getDTO() as TestPageDTO
            }
        }
        val updatedPageDTO =  assertNotNull(updatedDTO, "Page DTO update failure")
        assertAll("idReferenced updated on update",
            { assertNotEquals(0, updatedPageDTO.updatedById, "idReferenced property update failure") },
            { assertEquals(user.id, updatedPageDTO.updatedById, "In DTO. Expected ${user.id}") },
            { assertEquals(user.id, updatedPageDTO.dataModel.updatedBy, "In DataModel. Expected ${user.id}") }
          )

        val pickedPageDTO =  assertNotNull(pickedDTO, "Page DTO pick failure")
        assertAll("idReferenced updated on pick",
            { assertNotEquals(0, pickedPageDTO.updatedById, "idReferenced property update failure") },
            { assertEquals(user.id, pickedPageDTO.updatedById, "In DTO. Expected ${user.id}") },
            { assertEquals(user.id, pickedPageDTO.dataModel.updatedBy, "In DataModel. Expected ${user.id}") }
        )
    }
}