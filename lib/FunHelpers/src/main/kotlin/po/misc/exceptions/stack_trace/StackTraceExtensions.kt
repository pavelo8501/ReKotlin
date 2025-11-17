package po.misc.exceptions.stack_trace

import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.classifier.PackageRole
import po.misc.exceptions.classifier.classifyPackage
import po.misc.reflection.anotations.HelperFunction
import po.misc.reflection.anotations.hasAnnotation


fun StackTraceElement.toMeta(): StackFrameMeta {
    val classPackage = className.substringBeforeLast('.', missingDelimiterValue = "")
    val role = classifyPackage(classPackage)

    return StackFrameMeta(
        fileName = this.fileName?:"N/A",
        simpleClassName = className.substringAfterLast('.'),
        methodName = methodName,
        lineNumber = lineNumber,
        classPackage = classPackage,
        isHelperMethod = role == PackageRole.Helper,
        isUserCode = role == PackageRole.User,
        stackTraceElement = this
    )
}

fun List<StackTraceElement>.toMeta(): List<StackFrameMeta> = map { it.toMeta() }


fun StackTraceElement.isHelperMethod(): Boolean {
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
