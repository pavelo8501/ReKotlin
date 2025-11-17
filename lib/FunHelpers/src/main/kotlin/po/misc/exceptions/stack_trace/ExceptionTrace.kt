package po.misc.exceptions.stack_trace

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.throwableToText
import kotlin.reflect.KClass

data class ExceptionTrace(
    val exceptionName: String,
    val stackFrames: List<StackFrameMeta>,
    val kClass: KClass<*>? = null
): PrettyPrint{

    var reliable: Boolean = true
        internal set
    var bestPick:StackFrameMeta = stackFrames.first()
    var ctxName: String = ""

    constructor(
        exception: Throwable,
        stackFrames: List<StackFrameMeta>,
        contextClass: KClass<*>? = null
    ): this(
        exception.throwableToText(),
        stackFrames,
        contextClass
    )

    override val formattedString: String =  exceptionName.colorize(Colour.Red).newLine {
        bestPick.formattedString
    }

    init {
        require(stackFrames.isNotEmpty()) { "stackFrames must contain at least one frame" }
    }

    internal fun setBestPick(frameMeta : StackFrameMeta):ExceptionTrace{
        bestPick = frameMeta
        return this
    }

    fun addKnownContextData(name: String):ExceptionTrace{
        ctxName = name
        return this
    }

    override fun toString(): String {
        return buildString {
            appendLine(exceptionName)
            appendLine("Trace for ${bestPick.simpleClassName}")
            appendLine(bestPick)
        }
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
): PrettyPrint {
    val consoleLink: String get() = "$classPackage.$simpleClassName.$methodName($fileName:$lineNumber)"

   // override val formattedString: String get() = ""

    val normalizedMethodName: String
        get() = methodName
            .replace(Regex("""lambda\$\d+"""), "[lambda]")
            .replace('_', ' ')
            .replace('$', '.')


    override val formattedString: String get() {
        return buildString {
            appendLine("File name: $fileName")
            appendLine("Simple class name: $simpleClassName")
            appendLine("Method name: $methodName")
            appendLine("Line number: $lineNumber")
            appendLine("Class package: $classPackage")
            appendLine("Is helper method: $isHelperMethod")
            appendLine("Is user code: $isUserCode")
            appendLine(consoleLink)
        }
    }


//    fun output() {
//        val outputString = buildString {
//            appendLine("Simple class name: $simpleClassName")
//            appendLine("Method name: $methodName")
//            appendLine("Line number: $lineNumber")
//            appendLine(consoleLink)
//        }
//        println(outputString)
//    }

    override fun toString(): String {
        return "File name: $fileName Simple class name: $simpleClassName Method name: $methodName"
    }
}

