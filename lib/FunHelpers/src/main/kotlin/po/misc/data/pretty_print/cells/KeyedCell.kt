package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.strings.FormattedPair
import po.misc.data.strings.classParam
import po.misc.data.strings.classProperty
import po.misc.data.styles.TextStyler
import po.misc.reflection.displayName
import kotlin.reflect.KProperty


class KeyedCell(
    val cellName: String,
    options: KeyedCellOptions = KeyedCellOptions(KeyedPresets.Property),
): PrettyCellBase<KeyedPresets>(options), KeyedCellRenderer {

    constructor(
        kProperty: KProperty<*>,
        cellName: String = kProperty.displayName,
        options: KeyedCellOptions = KeyedCellOptions(KeyedPresets.Property)
    ) : this(cellName, options) {
        property = kProperty
    }

    var property: KProperty<*>? = null

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
        options = preset.toKeyedOptions(options.width)
        return this
    }

    override fun render(formatted: FormattedPair, renderOptions: CommonRenderOptions): String{
        val usedText = if(renderOptions.usePlain){ formatted.text } else { formatted.formatedText }
        val modified = staticModifiers.modify(usedText)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
        return final
    }

    override fun render(content: String, renderOptions: CommonRenderOptions): String {
        val modified = staticModifiers.modify(content)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
        return final
    }

    override fun toString(): String {
        return buildString {
            appendLine("KeyedCell")
            classParam("id", options.id)
            classParam("width", options.width)
            classProperty(::cellName)
        }
    }

    companion object

}
