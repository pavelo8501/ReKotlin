package po.misc.debugging.stack_tracer

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.stack_tracer.reports.CallSiteReport
import po.misc.exceptions.throwableToText
import java.time.Instant
import kotlin.reflect.KClass

class ExceptionTrace(
    val exceptionName: String,
    frameMetas: List<StackFrameMeta>,
    val kClass: KClass<*>? = null,
    val type:  TraceOptions.TraceType = TraceOptions.TraceType.Default
): PrettyPrint {

    constructor(
        exceptionName: String,
        stackFrames: List<StackFrameMeta>,
        reliable: Boolean,
        type:  TraceOptions.TraceType = TraceOptions.TraceType.Default
    ):this(exceptionName, stackFrames, type = type){
        isReliable = reliable
    }

    constructor(
        exception: Throwable,
        stackFrames: List<StackFrameMeta>,
        contextClass: KClass<*>? = null
    ): this(
        exception.throwableToText(),
        stackFrames,
        contextClass
    )

    var frameMetas: List<StackFrameMeta> = frameMetas
        internal set

    val created: Instant = Instant.now()

    var isReliable: Boolean = true
        internal set

    var bestPick: StackFrameMeta = frameMetas.first()

    override val formattedString: String get() = exceptionName.colorize(Colour.Red).newLine {
        bestPick.formattedString
    }

    init {
        require(frameMetas.isNotEmpty()) { "stackFrames must contain at least one frame" }
        if(!isReliable){
            "Created ExceptionTrace is considered to be unreliable".output(Colour.Yellow)
        }
    }

    override fun toString(): String {
        return buildString {
            appendLine(exceptionName)
            appendLine("Trace for ${bestPick.simpleClassName}")
            appendLine(bestPick)
        }
    }

    companion object {
        val harshFilter : (StackFrameMeta)-> Boolean = { frame->
            frame.isUserCode &&
            !frame.isHelperMethod &&
            !frame.isInline &&
            !frame.isLambda &&
            !frame.isReflection &&
            !frame.isThreadEntry &&
            !frame.isCoroutineInternal
        }
        fun callSiteReport(exceptionTrace: ExceptionTrace): CallSiteReport {
           return when {
                exceptionTrace.frameMetas.size <=2 ->{
                    CallSiteReport(exceptionTrace.frameMetas.last(), exceptionTrace.frameMetas.first())
                }
                exceptionTrace.frameMetas.size > 2 ->{
                    val first = exceptionTrace.frameMetas.first()
                    val last = exceptionTrace.frameMetas.last()
                    val takeSize = (exceptionTrace.frameMetas.size - 2).coerceAtLeast(0)
                    val sublist = exceptionTrace.frameMetas.drop(1).take(takeSize)
                    val filtered = sublist.filter { it.packageRole != PackageClassifier.PackageRole.Helper }
                    CallSiteReport(last, first, filtered.asReversed())
                }
                else -> {
                    val msg = "callSiteReport creation failure." +
                            "exceptionTrace.stackFrames count ${exceptionTrace.frameMetas.size}"
                    throw IllegalArgumentException(msg)
                }
            }
        }
    }
}