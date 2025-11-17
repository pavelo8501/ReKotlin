package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.StackFrameMeta
import po.misc.functions.Nullable
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.token.TypeToken
import kotlin.collections.get
import kotlin.compareTo
import kotlin.reflect.KClass
import kotlin.reflect.KType


data class GenericInfo(
    val parameterName: String,
    val kType: KType,
    val classInfo: ClassInfo
): PrettyPrint{

    private val classDisplayName: String  get() {
       return if(kType.isMarkedNullable){
            "${classInfo.simpleName}?".colorize(Colour.Yellow)
        }else{
            classInfo.simpleName.colorize(Colour.Yellow)
        }
    }
    val isMarkedNullable: Boolean get() = kType.isMarkedNullable

    override val formattedString: String = "${parameterName.colorize(Colour.GreenBright)}: $classDisplayName"
}

data class ClassInfo(
    internal val kClass: KClass<*>,
    val hashCode: Int,
    val instanceName: String? = null
): PrettyPrint{

    val fromInstance: Boolean get() = instanceName != null
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

    val packageName: String = kClass.java.packageName

    var functionName : String? = null
    val normalizedName : String get() {
       return if(isLambda){
            "${suspendTag}fun $functionName $receiveTag in Package : $packageName"
        }else{
            simpleName
        }
    }
    internal val genericInfoBacking = mutableListOf<GenericInfo>()
    val genericInfo: List<GenericInfo> = genericInfoBacking
    var stackFrame: StackFrameMeta? = null

    constructor(
        kClass: KClass<out Function<*>>,
        suspended: Boolean,
        withReceiver: Boolean,
        hashCode: Int? = null,
    ):this(kClass, hashCode?:kClass.hashCode()){
        isSuspended = suspended
        hasReceiver = withReceiver
        isLambda = true
        val parts = kClass.java.name.substringAfterLast('.').split('$')
        functionName = parts.getOrNull(1) ?: parts[0]
    }
    constructor(nullable: Nullable): this(Nullable::class, 0){
        simpleName = "null"
        qualifiedName = "Null"
    }

    private val genericParamsStr: String get() = genericInfo.joinToString(separator = ", ") {
        it.formattedString
    }
    
    val formattedClassName: String = "${simpleName.colorize(Colour.Yellow)}<$genericParamsStr>"

    override val formattedString: String get() = formattedClassName

    val resolutionText: String = buildString {
        appendLine(formattedClassName)
        appendLine("Resolved by instance: $fromInstance")
        if(fromInstance){
            appendLine("Hash code: $hashCode")
        }else{
            appendLine("Declared in : $packageName")
            if(isLambda){
                appendLine("Is function  : $isLambda")
                appendLine("Suspending: $isSuspended")
            }
        }
        if(stackFrame != null){
            appendLine(stackFrame?.formattedString)
        }
    }


    fun addTraceInfo(trace : StackFrameMeta): ClassInfo{
        stackFrame =  trace
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
            appendLine("Hash Code: $hashCode")
        }
    }
}
