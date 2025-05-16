package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.MetaTag
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.assertEquals
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
    fun `Serializable delegates`(){
        val classes : List<ClassItem> = listOf(ClassItem(1,"class_1"), ClassItem(2,"class_2"), ClassItem(3,"class_3"))
        val metaTags : List<MetaTag> = listOf(MetaTag(1,"key1", "value1"), MetaTag(2,"key2", "value2"))
        val inputPages = pageModelsWithSections(
            pageCount = 1,
            sectionsCount = 1,
            updatedBy =  updatedById,
            classes = classes,
            metaTags = metaTags)

        var updatedDto : PageDTO? = null
        var selectedDTOs: List<PageDTO>? = null
        startTestConnection {
            service(PageDTO){
                updatedDto = update(inputPages).getDTO().firstOrNull()
                selectedDTOs = select().getDTO()
            }
        }

        val pageDto = assertNotNull(updatedDto, "Updated DTO is null")
        assertTrue(pageDto.sections.size == inputPages[0].sections.size, "Section list size does not match input")
        val section = pageDto.sections.first()
        assertTrue(section.classList.size == inputPages[0].sections[0].classList.size, "ClassList size does not match input")
        assertTrue(section.metaTags.size == inputPages[0].sections[0].metaTags.size, "MetaTags size does not match input")

        assertAll("ClassList properties persisted",
            { assertEquals(classes[0].key, section.classList[0].key, "Key property mismatch. Expecting ${classes[0].key}") },
            { assertEquals(classes[0].value, section.classList[0].value, "Value property mismatch. Expecting ${classes[0].value}") },
            { assertEquals(classes[2].key, section.classList[2].key, "Key property mismatch. Expecting ${classes[2].key}")},
            { assertEquals(classes[2].value, section.classList[2].value, "Value property mismatch. Expecting ${classes[2].value}") }
        )

        assertAll("MetaTag properties persisted",
            { assertEquals(metaTags[0].key, section.metaTags[0].key, "Key property mismatch. Expecting ${metaTags[0].key}") },
            { assertEquals(metaTags[0].value, section.metaTags[0].value, "Value property mismatch. Expecting ${metaTags[0].value}") },
            { assertEquals(metaTags[0].type, section.metaTags[0].type, "Type property mismatch. Expecting ${metaTags[0].type}") },
            { assertEquals(metaTags[1].key, section.metaTags[1].key, "Key property mismatch. Expecting ${metaTags[1].key}")},
            { assertEquals(metaTags[1].value, section.metaTags[1].value, "Value property mismatch. Expecting ${metaTags[1].value}") },
            { assertEquals(metaTags[1].type, section.metaTags[1].type, "Type property mismatch. Expecting ${metaTags[1].type}") }
        )

        assertTrue(selectedDTOs?.size == inputPages[0].sections.size, "Section list size does not match input")
        val selectedSection = pageDto.sections.first()
        assertTrue(selectedSection.classList.size == inputPages[0].sections[0].classList.size, "ClassList size does not match input")
        assertTrue(selectedSection.metaTags.size == inputPages[0].sections[0].metaTags.size, "MetaTags size does not match input")

        assertAll("ClassList properties persisted",
            { assertEquals(classes[0].key, selectedSection.classList[0].key, "Key property mismatch. Expecting ${classes[0].key}") },
            { assertEquals(classes[0].value, selectedSection.classList[0].value, "Value property mismatch. Expecting ${classes[0].value}") },
            { assertEquals(classes[2].key, selectedSection.classList[2].key, "Key property mismatch. Expecting ${classes[2].key}")},
            { assertEquals(classes[2].value, selectedSection.classList[2].value, "Value property mismatch. Expecting ${classes[2].value}") }
        )

        assertAll("MetaTag properties persisted",
            { assertEquals(metaTags[0].key, selectedSection.metaTags[0].key, "Key property mismatch. Expecting ${metaTags[0].key}") },
            { assertEquals(metaTags[0].value, selectedSection.metaTags[0].value, "Value property mismatch. Expecting ${metaTags[0].value}") },
            { assertEquals(metaTags[0].type, selectedSection.metaTags[0].type, "Type property mismatch. Expecting ${metaTags[0].type}") },
            { assertEquals(metaTags[1].key, selectedSection.metaTags[1].key, "Key property mismatch. Expecting ${metaTags[1].key}")},
            { assertEquals(metaTags[1].value, selectedSection.metaTags[1].value, "Value property mismatch. Expecting ${metaTags[1].value}") },
            { assertEquals(metaTags[1].type, selectedSection.metaTags[1].type, "Type property mismatch. Expecting ${metaTags[1].type}") }
        )

    }
}