package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.exposify.extensions.WhereCondition
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.extensions.createHandler
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.DatabaseTest
import po.test.exposify.setup.TestClassItem
import po.test.exposify.setup.TestPage
import po.test.exposify.setup.TestPageDTO
import po.test.exposify.setup.TestPages
import po.test.exposify.setup.TestUser
import po.test.exposify.setup.TestUserDTO
import po.test.exposify.setup.pageModels
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestSequenceContext : DatabaseTest() {

    companion object{
        var updatedById : Long = 0
    }

    @Test
    fun `sequence launched with conditions and input work`() = runTest{
        val user = TestUser("some_login", "name", "nomail@void.null", "******")
        val pageClasses = listOf<TestClassItem>(TestClassItem(1,"class_1"), TestClassItem(2, "class_2"))
        connectionContext?.let { connection ->
            connection.service<TestUserDTO, TestUser>(TestUserDTO, TableCreateMode.FORCE_RECREATE) {
                updatedById =  update(user).getData().id
            }
            connection.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.CREATE) {
                truncate()
                sequence(createHandler(SequenceID.UPDATE)){inputList, conditions->
                    update(inputList)
                }
                sequence(createHandler(SequenceID.SELECT)){inputList, conditions->
                    select(conditions)
                }
            }
        }
        val pages = pageModels(quantity = 4, updatedBy = updatedById, pageClasses = pageClasses)
        pages[1].name = "this_name"
        pages[1].langId = 2
        pages[2].langId = 2
        pages[3].langId = 2

        val updatedPages = TestPageDTO.runSequence<TestPage>(SequenceID.UPDATE, this@runTest.coroutineContext){
            withInputData(pages)
        }

        assertAll(
            { assertEquals(4, updatedPages.count(), "Updated page count mismatch") },
            { assertNotEquals(0, updatedPages[0].id, "Page Update failure") },
            { assertEquals("this_name", updatedPages[1].name, "Updated page name mismatch") }
        )

        val selectPages = TestPageDTO.runSequence<TestPage>(SequenceID.SELECT, this@runTest.coroutineContext){
            withConditions(WhereCondition<TestPages>(TestPages).equalsTo(TestPages.langId, 2))
        }

        assertAll(
            { assertEquals(3, selectPages.count(), "Selected page count mismatch") },
            { assertNotEquals(0, selectPages[0].id, "Page Update failure") },
            { assertEquals("this_name", selectPages[0].name, "Selected page 1 name mismatch") }
        )

    }

}