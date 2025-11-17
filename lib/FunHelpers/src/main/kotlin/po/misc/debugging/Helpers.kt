package po.misc.debugging

import po.misc.context.CTX
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.helpers.stripAfter
import po.misc.exceptions.isLikelyUserCode
import po.misc.exceptions.stack_trace.StackFrameMeta
import po.misc.types.helpers.simpleOrAnon


fun StackTraceElement.toFrameMeta(knownPackages : KnownPackages = KnownPackages): StackFrameMeta {
    val classPackage = this.className.substringBeforeLast('.', missingDelimiterValue = "")
    val isHelper = knownPackages.helperPackagePrefixes.any { prefix -> this.className.startsWith(prefix) }
    val isUser = !isHelper && classPackage.isLikelyUserCode()

    return StackFrameMeta(
        fileName = this.className,
        simpleClassName = this.className.substringAfterLast("."),
        methodName = this.methodName,
        lineNumber = this.lineNumber,
        classPackage = classPackage,
        isHelperMethod = isHelper,
        isUserCode = isUser
    )
}

fun StackFrameMeta.toConsoleLink(): String {
    val simpleClassName = fileName.substringAfterLast('.')
    val fileName = "$simpleClassName.kt" // or use `.java` if applicable
    return "\tat ${fileName.stripAfter('$')}.$methodName($fileName:$lineNumber)"
}

fun <T: Any> T?.checkNullability(printout:((T?)-> String)? = null){
    var text = ""
    if(this != null){
        text = "Tested object of class ${this::class.simpleOrAnon} is not null"
        text +=  printout?.invoke(this)?:text
    }else{
        text = "Object is null"
        text +=  printout?.invoke(null)?:text
    }
    text.output()
}


fun TraceableContext.identityData(): DebugFrameData{
    val kClass = this::class
    val simpleName =kClass.simpleOrAnon
    var hash: Int = 0
   return when(this){
        is CTX -> {
            DebugFrameData(this)
        }
        is TraceableContext->{
            hash = this.hashCode()
            DebugFrameData(simpleName, numericId = hash.toLong())
        }

    }
}
