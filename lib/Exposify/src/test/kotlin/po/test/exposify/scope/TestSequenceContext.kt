package po.test.exposify.scope

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.extensions.WhereCondition
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.extensions.createHandler
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Pages
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModels
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestSequenceContext : DatabaseTest() {

    companion object{
        var updatedById : Long = 0
    }

    @Test
    fun `sequence launched with conditions and input work`() = runTest {
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        val pageClasses = listOf<ClassItem>(ClassItem(1, "class_1"), ClassItem(2, "class_2"))
        val  connectionContext = startTestConnection()
        connectionContext?.let { connection ->
            connection.service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                updatedById = update(user).getData().id
            }
            connection.service<PageDTO, Page>(PageDTO, TableCreateMode.CREATE) {
                truncate()
                sequence(createHandler(SequenceID.UPDATE)) { inputList, conditions ->
                    update(inputList)
                }
                sequence(createHandler(SequenceID.SELECT)) { inputList, conditions ->
                    select(conditions)
                }
            }
        }
        val pages = pageModels(pageCount = 4, updatedBy = updatedById)
        pages[1].name = "this_name"
        pages[1].langId = 2
        pages[2].langId = 2
        pages[3].langId = 2

        val updatedPages = PageDTO.Companion.runSequence(SequenceID.UPDATE.value) {
            withInputData(pages)
        }

        assertAll(
            { assertEquals(4, updatedPages.count(), "Updated page count mismatch") },
            { assertNotEquals(0, updatedPages[0].id, "Page Update failure") },
            { assertEquals("this_name", updatedPages[1].name, "Updated page name mismatch") }
        )

        val selectPages = PageDTO.Companion.runSequence<Page>(SequenceID.SELECT.value) {
            withConditions(WhereCondition<Pages>().equalsTo(Pages.langId, 2))
        }

        assertAll(
            { assertEquals(3, selectPages.count(), "Selected page count mismatch") },
            { assertNotEquals(0, selectPages[0].id, "Page Update failure") },
            { assertEquals("this_name", selectPages[0].name, "Selected page 1 name mismatch") }
        )
    }

}