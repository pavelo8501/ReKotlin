package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.Colorizer


interface BaseRenderer <P : StylePresets> : Colorizer {
    var preset : P?
    val options: CommonCellOptions

    fun render(content: String): String
    fun render(formatted: FormattedPair, renderOptions: RenderOptions): String
    fun addModifiers(vararg modifiers: TextModifier)
}

interface CellRenderer :  BaseRenderer<PrettyPresets>{
    override var preset : PrettyPresets?
}

interface KeyedCellRenderer : BaseRenderer<KeyedPresets> {
    override var preset : KeyedPresets?
}


