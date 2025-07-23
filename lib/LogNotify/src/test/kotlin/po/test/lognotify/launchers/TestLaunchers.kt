package po.test.lognotify.launchers

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.extensions.runAction
import po.lognotify.extensions.runInlineAction
import po.lognotify.extensions.runTask
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class TestLaunchers: TasksManaged {

    override val identity: CTXIdentity<out CTX> = asIdentity()

    val property1: String = "TestLaunchers"

    @Test
    fun `All launch functions preserve access to original context`(){

        val unitType = typeOf<Unit>()
        runTask<TestLaunchers, Unit>("SomeTask"){
            assertEquals("TestLaunchers", property1)
        }
        runInlineAction("TestLaunchers"){
            assertEquals("TestLaunchers", property1)
        }
        runAction("TestLaunchers", unitType){
            assertEquals("TestLaunchers", property1)
        }
    }
}