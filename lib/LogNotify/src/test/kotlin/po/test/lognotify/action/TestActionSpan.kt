package po.test.lognotify.action

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.action.runInlineAction
import po.lognotify.extensions.runTask
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.asIdentifiableModule
import kotlin.test.assertEquals

class TestActionSpan: TasksManaged {

    class FactoryClass() : InlineAction{

       val module: Identifiable = asIdentifiable("Page", "DTOClass")

       private var counter:Int = 0

       private fun  privateMethod(): String{
          counter++
          return "Produced:${counter}"
       }

        fun method1() : String = runInlineAction(module, "method1"){
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