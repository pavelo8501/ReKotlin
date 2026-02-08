package po.misc.debugging

import po.misc.collections.asList
import po.misc.data.logging.parts.DebugMethod.methodName
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.classifier.SimplePackageClassifier
import po.misc.exceptions.ExceptionPayload
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.debugging.stack_tracer.extractTrace
import po.misc.reflection.anotations.HelperFunction
import po.misc.reflection.anotations.hasAnnotation


internal fun normalizedMethodName(element:  StackTraceElement): String {

    fun nameFromParts(parts: List<String>): String{
        return when{
            parts.size >= 4 ->{
                val owner = parts[parts.size - 3].replace("_", " ")
                val lambdaName = parts[parts.size - 2]
                "Lambda -> $lambdaName on $owner"
            }
            parts.size >= 3 ->{
                val owner = parts.first().replace("_", " ")
                val lambdaIndex = parts[parts.size - 1]
                "Lambda -> Anonymous # $lambdaIndex on $owner"
            }
            parts.isEmpty() -> {
                "Lambda -> ${parts.first()}"
            }
            else -> "Lambda"
        }
    }

    val lambdaRegex = Regex("""lambda\$(.*?)\$\d+""")

    lambdaRegex.find(element.methodName)?.let { match ->
        val owner = match.groupValues[1].replace("_", " ")
        return "Lambda -> ${element.methodName.substringAfterLast('$')} on $owner"
    }
    if( element.methodName.contains("lambda")){
        val parts = element.methodName.split("$")
        return nameFromParts(parts)
    }
    if (element.methodName == "invoke" || element.methodName == "invokeSuspend") {
        val parts = element.className.split("$")
        return nameFromParts(parts)
    }
    if (element.methodName.startsWith("access$")) {
        return "Synthetic -> ${element.methodName.substringAfter("access$")}"
    }
    return element.methodName.substringAfterLast('$')
}

fun StackTraceElement.checkIfHelperFunctionAnnotated(): Boolean {
    return try {
        val cls = Class.forName(className)
        cls.hasAnnotation<HelperFunction>(methodName) != null
    }catch (notFound: ClassNotFoundException){
        notFound.output(Colour.Yellow)
        false
    }catch (th: Throwable){
        val payload = ExceptionPayload(th.message?:"Not found", "isHelperMethod", true, this)
        th.extractTrace(payload).output()
        throw th
    }
}

fun StackTraceElement.toFrameMeta(  classifier: PackageClassifier? = null): StackFrameMeta{

    val useClassifier = try {
        classifier?:run {
            SimplePackageClassifier(KnownHelpers)
        }
    }catch (th: Throwable){
        th.output()
        throw th
    }
    val className = className
    val simpleClasName = className.substringAfterLast('.')
    val parts : List<String> =  methodName.substringAfterLast('.').split('$')

    val displayMethodName = normalizedMethodName(this)
    val classPackage = className.substringBeforeLast('.', missingDelimiterValue = "")
    val packageRole = useClassifier.resolvePackageRole(this)
    val meta = try {
        StackFrameMeta(
            fileName = fileName?:"N/A",
            simpleClassName = simpleClasName,
            displayMethodName = displayMethodName,
            lineNumber = lineNumber,
            classPackage = classPackage,
            packageRole = packageRole,
            isReflection =  className.startsWith("java.lang.reflect"),
            isThreadEntry =  className == "java.lang.Thread" && methodName.contains("run"),
            isCoroutineInternal = className.startsWith("kotlinx.coroutines"),
            isInline =  methodName.contains($$"$inline$") || className.contains($$"$inlined$"),
            isLambda =  methodName.contains($$"$lambda") || className.contains($$"$Lambda$"),
            methodName= parts.getOrNull(1)?:parts[0],
            index = 0,
            stackTraceElement = this
        )
    }catch (th: Throwable){
        th.output()
        throw th
    }
    return meta
}


fun Collection<StackTraceElement>.toFrameMeta(classifier: PackageClassifier?): List<StackFrameMeta>{
    val result = mutableListOf<StackFrameMeta>()
    val stackTraceElements = this.toList()
    for(element in stackTraceElements){
        val rawFrameMeta = element.toFrameMeta(classifier)
        result.add(rawFrameMeta)
    }
    return result
}

internal fun lookupByMethod(
    frames:  List<StackFrameMeta>,
    options: TraceOptions,
    classifier: PackageClassifier? = null
): List<StackFrameMeta>{
    val method = options.methodName
    val selection: MutableList<StackFrameMeta> =mutableListOf()
    return when{

        options.lookup == TraceOptions.Lookup.ThisMethod -> {
            frames.firstOrNull { it.methodName == method }?.asList()?:emptyList()
        }
        options.lookup == TraceOptions.Lookup.BeforeThis -> {

            val methodIndex = frames.indexOfFirst {
                val name =  it.stackTraceElement?.methodName?: it.methodName
                name.contains(method)
            }
            if (methodIndex == -1) {
                return emptyList()
            }
            for (i in methodIndex downTo  0) {
                val meta =  frames[i].setIndex(i)
                selection.add(meta)
            }
            selection
        }
        else -> {
           emptyList()
        }
    }
}

fun Array<StackTraceElement>.toFrameMeta(classifier: PackageClassifier?): List<StackFrameMeta>{
    return toList().toFrameMeta(classifier)
}