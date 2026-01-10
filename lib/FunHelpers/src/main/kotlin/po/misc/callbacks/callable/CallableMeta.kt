package po.misc.callbacks.callable


import po.misc.data.strings.appendVertical
import kotlin.reflect.KClass


class CallableMeta(
    val funcClass:  KClass<out Function<*>>,
    val receiver: KClass<*>? = null,
    val parts : List<String> = funcClass.java.name.substringAfterLast('.').split('$')
) {

    val isSuspended: Boolean = false
    val javaName:String = receiver?.java?.canonicalName ?: ""
    val packageName: String = funcClass.java.packageName
    var funcClassName:String? = null
    var receiverClass: KClass<*>? = null
    val functionName : String = parts.getOrNull(1)?:parts[0]
    val receiverName : String = receiverClass?.simpleName?:parts[0]
    val hasReceiver: Boolean get() = receiverClass != null
    var suspended: Boolean = false
    val isLambda : Boolean  = true
    private val suspendTag : String get() =  if (suspended) "suspend " else ""
    private val receiveTag  : String get() = if (hasReceiver) "with receiver" else ""

    val displayName : String get() = if(isLambda){
            "${suspendTag}fun $functionName $receiveTag in Package : $packageName"
        }else{
             funcClassName?:"Anonymous"
        }

    override fun toString(): String =
        buildString {
            appendVertical("CallableMeta", ::javaName, ::packageName, ::functionName, ::hasReceiver, ::suspended)
        }
}