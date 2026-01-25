package po.misc.data.pretty_print.parts.template

import po.misc.data.pretty_print.PrettyRowBase
import po.misc.data.pretty_print.TemplatePart
import po.misc.data.pretty_print.parts.rendering.KeyRenderParameters
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.error
import po.misc.types.getOrThrow


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


class RowDelegate<T>(){
    private var rowBacking: PrettyRowBase<*, T>? = null
    val row: PrettyRowBase<*, T> by lazy(LazyThreadSafetyMode.NONE) {
        rowBacking.getOrThrow {
            error("TemplateDelegate used before hos assigned", TraceOptions.ThisMethod)
        }
    }
    val keyParameters: KeyRenderParameters get() = row.keyParameters
    internal fun attachHost(row: PrettyRowBase<*, T>) {
        rowBacking = row
    }
}


class TemplateDelegate<T>(){
    private var hostBacking: TemplatePart<*>? = null
    val host: TemplatePart<*> by lazy(LazyThreadSafetyMode.NONE) {
        hostBacking.getOrThrow {
            error("TemplateDelegate used before hos assigned", TraceOptions.ThisMethod)
        }
    }
    val keyParameters: KeyRenderParameters get() = host.keyParameters
    internal fun attachHost(host: TemplatePart<*>) {
        hostBacking = host
    }
}