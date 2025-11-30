package po.misc.debugging

import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.classifier.SimplePackageClassifier
import po.misc.exceptions.ExceptionPayload
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.reflection.anotations.HelperFunction
import po.misc.reflection.anotations.hasAnnotation


fun StackTraceElement.normalizedMethodName(): String {

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

fun StackTraceElement.toFrameMeta(classifier: PackageClassifier? = null): StackFrameMeta{


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
    val normalizedName = normalizedMethodName()
    val classPackage = className.substringBeforeLast('.', missingDelimiterValue = "")
    val packageRole = useClassifier.resolvePackageRole(this)


    val meta = try {
        StackFrameMeta(
            fileName = fileName?:"N/A",
            simpleClassName = simpleClasName,
            methodName = normalizedName,
            lineNumber = lineNumber,
            classPackage = classPackage,
            packageRole = packageRole,
            isReflection =  className.startsWith("java.lang.reflect"),
            isThreadEntry =  className == "java.lang.Thread" && methodName.contains("run"),
            isCoroutineInternal = className.startsWith("kotlinx.coroutines"),
            isInline =  methodName.contains($$"$inline$") || className.contains($$"$inlined$"),
            isLambda =  methodName.contains($$"$lambda") || className.contains($$"$Lambda$"),
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

fun Array<StackTraceElement>.toFrameMeta(classifier: PackageClassifier?): List<StackFrameMeta>{
    return toList().toFrameMeta(classifier)
}