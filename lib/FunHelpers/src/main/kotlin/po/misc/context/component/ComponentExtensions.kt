package po.misc.context.component

import po.misc.callbacks.signal.Signal
import po.misc.data.logging.Verbosity
import po.misc.debugging.ClassResolver


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


fun <T:Component> T.setName(name: String):T{
    this.componentID.useName(name)
    return this
}
