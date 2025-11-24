package po.misc.context.component


import po.misc.data.logging.Verbosity
import po.misc.data.logging.log_subject.Configuration
import po.misc.data.logging.log_subject.Initialization
import po.misc.data.logging.log_subject.updateText
import po.misc.data.badges.Badge
import po.misc.data.logging.log_subject.StartProcessSubject
import po.misc.debugging.ClassResolver
import kotlin.reflect.KFunction


fun <T: Component> T.componentID(
    componentName: String? = null
):ComponentID{
    return  if(componentName != null){
        ComponentID(this, nameProvider = { componentName })
    }else{
        ComponentID(this)
    }
}

fun <T: Component> T.componentID(
    componentName: String,
    verbosity: Verbosity = Verbosity.Info
):ComponentID{
  return  ComponentID(this, verbosity, { componentName })
}

fun <T: Component> T.componentID(
    nameProvider: () ->  String,
    verbosity: Verbosity = Verbosity.Info
):ComponentID {
    return  ComponentID(this, verbosity, nameProvider =  nameProvider)
}

fun <T:Component> T.setName(name: String):T{
    this.componentID.useName(name)
    return this
}

val Component.initSubject: Initialization get() = Initialization.updateText("Initializing ${ClassResolver.instanceName(this)}", null)
val Component.configSubject: Configuration  get() = Configuration.updateText("Configuring ${ClassResolver.instanceName(this)}")









