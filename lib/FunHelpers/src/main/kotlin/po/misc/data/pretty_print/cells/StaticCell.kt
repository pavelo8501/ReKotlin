package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.strings.FormattedText
import po.misc.data.strings.appendParam
import po.misc.data.styles.TextStyler
import po.misc.types.token.TypeToken


class StaticCell(
    val text :String,
    opts: CellOptions? = null
): PrettyCellBase<Unit>(PrettyHelper.toOptions(opts, plainText), TypeToken<Unit>()), StaticRenderingCell, SourceLessCell, TextStyler, PrettyHelper {

    internal fun resolve(): RenderRecord{
        return RenderRecord(FormattedText(text))
    }

    override fun render(opts: CellOptions?): String {
        if(!areOptionsExplicit){
            currentRenderOpts = toOptions(opts, currentRenderOpts)
        }
        return finalizeRender(resolve())
    }
    fun render(optionBuilder: (Options) -> Unit): String {
        optionBuilder.invoke(currentRenderOpts)
        areOptionsExplicit = true
        return  finalizeRender(resolve())
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