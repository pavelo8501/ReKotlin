package po.test.lognotify.testmodels

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify


class ParentHostingObject(val moduleName: String
) : CanNotify {

    override val eventHandler = RootEventHandler(moduleName)

    val childObjects = mutableListOf<HostingObject>()

    suspend fun mockTaskRun(delayTime: Long, block: ParentHostingObject.()->Unit){
        block(this@ParentHostingObject)
        delay(delayTime)
        childObjects.forEach {
            it.propagateParentTask(delayTime)
        }
    }
}

class HostingObject(val moduleName: String, parent : ParentHostingObject) : CanNotify {

    override val eventHandler = EventHandler(moduleName, parent.eventHandler)
    val subObjects = mutableListOf<SubHostingObject>()

    suspend fun propagateParentTask(delayTime: Long){
        delay(delayTime)
    }
}

class SubHostingObject(val moduleName: String, parent : HostingObject) : CanNotify {

    override val eventHandler = EventHandler(moduleName, parent.eventHandler)
    val subObjects = mutableListOf<HostingObject>()

    suspend fun propagateParentTask(delayTime: Long){
        delay(delayTime)
    }
}
