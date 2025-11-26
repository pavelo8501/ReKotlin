package po.misc.exceptions.stack_trace

import po.misc.collections.selectUntil
import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.TraceOptions
import po.misc.exceptions.throwableToText
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.text.appendLine


data class ExceptionTrace(
    val exceptionName: String,
    val stackFrames: List<StackFrameMeta>,
    val kClass: KClass<*>? = null,
    val type:  TraceOptions.TraceType = TraceOptions.TraceType.Default
): PrettyPrint{

    constructor(
        exceptionName: String,
        stackFrames: List<StackFrameMeta>,
        reliable: Boolean,
        type:  TraceOptions.TraceType = TraceOptions.TraceType.Default
    ):this(exceptionName, stackFrames, type = type){
        isReliable = reliable
    }

    val created: Instant = Instant.now()

    var isReliable: Boolean = true
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


    val callSiteReport: PrettyRow = buildPrettyRow<CallSiteReport> {
        addCell("Call site trace report")
        addCell("Caller trace snapshot")
        addCell(CallSiteReport::callerTraceMeta){
            it.methodName
        }
        addCell("Registration place snapshot")
        addCell(CallSiteReport::registrationTraceMeta){
            it.methodName
        }
    }

    fun printCallSite(){
        val report = callSiteReport(this)
        callSiteReport.render(report, RowPresets.VerticalRow).output()
    }

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

    companion object {
        val harshFilter : (StackFrameMeta)-> Boolean = {frame->
            frame.isUserCode &&
            !frame.isHelperMethod &&
            !frame.isInline &&
            !frame.isLambda &&
            !frame.isReflection &&
            !frame.isThreadEntry &&
            !frame.isCoroutineInternal
        }

        fun callSiteReport(exceptionTrace: ExceptionTrace):CallSiteReport{

           return when{
                exceptionTrace.stackFrames.size <=2 ->{
                    CallSiteReport(exceptionTrace.stackFrames.last(), exceptionTrace.stackFrames.first())
                }
                exceptionTrace.stackFrames.size > 2 ->{
                    val first = exceptionTrace.stackFrames.first()
                    val last = exceptionTrace.stackFrames.last()
                    val selected = exceptionTrace.stackFrames.drop(1).take(exceptionTrace.stackFrames.size - 2)
                    val reversed = selected.asReversed()
                    CallSiteReport(last, first, reversed)
                }
                else -> {
                    val msg = "callSiteReport creation failure." +
                            "exceptionTrace.stackFrames count ${exceptionTrace.stackFrames.size}"
                    throw IllegalArgumentException(msg)
                }
            }
        }

    }
}
