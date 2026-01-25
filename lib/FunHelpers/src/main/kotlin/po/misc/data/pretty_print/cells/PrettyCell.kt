package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.rendering.CellParameters
import po.misc.data.strings.stringify
import po.misc.types.token.TypeToken
import kotlin.Any


class PrettyCell(
    opts: CellOptions? = null
): PrettyCellBase<Any>(PrettyHelper.toOptions(opts, plainText), TypeToken<Any>()), SourceLessCell, AnyRenderingCell, PrettyHelper{

    constructor(presets: CellPresets):this(){
        applyOptions( presets.asOptions())
    }
    constructor(width: Int, options:Options = Options(width)):this(options){
        cellOptions.width = width
    }

    override fun render(content: Any, opts: CellOptions?): String {
        if(!areOptionsExplicit){
            currentRenderOpts = toOptions(opts, currentRenderOpts)
        }
        val content = content.stringify()
        return finalizeRender( RenderRecord(content.copyAsStacked(), null, null))
    }
    fun render(content: Any, optionBuilder: (Options) -> Unit): String{
        optionBuilder.invoke(currentRenderOpts)
        val content = content.stringify()
        return finalizeRender(RenderRecord(content.copyAsStacked(), null, null))
    }

    override fun CellParameters.scopedRender(receiver: Any): RenderRecord {
        val content = receiver.stringify()
        return finalizeScopedRender(RenderRecord(content.copyAsStacked(), null, null))
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

