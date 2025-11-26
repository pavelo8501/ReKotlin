package po.misc.debugging

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.models.InstanceInfo


class CompareInstances(
    val instanceInfo1:  InstanceInfo,
    val instanceInfo2: InstanceInfo
): PrettyPrint {

    val isSameInstance : Boolean = instanceInfo1.instanceHash == instanceInfo2.instanceHash
    private val equalityText: String get() {
       return if(isSameInstance){
            " is same instance as ".colorize(Colour.Green)
        }else{
            " not the same instance as ".colorize(Colour.Red)
        }
    }

    private val instance1Hash : String get() {
       return  if(isSameInstance){
            instanceInfo1.instanceHash.stringify(Colour.Green).formatedString
        }else{
            instanceInfo1.instanceHash.stringify(Colour.Red).formatedString
        }
    }

    private val instance2Hash : String get() {
        return  if(isSameInstance){
            instanceInfo2.instanceHash.stringify(Colour.Green).formatedString
        }else{
            instanceInfo2.instanceHash.stringify(Colour.Red).formatedString
        }
    }

    override val formattedString: String
        get() = buildString {
                append(instanceInfo1.className.colorize(Colour.YellowBright))
                append(" $instance1Hash")
                append(" " + instanceInfo2.className.colorize(Colour.YellowBright))
                append(" $instance2Hash")
            }
}

fun <T: Any> TraceableContext.compare(instance1:T, instance2: T): CompareInstances{
    val classInfo1 = ClassResolver.instanceInfo(instance1)
    val classInfo2 = ClassResolver.instanceInfo(instance2)
    return CompareInstances(classInfo1, classInfo2)
}