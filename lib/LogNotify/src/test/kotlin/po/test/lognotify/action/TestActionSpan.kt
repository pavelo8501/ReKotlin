package po.test.lognotify.action

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.action.runInlineAction
import po.lognotify.extensions.runTask
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.asIdentifiableClass
import kotlin.test.assertEquals

class TestActionSpan: TasksManaged {

    override val contextName: String
        get() = "TestActionSpan"

    class FactoryClass() : IdentifiableClass, InlineAction{
       override val identity:  ClassIdentity = asIdentifiableClass("TestActionSpan", "FactoryClass")

       private var counter:Int = 0

       private fun  privateMethod(): String{
          counter++
          return "Produced:${counter}"
       }

        fun method1() : String = runInlineAction("method1"){
            privateMethod()
        }
    }

    val factory : FactoryClass = FactoryClass()

    @Test
    fun `ActionSpan test`(){

        runTask("RootTask"){handler->
            factory.method1()
            factory.method1()
            factory.method1()

            assertEquals(3, handler.actions.size, "ActionSpan size mismatch")
        }

    }

}