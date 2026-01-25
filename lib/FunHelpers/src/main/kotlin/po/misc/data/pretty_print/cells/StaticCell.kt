package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.rendering.CellParameters
import po.misc.data.strings.appendParam
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.MutablePair
import po.misc.types.token.TypeToken


class StaticCell(
    val text :String,
    opts: CellOptions? = null
): PrettyCellBase<Unit>(PrettyHelper.toOptions(opts, plainText), TypeToken<Unit>()), StaticRenderingCell, SourceLessCell, TextStyler, PrettyHelper {

    override fun render(opts: CellOptions?): String {
        
        if(!areOptionsExplicit){
            currentRenderOpts = toOptions(opts, currentRenderOpts)
        }

        return finalizeRender(RenderRecord(MutablePair(text),null, null))
    }
    fun render(optionBuilder: (Options) -> Unit): String {
        optionBuilder.invoke(currentRenderOpts)

        areOptionsExplicit = true
        return  finalizeRender(RenderRecord(MutablePair(text),null, null))
    }

    override fun CellParameters.scopedRender(receiver: Unit): RenderRecord {
        return finalizeScopedRender(RenderRecord(MutablePair(text),null, null))
    }
    override fun applyOptions(opts: CellOptions?): StaticCell{
        val options = toOptionsOrNull(opts)
        if(options!=null){
            setOptions(options)
        }
       return this
    }

    override fun copy(): StaticCell{
       return StaticCell(text, cellOptions.copy())
    }
    override fun toString(): String {
        return buildString {
            append("StaticCell")
            appendParam("Width", cellOptions.width)
            appendParam(::text)
        }
    }

    companion object {
        val plainText: Options = Options(CellPresets.PlainText)
        operator fun invoke(opts: CellOptions? = null, builderAction: StringBuilder.() -> Unit): StaticCell {
            val result = buildString(builderAction)
            return StaticCell(result, opts)
        }
    }
}