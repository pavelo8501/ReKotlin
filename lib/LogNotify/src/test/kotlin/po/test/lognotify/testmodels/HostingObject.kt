package po.test.lognotify.testmodels

import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify


class ParentHostingObject() : HostingObjectBase(), CanNotify {

    override val eventHandler = RootEventHandler("ParentHostingObject")
    val subObjects : MutableList<HostingObject> = mutableListOf()

}

class HostingObject(val number: Int, parent : ParentHostingObject) : HostingObjectBase(), CanNotify {

    override val eventHandler = EventHandler("HostingObject/$number", parent.eventHandler )
    val subObjects : MutableList<HostingObject> = mutableListOf()

}

abstract class HostingObjectBase