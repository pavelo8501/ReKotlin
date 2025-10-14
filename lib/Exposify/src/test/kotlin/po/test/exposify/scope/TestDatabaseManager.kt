package po.test.exposify.scope

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A
import po.exposify.extensions.respondOnUpdate
import po.exposify.extensions.withServiceContext
import po.exposify.scope.connection.ConnectionClass
import po.lognotify.notification.models.ConsoleBehaviour
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.TestItem
import po.test.exposify.setup.dtos.TestItemDTO
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDatabaseManager : DatabaseTest() {

    override val identity: CTXIdentity<TestDatabaseManager> = asIdentity()

    companion object {
        @JvmStatic
        lateinit var connClass: ConnectionClass
    }

     @BeforeAll
    fun setup() {

         connClass =  withConnection {

            service(TestItemDTO) {

            }
        }
    }

    @Test
    fun `Identity data model can be safely tracked`(){

        var foundDto: Any? = null
        var triggerCount: Int = 0

        val testItem2 = TestItem(0, "ShouldNotReact", null)
        val testItem = TestItem(0, "TestItem", "Value")

        TestItemDTO.withServiceContext {
            update(testItem)
            update(testItem2)
        }
        val dto =  assertIs<TestItemDTO>(foundDto)
        assertEquals(testItem.name,  dto.name)
        assertEquals(1, triggerCount)
    }

    fun `Open connection blocking with retries`(){
        withConnection{
            service(PageDTO){
                truncate()
                select()
            }
        }
    }
}
