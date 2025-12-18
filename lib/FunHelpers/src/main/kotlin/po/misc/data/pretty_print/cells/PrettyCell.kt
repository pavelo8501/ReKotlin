package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.formatters.StringNormalizer
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.PrettyHelper
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
    row: PrettyRow<*>? = null
): PrettyCellBase(prettyCellOptions){

    constructor(presets: CellPresets):this(){
        applyOptions( presets.asOptions())
    }
    constructor(width: Int,  row: PrettyRow<*>? = null):this(row){
        applyOptions(Options(width))
    }

    fun render(content: Any, commonOptions: CellOptions? = null): String {
        applyOptions(PrettyHelper.toOptionsOrNull(commonOptions))
        val text = if(plainText){
            content.toString()
        }else{
            content.stringify().formatedString
        }
        val modified =  staticModifiers.modify(text)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  cellOptions)
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
           append("id", cellOptions.id)
           append("width", cellOptions.width)
        }
    }

    companion object {
        val prettyCellOptions: Options = Options().build {
            plainText = false
        }
    }
}

