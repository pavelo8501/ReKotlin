package po.misc.exceptions

import po.misc.collections.takeFromMatch
import po.misc.exceptions.models.ExceptionTrace
import po.misc.exceptions.models.StackFrameMeta
import kotlin.reflect.KClass


enum class PackageRole {
    Helper,
    User,
    Unknown
}

fun classifyPackage(
    classPackage: String,
    helperPrefixes: List<String> = listOf("po.misc", "kotlin", "java"),
    userPrefixes: List<String> = listOf("po.test", "com.myapp")
): PackageRole {
    return when {
        helperPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.") } ->
            PackageRole.Helper

        userPrefixes.any { prefix -> classPackage == prefix || classPackage.startsWith("$prefix.") } ->
            PackageRole.User

        else -> PackageRole.Unknown
    }
}


/**
 * Generates an [ExceptionTrace] from a [TraceableContext].
 *
 * ## Purpose
 * - Provides a convenient way to capture the current execution context
 *   for logging, testing, or debugging.
 * - Automatically delegates to [ContextTracer] for full trace computation.
 *
 * Example:
 * ```
 * val trace = context.metaFrameTrace()
 * ```
 */
fun TraceableContext.metaFrameTrace(

):ExceptionTrace{
  return  ContextTracer(this).exceptionTrace
}

/**
 * Generates an [ExceptionTrace] from a [Throwable] and a [TraceableContext].
 *
 * ## Parameters
 * - [context] → The context used to identify the trace.
 * - [framesCount] → Number of frames to capture from the stack trace (default 2).
 *
 * ## Usage
 * ```
 * val trace = someThrowable.metaFrameTrace(context)
 * ```
 */
fun Throwable.metaFrameTrace(
    context: TraceableContext,
    framesCount: Int = 2
): ExceptionTrace = metaFrameTrace(context::class)


/**
 * Generates an [ExceptionTrace] from a [Throwable] and a target [KClass].
 *
 * ## Parameters
 * - [kClass] → The class used to identify the relevant stack frames.
 * - [framesCount] → Number of frames to include starting from the match (default 2).
 *
 * ## Behavior
 * - Takes the stack trace from the throwable.
 * - Uses [takeFromMatch] to extract frames starting from the first frame
 *   that matches [kClass.qualifiedName].
 * - Converts each [StackTraceElement] into a [StackFrameMeta], classifying packages
 *   as `Helper` or `User`.
 *
 * Example:
 * ```
 * val trace = someThrowable.metaFrameTrace(MyClass::class)
 * val topFrame = trace.stackFrames.first()
 * println(topFrame.normalizedMethodName)
 * ```
 */
fun Throwable.metaFrameTrace(
    kClass: KClass<*>,
    framesCount: Int = 2
): ExceptionTrace {

    val frames = stackTrace.takeFromMatch<StackTraceElement>(framesCount) {
        it.className == kClass.qualifiedName
    }
  val result =  frames.map { ste ->
        val classPackage = ste.className.substringBeforeLast('.', missingDelimiterValue = "")
        val role = classifyPackage(classPackage)
        StackFrameMeta(
            fileName = ste.className,
            simpleClassName = ste.className.substringAfterLast('.'),
            methodName = ste.methodName,
            lineNumber = ste.lineNumber,
            classPackage = classPackage,
            isHelperMethod = role == PackageRole.Helper,
            isUserCode = role == PackageRole.User,
            stackTraceElement = ste
        )
    }
  return  ExceptionTrace(kClass, result)
}