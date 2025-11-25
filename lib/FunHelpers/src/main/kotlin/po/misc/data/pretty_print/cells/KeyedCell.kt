package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.TextStyler
import kotlin.reflect.KProperty


class KeyedCell(width: Int, val cellName: String): PrettyCellBase<KeyedPresets>(width, Align.LEFT), KeyedCellRenderer {

    constructor(kProperty: KProperty<*>, cellName: String = kProperty.name) : this(width = 0, cellName) {
        property = kProperty
        options = KeyedCellOptions(KeyedPresets.Property)
    }

    override var preset: KeyedPresets? = null
    var property: KProperty<*>? = null

    override var options: CommonCellOptions = KeyedCellOptions()
    val keyedOptions: KeyedCellOptions get() = options as KeyedCellOptions

    init {

        textFormatter.formatter = { text, cell ->
            text
        }
        dynamicTextStyler.formatter = { text, cell, ->
            var keyText = ""
            if(keyedOptions.showKey){
                keyText = colorizeKey()
            }
            val useStyle = keyedOptions.styleOptions.style
            val useColour = keyedOptions.styleOptions.colour
            val useBackgroundColour = keyedOptions.styleOptions.backgroundColour
            val valueStyle = TextStyler.style(text, applyColourIfExists = false, useStyle, useColour, useBackgroundColour)
            "$keyText $valueStyle"
        }
    }

    private fun colorizeKey(): String {
        val useStyle = keyedOptions.keyStyleOptions.style
        val useColour = keyedOptions.keyStyleOptions.colour
        val useBackgroundColour = keyedOptions.keyStyleOptions.backgroundColour
        return TextStyler.style(cellName, useStyle, useColour, useBackgroundColour)
    }


    internal fun applyOptions(options: KeyedCellOptions): KeyedCell{
        this.options = options
        return this
    }

    override fun applyPreset(preset: KeyedPresets): KeyedCell {
        options = preset.toKeyedOptions(width)
        return this
    }

    override fun render(formatted: FormattedPair, renderOptions: RenderOptions): String{
        val usedText = if(renderOptions.usePlain){ formatted.text } else { formatted.formatedText }
        val modified = staticModifiers.modify(usedText)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
        return final
    }

    override fun render(content: String): String {
        val modified = staticModifiers.modify(content)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  RenderOptions())
        return final
    }

    companion object

}
