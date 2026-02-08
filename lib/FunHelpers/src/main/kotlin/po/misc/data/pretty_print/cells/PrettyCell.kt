package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.strings.stringify
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.copyMutable
import po.misc.types.token.TypeToken
import kotlin.Any


class PrettyCell(
    opts: CellOptions? = null
): PrettyCellBase<Any>(PrettyHelper.toOptions(opts, plainText), TypeToken<Any>()), AnyRenderingCell, PrettyHelper{

    constructor(presets: CellPresets):this(){
        applyOptions( presets.asOptions())
    }
    constructor(width: Int, options:Options = Options(width)):this(options){
        cellOptions.width = width
    }

    override fun render(content: Any, opts: CellOptions?): String {
        if(!explicitOptions){
            renderOptions = toOptions(opts, renderOptions)
        }
        val content = content.stringify()
        return finalizeRender( RenderRecord(content.copyMutable(), null, null))
    }
    fun render(content: Any, optionBuilder: (Options) -> Unit): String{
        optionBuilder.invoke(renderOptions)
        val content = content.stringify()
        return finalizeRender(RenderRecord(content.copyMutable(), null, null))
    }

    override fun CellParameters.renderInScope(receiver: Any): TextSpan {
        val content = receiver.stringify()
        return finalizeScopedRender(RenderRecord(content.copyMutable(), null, null))
    }

    override fun applyOptions(opts: CellOptions?): PrettyCell{
        val options = PrettyHelper.toOptionsOrNull(opts)
        if(options != null){
            setOptions(options)
        }
        return this
    }

    override fun copy(): PrettyCell{
        return  PrettyCell(cellOptions.copy())
    }
    override fun toString(): String {
       return buildString {
           append("PrettyCell")
           append("Width: ${cellOptions.width}")
        }
    }

    companion object {
        val plainText: Options = Options(CellPresets.PlainText)
    }
}

