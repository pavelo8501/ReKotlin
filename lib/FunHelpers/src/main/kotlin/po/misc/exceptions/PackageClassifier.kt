package po.misc.exceptions

import po.misc.collections.selectUntil
import po.misc.collections.takeFromMatch
import po.misc.context.CTX
import po.misc.exceptions.models.CTXResolutionFlag
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



private fun buildExceptionTrace(
    throwable:  Throwable,
    kClass: KClass<*>,
    emitter:TraceableContext?,
    framesCount: Int = 3,
    flag: CTXResolutionFlag = CTXResolutionFlag.Resolvable
): ExceptionTrace{
    fun traceElementToMeta(ste: StackTraceElement): StackFrameMeta {
        val classPackage = ste.className.substringBeforeLast('.', missingDelimiterValue = "")
        val role = classifyPackage(classPackage)
        return StackFrameMeta(
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
    fun resolveAsTraceable(ctx: TraceableContext, trace: ExceptionTrace){
        if(ctx != TraceableContext.NonResolvable){
            when(ctx){
                is CTX->{
                    trace.addKnownContextData(ctx.identifiedByName)
                }
            }
        }
    }
    fun tryResolveEmitter(trace: ExceptionTrace,  emitter: Any?):ExceptionTrace{
        if(emitter != null){
            when(emitter){
                is TraceableContext ->{
                    resolveAsTraceable(emitter, trace)
                }
            }
        }
        return trace
    }

    val filteredPick = throwable.stackTrace.toList().selectUntil {
        it.className == kClass.java.name
    }

    val reversedReduced = filteredPick.drop((filteredPick.size - framesCount).coerceAtLeast(0))

    val metaList = if (reversedReduced.isNotEmpty()) {
        val result =  reversedReduced.map { ste ->
            traceElementToMeta(ste)
        }
        ExceptionTrace(kClass, result, result.first())
    } else {
        ExceptionTrace(kClass, emptyList(), traceElementToMeta(throwable.stackTrace.first()))
    }
    if(flag == CTXResolutionFlag.Resolvable){
      return  tryResolveEmitter(metaList, emitter)
    }
    return  metaList
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
    framesCount: Int = 3,
    flag: CTXResolutionFlag = CTXResolutionFlag.Resolvable
):ExceptionTrace{

   val tracer = ContextTracer(this, flag)
    return buildExceptionTrace(tracer, this::class, this, framesCount,  flag)
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
    framesCount: Int = 3,
    flag: CTXResolutionFlag = CTXResolutionFlag.Resolvable
): ExceptionTrace = buildExceptionTrace(this, context::class, context, framesCount, flag)


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
    framesCount: Int = 3,
): ExceptionTrace  = buildExceptionTrace(this, kClass, null, framesCount, CTXResolutionFlag.NoResolution)