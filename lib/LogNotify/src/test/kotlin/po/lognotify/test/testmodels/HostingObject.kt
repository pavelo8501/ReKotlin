package po.lognotify.test.testmodels

import kotlinx.coroutines.delay
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify


class ParentHostingObject(val name: String
) : CanNotify {

    override val eventHandler = RootEventHandler(name)

    val childObjects = mutableListOf<HostingObject>()


    fun exposeSelf(block: ParentHostingObject.()-> Unit ){
        this.block()
    }

    suspend fun mockTaskRun(delayTime: Long, block: suspend  ParentHostingObject.()->Unit){
        block(this@ParentHostingObject)
        delay(delayTime)
        childObjects.forEach {
            it.propagateParentTask(delayTime)
        }
    }
    fun returnAsResult(param: Any): Any{
        return param
    }
}

class HostingObject(val moduleName: String, parent : ParentHostingObject) : CanNotify {

    override val eventHandler = EventHandler(moduleName, parent.eventHandler)
    var currentParentParam :Any? = null
    var currentParam : Any? = null
    val subObjects = mutableListOf<SubHostingObject>()

    suspend fun propagateParentTask(delayTime: Long, block: (HostingObject.()->Unit)? = null){
        delay(delayTime)
        if(block != null){
            this.block()
        }
    }

    suspend fun mockTaskRun(delayTime: Long, parentParam: Any? = null, block : suspend  HostingObject.()->Unit){
        this.block()
        delay(delayTime)
        currentParentParam = parentParam
    }
}

class SubHostingObject(val moduleName: String, parent : HostingObject) : CanNotify {

    override val eventHandler = EventHandler(moduleName, parent.eventHandler)
    var currentParentParam :Any? = null
    val subObjects = mutableListOf<SubHostingObject>()


    suspend fun mockTaskRun(delayTime: Long, parentParam: Any? = null, block : SubHostingObject.()->Unit){
        this.block()
        delay(delayTime)
        currentParentParam = parentParam
    }
}


