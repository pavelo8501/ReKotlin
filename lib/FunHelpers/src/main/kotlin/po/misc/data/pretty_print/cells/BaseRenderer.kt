package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.Colorizer


interface BaseRenderer <P : StylePresets> : Colorizer {

    val options: CommonCellOptions

    fun render(content: String, renderOptions: CommonRenderOptions): String
    fun render(formatted: FormattedPair, renderOptions: CommonRenderOptions): String
    fun addModifiers(vararg modifiers: TextModifier)
}

interface CellRenderer :  BaseRenderer<PrettyPresets>
interface KeyedCellRenderer : BaseRenderer<KeyedPresets>


