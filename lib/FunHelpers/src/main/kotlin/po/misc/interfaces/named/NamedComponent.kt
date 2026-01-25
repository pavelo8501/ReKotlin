package po.misc.interfaces.named

import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.Verbosity
import po.misc.data.output.OutputCompare
import po.misc.data.output.OutputParameters
import po.misc.data.output.outputCompare
import po.misc.data.output.outputInternal
import po.misc.data.styles.Colour
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan

interface NamedComponent: Named {

    val onBehalfName: TextSpan? get() = null
    val displayName: TextSpan get() {
        val ownColorized = name.colorize(Colour.Blue)
        return onBehalfName?.let {
            val plainName = "${it.plain} -> $name "
            val styledName = "${it.styled} -> $ownColorized"
            StyledPair(plainName, styledName)
        } ?: run {
            StyledPair(name, ownColorized)
        }
    }

    val verbosity : Verbosity get() = Verbosity.Warnings

    fun Any?.output(colour: Colour? = null): Unit = outputInternal(OutputParameters(displayName, this,colour))

    fun output(
        styleCode: StyleCode? = null,
        builderAction: StringBuilder.()-> Unit
    ): Unit = outputInternal(OutputParameters(displayName, styleCode, preserveAnsi = true, builderAction))

    fun <T> T.outputCompare(other: T): Unit = outputInternal(OutputCompare(this, other, displayName))

    fun notify(text: String, topic: NotificationTopic) {
        if(verbosity.minTopic <= topic){
            text.output(Colour.WhiteBright)
        }
    }
}

