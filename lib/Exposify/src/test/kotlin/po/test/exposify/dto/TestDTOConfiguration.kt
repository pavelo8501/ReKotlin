package po.test.exposify.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.auth.extensions.generatePassword
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.exceptions.InitException
import po.exposify.scope.service.enums.TableCreateMode
import po.misc.callbacks.manager.Containable
import po.misc.exceptions.ManagedException
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiableClass
import po.test.exposify.dto.TestDTOTracker.Companion.updatedById
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.dtos.ContentBlockDTO
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import po.test.exposify.setup.pagesSectionsContentBlocks
import kotlin.test.assertEquals
import kotlin.test.assertIs

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDTOConfiguration : DatabaseTest(), IdentifiableClass {

    override val identity = asIdentifiableClass("TestDTOConfiguration", "TestDTOConfiguration")
    override val contextName: String
        get() = identity.componentName

    companion object {
        @JvmStatic()
        var userId: Long = 0
    }

    @Test
    fun `Validator reports fails when missing init`() {
        val exception = assertThrows<InitException> {
            withConnection {
                service(PageDTO) {

                }
            }
        }
    }


    @Test
    fun `Happy path validator reports all green`() {
        startTestConnection()
        fun onInitialized(dto: Containable<DTOBase<PageDTO, Page, PageEntity>>){
            val pageDTOClass  = assertIs<PageDTO.Companion>(dto.getData())
            assertEquals(DTOClassStatus.Initialized, pageDTOClass.status, "Root DTO uninitialized")
            assertEquals(DTOClassStatus.Initialized, SectionDTO.status, "SectionDTO uninitialized")
            assertEquals(DTOClassStatus.Initialized, ContentBlockDTO.status, "ContentBlockDTO uninitialized")
            assertEquals(DTOClassStatus.Initialized, UserDTO.status, "UserDTO uninitialized")
        }

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        withConnection {
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                updatedById = update(user).getDataForced().id
            }
        }

        val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = updatedById)
        PageDTO.onInitialized.request(this, ::onInitialized)
        assertDoesNotThrow {
            withConnection {
                service(PageDTO) {
                    update(pages)
                }
            }
        }
    }
}