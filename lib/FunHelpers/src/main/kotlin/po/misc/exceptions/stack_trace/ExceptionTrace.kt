package po.misc.exceptions.stack_trace

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.throwableToText
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.text.appendLine



data class ExceptionTrace(
    val exceptionName: String,
    val stackFrames: List<StackFrameMeta>,
    val kClass: KClass<*>? = null
): PrettyPrint{

    constructor(
        exceptionName: String,
        stackFrames: List<StackFrameMeta>,
        reliable: Boolean
    ):this(exceptionName, stackFrames){
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
    }
}
