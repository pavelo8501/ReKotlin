package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.token.GenericInfo
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


data class ClassInfo(
    internal val kClass: KClass<*>,
): PrettyPrint{


    private val genericParamsFormatted: String get() =
        if(genericInfo.isNotEmpty()){
            genericInfo.joinToString(prefix = "<", postfix = ">", separator = ", ") {
                it.formattedString
            }
        }else { "" }

    private val genericParams: String get() =
        if(genericInfo.isNotEmpty()){
            genericInfo.joinToString(prefix = "<", postfix = ">", separator = ", ") {
                it.parameterName
            }
        }else { "" }



    internal val genericInfoBacking = mutableListOf<GenericInfo>()

    var isLambda: Boolean = false
    var isSuspended: Boolean = false
        private set
    var hasReceiver: Boolean = false
        private set

    private val suspendTag : String get() =  if (isSuspended) "suspend " else ""
    private val receiveTag  : String get() = if (hasReceiver) "with receiver" else ""


    var simpleName : String = kClass.simpleOrAnon
        private set
    var qualifiedName: String = kClass.qualifiedName?:"Null"
        private set

    val completeName: String get() = "$simpleName$genericParams"

    val packageName: String = kClass.java.packageName

    var functionName : String? = null
    val normalizedName : String get() {
       return if(isLambda){
            "${suspendTag}fun $functionName $receiveTag in Package : $packageName"
        }else{
            simpleName
        }
    }


    val genericInfo: List<GenericInfo> = genericInfoBacking

    var stackFrame: StackFrameMeta? = null

    constructor(
        kClass: KClass<out Function<*>>,
        suspended: Boolean,
        withReceiver: Boolean,
    ):this(kClass){
        isSuspended = suspended
        hasReceiver = withReceiver
        isLambda = true
        val parts = kClass.java.name.substringAfterLast('.').split('$')
        functionName = parts.getOrNull(1) ?: parts[0]
    }



    
    val formattedClassName: String = "${simpleName.colorize(Colour.Yellow)}$genericParamsFormatted"

    override val formattedString: String get() = formattedClassName

    val resolutionText: String = buildString {
        appendLine(formattedClassName)
        appendLine("Declared in : $packageName")
        if(isLambda){
            appendLine("Is function  : $isLambda")
            appendLine("Suspending: $isSuspended")
        }
        if(stackFrame != null){
            appendLine(stackFrame?.formattedString)
        }
    }

    fun addTraceInfo(trace : StackFrameMeta): ClassInfo{
        stackFrame =  trace
        return this
    }

    fun addParamInfo(typeToken: TypeToken<*>): ClassInfo{
        val genericsList = typeToken.typeSlots.map { it.genericInfo }
        genericInfoBacking.addAll(genericsList)
        return this
    }

    fun addParamInfo(parameterName: String,  typeToken: TypeToken<*>): GenericInfo{
        val info = GenericInfo(parameterName, typeToken.kType, ClassResolver.classInfo(typeToken.kClass))
        genericInfoBacking.add(info)
        return info
    }

    override fun toString(): String {
        return buildString {
            appendLine("Qualified: $qualifiedName")
        }
    }
}
