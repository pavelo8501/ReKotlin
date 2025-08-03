package po.test.lognotify.notifictations

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.misc.containers.LazyBackingContainer
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.types.TypeData

class TestNotificationMethods{

   internal class SomeClass : TasksManaged {
       override val identity: CTXIdentity<SomeClass> = asIdentity()
       val container = LazyBackingContainer<SomeClass>(TypeData.create())
       fun logMessage(msg: String){
           container.testNotify2(this, msg)
           notify(msg)
       }
   }
    @Test
    fun `Some test`(){
        val someClass = SomeClass()
        someClass.logMessage("Message")
    }

}