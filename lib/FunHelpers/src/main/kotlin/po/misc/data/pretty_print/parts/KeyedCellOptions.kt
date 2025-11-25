package po.misc.data.pretty_print.parts

import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.parts.CellOptions.TextStyleOptions
import po.misc.data.pretty_print.presets.KeyedPresets


data class KeyedCellOptions(
    override val width: Int = 0,
    override val alignment: Align = Align.LEFT,
    override val styleOptions: TextStyleOptions = TextStyleOptions(),
    val keyStyleOptions : TextStyleOptions = TextStyleOptions(),
    val showKey: Boolean = true,
    val useKeyName: String? = null,
    private val emptySpaceFiller: Char? = null,
): CommonCellOptions{


    constructor(preset: KeyedPresets):this(width = 0, preset.align, preset.styleOption(), preset.keyStyleOption() )

    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()
}