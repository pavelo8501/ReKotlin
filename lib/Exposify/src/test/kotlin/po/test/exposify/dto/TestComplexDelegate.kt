package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.sectionsPreSaved
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestComplexDelegate : DatabaseTest() {


    @Test
    fun `parent2IdReference property binding un update`() = runTest {

        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        val connection = startTestConnection()
        connection?.service(UserDTO) {
            user = update(user).getData()
        }
        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedById = user.id)
        var updatedPageData: Page? = null
        var updatedPageDTO : PageDTO? = null
        connection?.service(PageDTO, TableCreateMode.CREATE) {
            page.sections.addAll(sourceSections)
            val updateResult = update(page)
            updatedPageData = updateResult.getData()
            updatedPageDTO = updateResult.getDTO() as PageDTO
        }
        val persistedDataSection  = updatedPageData?.sections?.first()
        val persistedDtoSection = updatedPageDTO?.sections?.first() as? SectionDTO

        assertNotNull(persistedDataSection, "Failed to get DataSection after update")
        assertNotNull(persistedDtoSection, "Failed to get DtoSection after update")

        assertAll("idReferenced updated",
            { assertNotEquals(0, updatedPageData.id, "updatedPageData id failed to update") },
            { assertEquals(updatedPageData.id, persistedDataSection.pageId, "PageId in data model not updated. Expected ${updatedPageData.id}") },
            { assertEquals(updatedPageData.id, persistedDtoSection.pageId, "PageId in DTO not updated.. Expected ${updatedPageData.id}") },
          )
    }

    @Test
    fun `parent2IdReference property binding un select`() = runTest {

        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        val connection = startTestConnection()
        connection?.service(UserDTO) {
            user = update(user).getData()
        }
        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedById = user.id)
        var selectedData: Page? = null
        var selectedDTO : PageDTO? = null
        var updatedSectionsCount = 0
        connection?.service(PageDTO, TableCreateMode.CREATE) {
            page.sections.addAll(sourceSections)
            updatedSectionsCount = update(page).getData().sections.count()

            val selectionResult = select()

            selectedData = selectionResult.getData().firstOrNull()
            selectedDTO = selectionResult.getDTO().firstOrNull() as? PageDTO
        }

        val selectedPageData = assertNotNull(selectedData)
        val selectedPageDTO = assertNotNull(selectedDTO)

        val persistedDataSection  = selectedPageData.sections.firstOrNull()
        val persistedDtoSection = selectedPageDTO.sections.firstOrNull() as? SectionDTO

        assertNotEquals(0, updatedSectionsCount, "Sections update failed. Expected count ${page.sections.count()}")
        assertNotNull(persistedDataSection, "Failed to get DataSection after update")
        assertNotNull(persistedDtoSection, "Failed to get DtoSection after update")

        assertAll("idReferenced updated",
            { assertNotEquals(0, selectedPageData.id, "updatedPageData id failed to update") },
            { assertEquals(
                selectedPageData.id,
                persistedDataSection.pageId,
                "PageId in data model not updated. Expected ${selectedPageData.id}") },
            { assertEquals(
                selectedPageData.id,
                persistedDtoSection.pageId,
                "PageId in DTO not updated. Expected ${selectedPageData.id}") },
        )

    }

//
//    @Test
//    fun `parent2IdReference property binding`() = runTest {
//        var user = TestUser(
//            id = 0,
//            login = "some_login",
//            hashedPassword = generatePassword("password"),
//            name = "name",
//            email = "nomail@void.null")
//
//        var updatedDTO : TestPageDTO? = null
//        var pickedDTO : TestPageDTO? = null
//        startTestConnection()?.run {
//            service(TestUserDTO, TableCreateMode.CREATE) {
//                user =  update(user).getData()
//            }
//            service(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
//                val page = pageModels(pageCount = 1, updatedBy = user.id,).firstOrNull()
//                updatedDTO = update(page!!).getDTO() as TestPageDTO
//                pickedDTO = pick(updatedDTO.id).getDTO() as TestPageDTO
//            }
//        }
//        val updatedPageDTO =  assertNotNull(updatedDTO, "Page DTO update failure")
//        assertAll("idReferenced updated on update",
//            { assertNotEquals(0, updatedPageDTO.updatedBy, "idReferenced property update failure") },
//            { assertEquals(user.id, updatedPageDTO.updatedBy, "In DTO. Expected ${user.id}") },
//            { assertEquals(user.id, updatedPageDTO.dataModel.updatedBy, "In DataModel. Expected ${user.id}") }
//          )
//
//        val pickedPageDTO =  assertNotNull(pickedDTO, "Page DTO pick failure")
//        assertAll("idReferenced updated on pick",
//            { assertNotEquals(0, pickedPageDTO.updatedBy, "idReferenced property update failure") },
//            { assertEquals(user.id, pickedPageDTO.updatedBy, "In DTO. Expected ${user.id}") },
//            { assertEquals(user.id, pickedPageDTO.dataModel.updatedBy, "In DataModel. Expected ${user.id}") }
//        )
//    }
//
//    @Test
//    fun `parentReference property binding`() = runTest {
//
//        var user = TestUser(
//            id = 0,
//            login = "some_login",
//            hashedPassword = generatePassword("password"),
//            name = "name",
//            email = "nomail@void.null")
//
//        var updatedDTO : TestPageDTO? = null
//        var pickedDTO : TestPageDTO? = null
//
//        startTestConnection()?.run {
//            service(TestUserDTO, TableCreateMode.CREATE) {
//                user =  update(user).getData()
//            }
//            service(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
//                val page = pageModelsWithSections(pageCount = 1, sectionsCount = 1,  updatedBy = user.id).first()
//                updatedDTO = update(page).getDTO() as TestPageDTO
//                pickedDTO = pick(updatedDTO.id).getDTO() as TestPageDTO
//            }
//        }
//
//        val updatedDataModel =  assertNotNull(updatedDTO!!.dataModel, "Page DataModel update failed")
//        assertEquals(1, updatedDataModel.sections.count(), "Section count does not meet expected 1")
//        val firstSection = updatedDataModel.sections.first()
//        assertAll("parentReference updated on update",
//            { assertNotEquals(0, firstSection.pageId, "parentReference property update failure") },
//            { assertEquals(updatedDataModel.id, firstSection.pageId, "In DataModel. Expected ${updatedDataModel.id}") },
//        )
//
//        val pickedDataModel =  assertNotNull(pickedDTO!!.dataModel, "Page DataModel update failed")
//        val pickedPageDataModel =  assertNotNull(pickedDataModel, "Page Data Model pick failure")
//        assertEquals(1, pickedPageDataModel.sections.count(), "Section count does not meet expected 1")
//        val firstPickedSection = pickedPageDataModel.sections.first()
//        assertAll("idReferenced updated on pick",
//            { assertNotEquals(0, firstPickedSection.pageId, "idReferenced property update failure") },
//            { assertEquals(user.id, firstPickedSection.pageId, "In DTO. Expected ${user.id}") },
//        )
//    }
//
}