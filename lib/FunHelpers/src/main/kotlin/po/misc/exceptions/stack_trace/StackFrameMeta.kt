package po.misc.exceptions.stack_trace

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.exceptions.classifier.PackageRole
import po.misc.exceptions.classifier.classifyPackage


data class StackFrameMeta(
    val fileName: String,
    val simpleClassName: String,
    val methodName: String,
    val lineNumber: Int,
    val classPackage: String,
    val isHelperMethod: Boolean,
    val isUserCode: Boolean,
    val isReflection: Boolean,
    val isThreadEntry: Boolean,
    val isCoroutineInternal: Boolean,
    val isInline: Boolean,
    val isLambda: Boolean,
    val stackTraceElement: StackTraceElement? = null
): PrettyPrint {

    enum class PrintFormat{
        Full,
        Complete,
        TillMethodName,
    }

    private var printFormat: PrintFormat = PrintFormat.Full

    val consoleLink: String get() = "$classPackage.$simpleClassName.$methodName($fileName:$lineNumber)"

    private val printPair = PrettyRow(PrettyCell(10, PrettyPresets.Key), PrettyCell(20,  PrettyPresets.Value))


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

    override val formattedString: String get()  =  buildString {
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
        return "File name: $fileName Simple class name: $simpleClassName Method name: $methodName"
    }
    companion object {
        fun normalizedMethod(methodName: String, className: String): String {

            fun nameFromParts(parts: List<String>): String{
               return when{
                    parts.size >= 4 ->{
                        val owner = parts[parts.size - 3].replace("_", " ")
                        val lambdaName = parts[parts.size - 2]
                        return "Lambda -> $lambdaName on $owner"
                    }
                   parts.size >= 3 ->{
                       val owner = parts.first().replace("_", " ")
                       val lambdaIndex = parts[parts.size - 1]
                       return "Lambda -> Anonymous # $lambdaIndex on $owner"
                   }
                    parts.isEmpty() -> {
                        "Lambda -> ${parts.first()}"
                    }
                    else -> "Lambda"
                }
            }

            val lambdaRegex = Regex("""lambda\$(.*?)\$\d+""")
            lambdaRegex.find(methodName)?.let { match ->
                val owner = match.groupValues[1].replace("_", " ")
                return "Lambda -> ${methodName.substringAfterLast('$')} on $owner"
            }
            if( methodName.contains("lambda")){
                val parts = methodName.split("$")
                return nameFromParts(parts)
            }
            if (methodName == "invoke" || methodName == "invokeSuspend") {
                val parts = className.split("$")
                return nameFromParts(parts)
            }
            if (methodName.startsWith("access$")) {
                return "Synthetic -> ${methodName.substringAfter("access$")}"
            }
            return methodName.substringAfterLast('$')
        }

        fun create(traceElement: StackTraceElement): StackFrameMeta{
            val className = traceElement.className
            val simpleClasName = className.substringAfterLast('.')
            val normalizedName = normalizedMethod(traceElement.methodName, simpleClasName)
            val classPackage = className.substringBeforeLast('.', missingDelimiterValue = "")
            val role = classifyPackage(classPackage)

            return StackFrameMeta(
                fileName = traceElement.fileName?:"N/A",
                simpleClassName = simpleClasName,
                methodName = normalizedName,
                lineNumber = traceElement.lineNumber,
                classPackage = classPackage,
                isHelperMethod = role == PackageRole.Helper,
                isUserCode = role == PackageRole.User,
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