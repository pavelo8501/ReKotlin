package po.misc.exceptions.stack_trace

import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.exceptions.ExceptionPayload
import po.misc.reflection.anotations.HelperFunction
import po.misc.reflection.anotations.hasAnnotation


fun StackTraceElement.toMeta(): StackFrameMeta = StackFrameMeta.create(this)

fun List<StackTraceElement>.toMeta(): List<StackFrameMeta> = map { StackFrameMeta.create(it) }

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
