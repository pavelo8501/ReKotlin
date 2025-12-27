package po.misc.data.pretty_print

import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions


fun <T> PrettyRow<T>.renderPlain(receiver:T, orientation: Orientation = Orientation.Horizontal):String {
   val options = RowOptions(orientation).also {
        it.plainText = true
    }
    return render(receiver, options)
}