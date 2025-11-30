package po.misc.data.pretty_print.cells

import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.formatters.StringNormalizer
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.strings.classParam
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
    options: CellOptions = CellOptions()
): PrettyCellBase<PrettyPresets>(options), CellRenderer {

    constructor(width: Int):this(CellOptions(width))
    constructor(presets: PrettyPresets, width: Int = 0):this(presets.toOptions(width))
    override fun applyPreset(preset: PrettyPresets): PrettyCell{
        options = preset.toOptions()
        return this
    }

    override fun toString(): String {
       return buildString {
            appendLine("PrettyCell")
            classParam("id", options.id)
            classParam("width", options.width)
        }
    }
    companion object
}

