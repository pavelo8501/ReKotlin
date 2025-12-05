package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.FormattedPair
import po.misc.data.strings.classParam
import po.misc.data.strings.classProperty
import po.misc.data.strings.stringify
import po.misc.data.styles.TextStyler
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


class KeyedCell<T: Any>(
    override val typeToken: TypeToken<T>,
    val cellName: String,
    options: KeyedCellOptions = KeyedCellOptions(KeyedPresets.Property),
    row: PrettyRow<T>? = null
): PrettyCellBase<KeyedPresets>(options, row), KeyedCellRenderer, ReceiverAwareCell<T> {

    constructor(
        typeToken: TypeToken<T>,
        property: KProperty1<T, Any>,
        options: KeyedCellOptions = KeyedCellOptions(KeyedPresets.Property),
        row: PrettyRow<T>? = null
    ) : this(typeToken, property.displayName, options, row) {
        property2 = property
    }

    var property: KProperty<*>? = null
    var property2: KProperty1<T, Any>? = null

    var keyedOptions: KeyedCellOptions
        get() = cellOptions as KeyedCellOptions
        set(value) {
            cellOptions = value
        }

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

    internal fun applyOptions(options: KeyedCellOptions): KeyedCell<T>{
        this.keyedOptions = options
        return this
    }

    override fun applyPreset(preset: KeyedPresets): KeyedCell<T> {
        cellOptions = preset.toKeyedOptions(cellOptions.width)
        return this
    }

    override fun render(formatted: FormattedPair, commonOptions: CommonCellOptions?): String{
        val usePlain = false
        val options = PrettyHelper.toKeyedCellOptionsOrDefault(commonOptions, keyedOptions)
        val usedText = if(usePlain){ formatted.text } else { formatted.formatedText }
        val modified = staticModifiers.modify(usedText)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }

    override fun render(content: String, commonOptions: CommonCellOptions?): String {

        val options = PrettyHelper.toKeyedCellOptionsOrDefault(commonOptions, keyedOptions)

        val modified = staticModifiers.modify(content)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }

    override fun toString(): String {
        return buildString {
            appendLine("KeyedCell")
            classParam("id", keyedOptions.id)
            classParam("width", keyedOptions.width)
            classProperty(::cellName)
        }
    }

    override fun render(receiver: T, commonOptions: CommonCellOptions?): String {
        val value = property2?.get(receiver).toString()
        val options = PrettyHelper.toKeyedCellOptionsOrDefault(commonOptions, keyedOptions)
        val modified = staticModifiers.modify(value)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }

    companion object

}
