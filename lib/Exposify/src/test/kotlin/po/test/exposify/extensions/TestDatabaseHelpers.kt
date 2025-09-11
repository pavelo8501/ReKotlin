package po.test.exposify.extensions

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertThrows
import po.exposify.exceptions.InitException
import po.exposify.extensions.withDefaultConnection
import po.exposify.scope.connection.ConnectionClass
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import kotlin.test.assertIs
import kotlin.test.assertSame


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDatabaseHelpers: DatabaseTest(startImmediately = false) {

    override val identity: CTXIdentity<TestDatabaseHelpers> = asIdentity()

    @Test
    @Order(1)
    fun `WithDefaultConnection throws exception if no active connections`(){
        assertThrows<InitException> {
            withDefaultConnection {

            }
        }
    }

    @Test
    @Order(2)
    fun `WithDefaultConnection returns same object after multiple calls`(){
        var firstCall: Any? = null
        var secondCall: Any? = null
        withConnection {

            withDefaultConnection {
                firstCall = this
            }
            withDefaultConnection {
                secondCall = this
            }
        }
        assertIs<ConnectionClass>(firstCall)
        assertIs<ConnectionClass>(secondCall)
        assertSame(firstCall, secondCall)
    }
}