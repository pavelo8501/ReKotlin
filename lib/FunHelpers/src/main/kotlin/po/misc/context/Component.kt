package po.misc.context

import po.misc.data.logging.Verbosity
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.exceptions.ManagedException
import po.misc.exceptions.stack_trace.extractTrace

interface Component : TraceableContext{
    val verbosity: Verbosity
    val componentName: String

    val infoMessageFormatter: (String, String) -> String get() =  { msg, methodName ->
        "$componentName  -> $methodName ${SpecialChars.newLine}".colorize(Colour.Blue) +
                msg.colorize(Colour.WhiteBright)
    }

    val warnMessageFormatter: (String, String) -> String get() =  { msg, methodName ->
        "$componentName  -> $methodName ${SpecialChars.newLine}".colorize(Colour.Blue) +
                msg.colorize(Colour.YellowBright)
    }

    fun notify(message: String, methodName: String, useVerbosity: Verbosity = verbosity){
        if(useVerbosity == Verbosity.Info){
            val msg = infoMessageFormatter(message, methodName)
            println(msg)
        }
        if(useVerbosity == Verbosity.Warnings){
            val msg = warnMessageFormatter(message, methodName)
            println(msg)
        }
    }
    override fun warn(subject: String, text: String){
        notify(text, subject, Verbosity.Warnings)
    }
}

fun Component.managedException(message: String): ManagedException{
    val exception =  ManagedException(this, message)
    exception.extractTrace(this)
    return exception
}



fun <T:Component> T.classInfo(): String{
    return   buildString {
        appendLine(componentName)
        appendLine("Hash Code: ${hashCode()}")
    }
}