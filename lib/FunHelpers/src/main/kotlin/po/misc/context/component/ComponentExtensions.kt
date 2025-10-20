package po.misc.context.component

import po.misc.callbacks.signal.Signal
import po.misc.context.tracable.Notification
import po.misc.context.tracable.NotificationTopic
import po.misc.data.logging.LogRecord
import po.misc.data.logging.Verbosity
import po.misc.debugging.ClassResolver
import java.lang.classfile.components.ClassPrinter


fun <T: Component> T.componentID(componentName: String? = null):ComponentID{
    return componentName?.let {
        ComponentID(it, this)
    }?:run {
        ComponentID(ClassResolver.instanceName(this), ClassResolver.classInfo(this))
    }
}


fun <T: Component> T.componentID(componentName: String, verbosity: Verbosity = Verbosity.Info):ComponentID{
    ComponentID(componentName, this, verbosity)

    return componentName?.let {
        ComponentID(it, this)
    }?:run {
        ComponentID(ClassResolver.instanceName(this), ClassResolver.classInfo(this))
    }
}


fun <T: Component> T.applyID(componentID: ComponentID):T{
    when(this){
        is Signal<*, *> -> {
            this.componentID = componentID
        }
    }
    return this
}