package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.formatters.StringNormalizer
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler

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
class PrettyCell(width: Int, align: Align = Align.LEFT): PrettyCellBase<PrettyPresets>(width, align), CellRenderer{

    constructor(width: Int, presets: PrettyPresets):this(width, Align.LEFT){
        preset = presets
    }

    override var preset: PrettyPresets? = null

    init {
        applyPresetSpecials()
        dynamicTextStyler.formatter = { text, cell, ->
            val textStyle = preset?.style?: TextStyle.Regular
            TextStyler.style(text, applyColourIfExists = false,  textStyle, preset?.colour, preset?.backgroundColour)
        }
    }

    private fun applyPresetSpecials(){
        val usedPreset = preset
        if(usedPreset != null && usedPreset.postfix != null){
            postfix = usedPreset.postfix?:""
        }
    }

    override fun applyPreset(preset: PrettyPresets): PrettyCell{
        options = preset.toOptions(width)
        return this
    }

    companion object

}

