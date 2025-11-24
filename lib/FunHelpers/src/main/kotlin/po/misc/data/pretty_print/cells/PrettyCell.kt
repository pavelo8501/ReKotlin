package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.Align
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.formatters.StringNormalizer
import po.misc.data.pretty_print.presets.KeyedPresets
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

    override val builder: (Int, Any) ->  PrettyCellBase<PrettyPresets> = { width, align ->
        val asAlign = align as? Align ?: Align.LEFT
        PrettyCell(width, asAlign)
    }

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

    fun applyPreset(preset: PrettyPresets): PrettyCellBase<*>{
        this.preset = preset
        return this
    }

    companion object{
        val builder: (Int, String) -> PrettyCellBase<PrettyPresets> = { width, cellName ->
            PrettyCell(width, Align.LEFT)
        }
    }

}

