package po.misc.data.pretty_print.parts.common

import po.misc.data.pretty_print.parts.decorator.DecoratorBorder
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler


interface ExtendedString : TextStyler{
    var text:String
    val styleCode: StyleCode?
    val size: Int get() = text.length
    val enabled:Boolean get() {
        return text.isEmpty()
    }
    val displaySize: Int get() {
        if(enabled) return size
        return 0
    }
}

fun ExtendedString.toSeparator(repeat: Int? = null): Separator{
    return when(this){
        is Separator -> Separator(text, styleCode, repeat?:this.repeat)
        is TaggedSeparator<*> -> Separator(text, styleCode, repeat?:this.repeat)
        is DecoratorBorder -> Separator(text, styleCode, repeat?:1)
        else -> Separator(text, styleCode, repeat?:1)
    }
}