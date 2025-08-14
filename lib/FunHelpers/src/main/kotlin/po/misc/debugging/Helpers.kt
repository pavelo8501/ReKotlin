package po.misc.debugging

import po.misc.collections.takeFromLastMatching
import po.misc.context.CTX
import po.misc.data.helpers.output
import po.misc.data.helpers.stripAfter
import po.misc.exceptions.isLikelyUserCode
import po.misc.exceptions.models.StackFrameMeta
import po.misc.types.helpers.simpleOrNan


private fun listFramesFrames(methodName: String? = null): List<StackFrameMeta>{
    val filterFromMethodName = methodName?: "listFramesFrames"
    val trace =  Thread.currentThread().stackTrace
    val filtered = trace.takeFromLastMatching(3){ it.methodName == filterFromMethodName }
    return  filtered.map { it.toFrameMeta() }
}

private fun listFramesFrames(shift: Int, methodName: String? = null): List<StackFrameMeta>{
    val filterFromMethodName = methodName?: "listFramesFrames"
    val trace =  Thread.currentThread().stackTrace
    val filtered = trace.takeFromLastMatching(3, shift){ it.methodName == filterFromMethodName }
    return  filtered.map { it.toFrameMeta() }
}

fun CTX.createDebugFrame(methodName: String? = null):DebugFrame{
    val frameMetaList = listFramesFrames(methodName?:"createDebugFrame")
    return DebugFrame(frameMetaList, this)
}



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
        text = "Tested object of class ${this::class.simpleOrNan()} is not null"
        text +=  printout?.invoke(this)?:text
    }else{
        text = "Object is null"
        text +=  printout?.invoke(null)?:text
    }
    text.output()

    listFramesFrames(1, "checkNullability").first().output()

}
