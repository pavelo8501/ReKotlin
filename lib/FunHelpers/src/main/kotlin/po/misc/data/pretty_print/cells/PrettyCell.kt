package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.formatters.StringNormalizer
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.strings.stringify
import kotlin.text.append

/**
 * A single formatted cell used for pretty-printing aligned and styled text.
 *
 * A cell consists of:
 *  - a fixed or clamped width
 *  - optional alignment rule
 *  - optional text style (bold, italic…)
 *  - optional colour and background
 *  - optional postfix appended after formatting
 *  - optional dynamic [StringNormalizer] applied before styling
 *
 * A [PrettyCell] is *lightweight* and stateless; rendering happens per input.
 */
class PrettyCell(
    options:Options = Options()
): PrettyCellBase(prettyCellOptions), AnyRenderingCell{

    constructor(presets: CellPresets):this(){
        applyOptions( presets.asOptions())
    }
    constructor(width: Int, options:Options = Options(width)):this(options){
        cellOptions.width = width
    }

    override fun render(content: Any, commonOptions: CommonCellOptions?): String {
        applyOptions(PrettyHelper.toOptionsOrNull(commonOptions))
        val text = if(plainText){
            content.toString()
        }else{
            content.stringify().formatted
        }
        val formatted = textFormatter.style(text)
        val final = justifyText(formatted)
        return final
    }

    override fun applyOptions(commonOpt: CommonCellOptions?): PrettyCell{
        val options = PrettyHelper.toOptionsOrNull(commonOpt)
        if(options != null){
            cellOptions = options
        }
        return this
    }

    override fun toString(): String {
       return buildString {
           append("PrettyCell")
           append("width", cellOptions.width)
        }
    }

    override fun copy(): PrettyCell{
      return  PrettyCell(cellOptions.copy())
    }

    fun render(commonOptions: CommonCellOptions?): String {
       return "null"
    }
    companion object {
        val prettyCellOptions: Options = Options().build {
            plainText = false
        }
    }
}

