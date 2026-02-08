package po.misc.interfaces.named

import po.misc.data.Postfix
import po.misc.data.Separator
import po.misc.data.logging.Topic
import po.misc.data.logging.Verbosity
import po.misc.data.output.NameHeader
import po.misc.data.output.Output
import po.misc.data.output.OutputBlock
import po.misc.data.output.OutputCompare
import po.misc.data.styles.Colour
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

interface NamedComponent: Named, TextStyler {

    val verbosity: Verbosity get() = Verbosity.Warnings

    val onBehalfName: TextSpan? get() = null
    val styledName: TextSpan get() {
        val styled = StyledPair(name, name.format(TextStyler.ValueTag.ClassName))
        return onBehalfName?.let {
            mergeSpans(Postfix(", "),  it, styled)
        }?:styled
    }

    fun Any?.output(colour: Colour? = null){
        val output = Output(styledName, this, colour)
        output.printAll()
    }

    fun output(
        styleCode: StyleCode? = null,
        builderAction: StringBuilder.()-> Unit
    ){
      val output = Output(styledName, builderAction)
      output.printAll()
    }

//    fun <R> outputBlock(block: OutputBlock.()-> R):R{
//        val output = OutputBlock(NameHeader(styledName))
//        output.printAll()
//        return block.invoke(output)
//    }

//    fun <R> outputBlock(verbosity: KProperty<Verbosity>,  block: OutputBlock.()-> R):R{
//        val output = OutputBlock(NameHeader(styledName))
//        output.printAll()
//        return block.invoke(output)
//    }

    fun notify(text: String, topic: Topic) {
        if(verbosity.minTopic <= topic){
            text.output(Colour.WhiteBright)
        }
    }
}

