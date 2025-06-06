package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.dto.components.result.toResultList
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.sequence.extensions.switchContext
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.setup.ClassData
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.MetaData
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import po.test.exposify.setup.pagesSectionsContentBlocks
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestResponsiveDelegates : DatabaseTest(), TasksManaged {

    companion object{
        @JvmStatic
        var updatedById : Long = 0
    }

    @BeforeAll
    fun setup() = runTest {

        val loggerHandler: LogNotifyHandler  = logNotify()
        loggerHandler.notifierConfig {
            console = ConsoleBehaviour.MuteNoEvents
        }
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection{
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                updatedById = update(user).getDataForced().id
            }
        }
    }

    @Test
    fun `Serialize delegates update data correctly on update and select`(){

        val classInitialList : List<ClassData> = listOf(ClassData(1,"class_1"))
        val classUpdatedList : List<ClassData> = listOf(ClassData(1,"class_1"), ClassData(2,"class_2"), ClassData(3,"class_3"))
        val page = pageModelsWithSections(
            pageCount = 1,
            sectionsCount = 2,
            updatedBy =  updatedById,
            classes = classInitialList).first()

        lateinit var updated : List<Section>
        lateinit var selected : List<Section>

        startTestConnection {
            service(PageDTO){
               val firstInsert =  update(page).getDataForced()
               firstInsert.sections.forEach { it.classList = classUpdatedList }
               updated = update(firstInsert).getDataForced().sections.map{it}
               selected = select(true).getData().flatMap{ it.sections }
            }
        }

        val classListInSelection = selected.flatMap { it.classList }

        assertEquals(3, updated.first().classList.size, "ClassList do not update")
        assertTrue(classListInSelection.size == 6, "ClassList items were not persisted")

        assertEquals(2, selected.size, "Selected sections count is not 2")
        assertEquals(updated.size, selected.size, "Updated and Selected Sections size mismatch")
        assertEquals("class_3", classListInSelection[2].value, "Value parameter of serialized ClassItem mismatch")
        assertEquals(classListInSelection[2].value, classListInSelection.last().value, "Value parameter mismatch across selection")
    }

    @Test
    fun `Property delegates update data correctly on update and select`(){
        val controlName = "Some Caption"
        val page = pagesSectionsContentBlocks(
            pageCount = 1,
            sectionsCount = 2,
            contentBlocksCount = 3,
            updatedBy = updatedById).first()

        page.sections[0].name = controlName
        page.sections.forEach {section->
            section.name = controlName
            section.contentBlocks.forEach { contentBlock->
                contentBlock.content = controlName
            }
        }

        var updated : Page? = null
        var selected : Page? = null

        startTestConnection {
            service(PageDTO, TableCreateMode.FORCE_RECREATE){
                updated =  update(page).getData()
                selected = select().getData().firstOrNull()
            }
        }

        val updatedPage = assertNotNull(updated)
        val selectedPage = assertNotNull(selected)

        assertNotEquals(0, updatedPage.id)
        assertEquals(updatedPage.id, selectedPage.id, "Page id mismatch")
        assertTrue(updatedPage.sections.size == 2, "Sections empty in updated")
        assertTrue(selectedPage.sections.size == 2, "Sections empty in selected")
        assertEquals(controlName, updatedPage.sections.first().name, "Name property on Sections was not updated")
        assertEquals(controlName, selectedPage.sections.first().name, "Name property on Sections was not updated on Select")
        val updatedFirstSection = updatedPage.sections.first()
        val selectedFirstSection = selectedPage.sections.first()
        val selectedLastSection = selectedPage.sections.last()
        assertEquals(updatedFirstSection.name, updatedPage.sections.last().name, "Name property mismatch in firs and last updated Section")
        assertEquals(selectedFirstSection.name, selectedLastSection.name,"Name property mismatch in firs and last selected Section")
        assertTrue(updatedFirstSection.contentBlocks.size == 3, "ContentBlocks wrong size in updated")

        val firstContentBlockOfFistSection = selectedFirstSection.contentBlocks.first()
        val lastContentBlockOfLastSection = selectedLastSection.contentBlocks.last()
        assertAll("Asserting ContentBlocks of selection",
            { assertTrue(selectedFirstSection.contentBlocks.size == 3, "ContentBlocks empty in selected") },
            { assertEquals(controlName, firstContentBlockOfFistSection.content, "Content property on ContentBlock mismatch in selection") },
            { assertEquals(firstContentBlockOfFistSection.content, lastContentBlockOfLastSection.content, "Content property on ContentBlocks mismatch") }
        )
    }
}