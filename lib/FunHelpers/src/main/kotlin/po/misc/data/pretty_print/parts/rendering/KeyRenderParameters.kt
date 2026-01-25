package po.misc.data.pretty_print.parts.rendering

import po.misc.data.pretty_print.TemplatePart
import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.Layout

class KeyRenderParameters(
    options: RowOptions? = null,
): RenderParameters{

    private var sizeUpdated: Boolean = false
    override var contentWidth: Int = 0
        private set(value) {
            if(field < value){
                field = value
                sizeUpdated = true
            }
        }

    internal var containerMaxWidth: Int = 0

    override var maxWidth: Int = 0
        private set

    override val leftOffset: Int = 0
    override var index: Int = 0
    override var layout : Layout = Layout.Compact
    override var orientation: Orientation = Orientation.Horizontal
    override var align: Align = Align.Left

    private var onUpdated: ((RenderParameters)-> Unit)? = null

    init {
        options?.let {
            initByOptions(it)
        }
    }

    private fun adjustMaxSize(size: Int){
        if (layout == Layout.Stretch){
            if(containerMaxWidth != 0){
                maxWidth = containerMaxWidth
            }
        }
    }
    fun onUpdated(callback: (RenderParameters)-> Unit){
        onUpdated = callback
    }

    fun updateWidth(width: Int){
        contentWidth = width
        if(sizeUpdated){
            adjustMaxSize(width)
            sizeUpdated = false
            onUpdated?.invoke(this)
        }
    }

    fun initByOptions(opts: RowOptions){
        layout = opts.layout
        align = opts.align
        orientation = opts.orientation
        opts.viewport?.let {
            maxWidth = it.size
        }
        onUpdated?.invoke(this)
    }

    fun implyConstraints(params: RenderParameters){
        if(params.maxWidth != 0){
            containerMaxWidth = params.maxWidth
        }
    }

    fun createSnapshot(templatePart: TemplatePart<*>):RenderSnapshot{
        return RenderSnapshot(templatePart.templateID.name, this)
    }

    fun createSnapshot(name: String):RenderSnapshot{
        return RenderSnapshot(name, this)
    }
}