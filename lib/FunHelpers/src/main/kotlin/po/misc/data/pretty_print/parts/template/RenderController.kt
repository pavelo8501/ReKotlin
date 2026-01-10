package po.misc.data.pretty_print.parts.template

import po.misc.data.pretty_print.PrettyRowBase

class RenderController{

    private var row : PrettyRowBase<*, *>? = null

    var enable: Boolean = row?.enabled?:false
        set(value) {
            row?.let {
                it.enabled = value
            }
            field = value
        }
    internal fun bind (prettyRow: PrettyRowBase<*, *>) {
        row = prettyRow
    }

}