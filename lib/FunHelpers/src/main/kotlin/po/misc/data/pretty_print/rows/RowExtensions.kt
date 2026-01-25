package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.options.InnerBorder
import po.misc.data.styles.Colour


fun PrettyRow<Any>.underlined(underlineChar: Char = '-', colour: Colour? = null):String {
    val opts = options
    TODO("underlined refactor")
   // opts.borders.bottomBorder  = InnerBorder(underlineChar, colour)
    return render(opts = opts)
}


fun <T> PrettyRow<T>.underlined(receiver:T,   underlineChar: Char = '-', colour: Colour? = null):String {
    val opts = options
    TODO("underlined refactor")
    //opts.borders.bottomBorder  = Border(underlineChar, colour)
    return render(receiver, opts = opts)
}

