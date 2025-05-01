package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.TestPage
import po.test.exposify.setup.dtos.TestPageDTO
import po.test.exposify.setup.dtos.TestUser
import po.test.exposify.setup.dtos.TestUserDTO
import po.test.exposify.setup.pageModels
import po.test.exposify.setup.pageModelsWithSections
import po.test.exposify.setup.sectionsPreSaved
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestComplexDelegate : DatabaseTest() {

    @Test
    fun `idReferenced property binding by extended list`() = runTest {


        var user = TestUser(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        val connection = startTestConnection()

        connection?.service(TestUserDTO) {
            user = update(user).getData()
        }
        val sourceSections = sectionsPreSaved(0)
        val page = TestPage(id = 0, name = "home", langId = 1, updatedById = user.id)
        var updatedDataModel: TestPage? = null
        connection?.service(TestPageDTO, TableCreateMode.CREATE) {
            page.sections.addAll(sourceSections)

            updatedDataModel = update(page).getData()
        }

        val updatedPageData = assertNotNull(updatedDataModel)
        val sections = updatedPageData.sections
        assertEquals(sourceSections.count(), sections.count())
        val firstSelected = sections[0]
        assertEquals(user.id, firstSelected.updatedBy, "User id and updated mismatch")
        assertEquals(2, firstSelected.sectionItems.count(), "SectionItems cont mismatch")
        assertNull(firstSelected.sectionItems.firstOrNull{  it.id == 0L }, "SectionItems not updated")
        assertNotEquals(0, firstSelected.sectionItems[0].sectionId, "SectionItemId did not updated")
    }
//
//    @Test
//    fun `idReferenced property binding`() = runTest {
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