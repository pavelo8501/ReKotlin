package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.TestPageDTO
import po.test.exposify.setup.dtos.TestUser
import po.test.exposify.setup.dtos.TestUserDTO
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestSimpleDelegate : DatabaseTest() {

    @Test
    fun `referenced property binding`() = runTest {
        val user = TestUser(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        val page = pageModelsWithSections(pageCount = 1, updatedBy = user.id,  sectionsCount = 1).firstOrNull()
        var updatedDTO : TestPageDTO? = null
        startTestConnection()?.run {
            service(TestUserDTO, TableCreateMode.CREATE) {
                update(user)
            }
            service(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
                updatedDTO = update(page!!).getDTO() as TestPageDTO
            }
        }

        val updatedPageDTO =  assertNotNull(updatedDTO, "Page DTO update failure")

        assertAll("Reference property updated",
            { assertEquals(user.id, updatedPageDTO.updatedById, "In DTO. Expected ${user.id}") },
            {assertEquals(user.id, updatedPageDTO.dataModel.updatedById, "In DataModel. Expected ${user.id}") }
          )
    }
}