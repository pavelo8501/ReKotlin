package po.misc.debugging

import po.misc.collections.takeFromLastMatching
import po.misc.collections.takeFromMatch
import po.misc.context.CTX
import po.misc.data.text.stripAfter
import po.misc.exceptions.isLikelyUserCode
import po.misc.exceptions.models.StackFrameMeta


fun CTX.createDebugFrame(methodName: String? = null):DebugFrame{
    val filterFromMethodName = methodName?: "createDebugFrame"
    val trace =  Thread.currentThread().stackTrace
    val filtered = trace.takeFromLastMatching(3){ it.methodName == filterFromMethodName }
    val frameMetaList = filtered.map { it.toFrameMeta() }
    return DebugFrame(this, frameMetaList)
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
