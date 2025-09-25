package po.misc.exceptions.models

import kotlin.reflect.KClass

data class ExceptionTrace(
    val kClass: KClass<*>,
    val stackFrames: List<StackFrameMeta>,
    var bestPick:StackFrameMeta
){

    var ctxName: String = ""

    fun addKnownContextData(name: String):ExceptionTrace{
        ctxName =name
        return this
    }

}

data class StackFrameMeta(
    val fileName: String,
    val simpleClassName: String,
    val methodName: String,
    val lineNumber: Int,
    val classPackage: String,
    val isHelperMethod: Boolean,
    val isUserCode: Boolean,
    val stackTraceElement: StackTraceElement? = null
){

    val normalizedMethodName: String
        get() = methodName
            .replace(Regex("""lambda\$\d+"""), "[lambda]")
            .replace('_', ' ')
            .replace('$', '.')

    override fun toString(): String {
       return buildString {
            appendLine("File name: $fileName")
            appendLine("Simple class name: $simpleClassName")
            appendLine("Method name: $methodName")
            appendLine("Line number: $lineNumber")
            appendLine("Class package: $classPackage")
            appendLine("Is helper method: $isHelperMethod")
            appendLine("Is user code: $isUserCode")
        }
    }
}

