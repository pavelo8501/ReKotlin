package po.misc.data.pretty_print.cells

import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.formatters.StringNormalizer
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.classParam
import po.misc.data.strings.stringify
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler
import po.misc.data.toDisplayName
import po.misc.debugging.ClassResolver
import po.misc.reflection.displayName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

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
    options: CellOptions = CellOptions(),
    row: PrettyRow<*>? = null
): PrettyCellBase<PrettyPresets>(options, row), CellRenderer {

    constructor(presets: PrettyPresets):this(presets.toOptions())
    constructor(width: Int,  row: PrettyRow<*>? = null):this(CellOptions(width), row)

    override fun applyPreset(preset: PrettyPresets): PrettyCell{
        cellOptions = preset.toOptions()
        return this
    }

    fun render(content: Any, commonOptions: CommonCellOptions?): String {
        val options = commonOptions?:cellOptions
        val text = if(options.usePlain){
            content.toString()
        }else{
            content.stringify().formatedString
        }
        val modified =  staticModifiers.modify(text)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }

    override fun toString(): String {
       return buildString {
            appendLine("PrettyCell")
            classParam("id", cellOptions.id)
            classParam("width", cellOptions.width)
        }
    }
    companion object
}

