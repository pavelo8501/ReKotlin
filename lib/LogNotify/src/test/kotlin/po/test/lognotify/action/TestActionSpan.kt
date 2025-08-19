package po.test.lognotify.action

import org.junit.jupiter.api.Test
import po.lognotify.launchers.runAction
import po.lognotify.launchers.runTask
import po.test.lognotify.setup.FakeTasksManaged
import kotlin.test.assertEquals

class TestActionSpan: FakeTasksManaged {

    override val contextName: String
        get() = "TestActionSpan"

    class FactoryClass() : FakeTasksManaged {

       private var counter:Int = 0

       private fun  privateMethod(): String{
          counter++
          return "Produced:${counter}"
       }

        fun method1() : String = runAction("method1"){
            privateMethod()
        }
    }

    val factory : FactoryClass = FactoryClass()

    @Test
    fun `ActionSpan test`(){

        runTask("RootTask"){
            factory.method1()
            factory.method1()
            factory.method1()

            assertEquals(3, taskHandler.actions.size, "ActionSpan size mismatch")
        }

    }

}