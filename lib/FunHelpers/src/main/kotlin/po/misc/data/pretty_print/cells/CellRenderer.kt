package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.formatters.TextModifier
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.styles.Colorizer


interface BaseRenderer <P : StylePresets> : Colorizer {
    var preset : P?
    fun render(content: String): String
    fun applyTextModifiers(vararg modifiers: TextModifier)
}

interface CellRenderer :  BaseRenderer<PrettyPresets>{
    override var preset : PrettyPresets?
}

interface KeyedCellRenderer : BaseRenderer<KeyedPresets> {
    override var preset : KeyedPresets?
}


