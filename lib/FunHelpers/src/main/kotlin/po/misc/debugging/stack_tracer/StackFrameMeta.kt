package po.misc.debugging.stack_tracer

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.classifier.SimplePackageClassifier
import po.misc.debugging.normalizedMethodName
import po.misc.types.k_class.simpleOrAnon

data class StackFrameMeta(
    val fileName: String,
    val simpleClassName: String,
    val methodName: String,
    val lineNumber: Int,
    val classPackage: String,
    val packageRole: PackageClassifier.PackageRole,
    val isReflection: Boolean,
    val isThreadEntry: Boolean,
    val isCoroutineInternal: Boolean,
    val isInline: Boolean,
    val isLambda: Boolean,
    val stackTraceElement: StackTraceElement? = null
): PrettyPrint {

    enum class PrintFormat{ Full, Complete, TillMethodName}

    val isHelperMethod: Boolean get() = packageRole == PackageClassifier.PackageRole.Helper
    val isUserCode: Boolean get() = packageRole != PackageClassifier.PackageRole.System


    private var printFormat: PrintFormat = PrintFormat.Full

    val consoleLink: String get() = "$classPackage.$simpleClassName.$methodName($fileName:$lineNumber)"

    private val printPair = PrettyRow(PrettyCell(10, PrettyPresets.Key), PrettyCell(20, PrettyPresets.Value))

    private val tillMethodName : String = buildString {
        appendLine(printPair.render("File name", fileName))
        appendLine(printPair.render("Simple class name", simpleClassName))
        append(printPair.render("Method name", methodName))
    }

    private val auxInfo : String = buildString {
        appendLine(printPair.render("Line number", lineNumber))
        appendLine(printPair.render("Class package", classPackage))
        appendLine(printPair.render("Is helper method", isHelperMethod))
        append(printPair.render("Is user code", isUserCode))
    }

    fun setPrintFormat(format:PrintFormat): StackFrameMeta{
        printFormat = format
        return this
    }

    override val formattedString: String get() = buildString {
        when(printFormat){
            PrintFormat.Full ->{
                appendLine(consoleLink)
                appendLine(tillMethodName)
                appendLine(auxInfo)
            }
            PrintFormat.Complete ->{
                appendLine(tillMethodName)
                appendLine(auxInfo)
            }
            PrintFormat.TillMethodName ->{
                appendLine(tillMethodName)
            }
        }
    }
    override fun toString(): String {
        val name = this::class.simpleOrAnon
        return "$name [File name: $fileName, Simple class name: $simpleClassName, "+
        "Method name: $methodName,  Is helper: $isHelperMethod]"
    }
    companion object {


        internal fun create(element: StackTraceElement, classifier: PackageClassifier):StackFrameMeta{
            val className = element.className
            val simpleClasName = className.substringAfterLast('.')
            val normalizedName = element.normalizedMethodName()
            val classPackage = className.substringBeforeLast('.', missingDelimiterValue = "")
            val packageRole = classifier.resolvePackageRole(element)

            return StackFrameMeta(
                fileName = element.fileName?:"N/A",
                simpleClassName = simpleClasName,
                methodName = normalizedName,
                lineNumber = element.lineNumber,
                classPackage = classPackage,
                packageRole = packageRole,
                isReflection =  className.startsWith("java.lang.reflect"),
                isThreadEntry =  className == "java.lang.Thread" && element.methodName.contains("run"),
                isCoroutineInternal = className.startsWith("kotlinx.coroutines"),
                isInline =  element.methodName.contains($$"$inline$") || className.contains($$"$inlined$"),
                isLambda =  element.methodName.contains($$"$lambda") || className.contains($$"$Lambda$"),
                stackTraceElement = element
            )
        }

        fun create(traceElement: StackTraceElement): StackFrameMeta{

            val classifier = SimplePackageClassifier(KnownHelpers)

            val className = traceElement.className
            val simpleClasName = className.substringAfterLast('.')
            val normalizedName = traceElement.normalizedMethodName()
            val classPackage = className.substringBeforeLast('.', missingDelimiterValue = "")
            val packageRole = classifier.resolvePackageRole(traceElement)

            return StackFrameMeta(
                fileName = traceElement.fileName?:"N/A",
                simpleClassName = simpleClasName,
                methodName = normalizedName,
                lineNumber = traceElement.lineNumber,
                classPackage = classPackage,
                packageRole = packageRole,
                isReflection =  className.startsWith("java.lang.reflect"),
                isThreadEntry =  className == "java.lang.Thread" && traceElement.methodName.contains("run"),
                isCoroutineInternal = className.startsWith("kotlinx.coroutines"),
                isInline =  traceElement.methodName.contains($$"$inline$") || className.contains($$"$inlined$"),
                isLambda =  traceElement.methodName.contains($$"$lambda") || className.contains($$"$Lambda$"),
                stackTraceElement = traceElement
            )
        }
    }
}