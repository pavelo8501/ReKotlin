package po.test.exposify.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.auth.extensions.generatePassword
import po.exposify.DatabaseManager
import po.exposify.dto.DTOBase
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.exceptions.InitException
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.misc.callbacks.Containable
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.dto.TestDTOTracker.Companion.updatedById
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.dtos.ContentBlockDTO
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPage
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDTOConfiguration : DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<TestDTOConfiguration> = asIdentity()

    companion object {
        @JvmStatic()
        var userId: Long = 0
    }

    @Test
    fun `Validator reports fails when missing init`() {
        assertThrows<InitException> {
            withConnection {
                service(PageDTO)
            }
        }
        assertEquals(DTOClassStatus.Uninitialized, PageDTO.status)
        assertTrue(DatabaseManager.connections.isEmpty())
    }

    @Test
    fun `Happy path validator reports all green`() {
        startTestConnection()
        assertDoesNotThrow {
            withConnection {
                service(UserDTO)
                service(PageDTO)
            }
        }
    }
}