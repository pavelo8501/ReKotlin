package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.Align
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler
import kotlin.reflect.KProperty


class KeyedCell(width: Int, val cellName: String): PrettyCellBase<KeyedPresets>(width, Align.LEFT), KeyedCellRenderer {

    override var preset: KeyedPresets? = null
    var property: KProperty<*>? = null
    var options: KeyedCellOptions = KeyedCellOptions()
        internal set

    override val builder: (Int, Any) -> PrettyCellBase<KeyedPresets> = Companion.builder

    constructor(kProperty: KProperty<*>, cellName: String = kProperty.name) : this(width = 0, cellName) {
        property = kProperty
        applyPreset(KeyedPresets.Property)
    }

    init {
        applyOptionSpecials()
        textFormatter.formatter = { text, cell ->
            text
        }
        dynamicTextStyler.formatter = { text, cell, ->
            val styledKey = if (cellName.isNotBlank() && options.showKey) {
                colorizeKey()
            } else {
                ""
            }
            val textStyle = preset?.style ?: TextStyle.Regular
            val valueStyle =
                TextStyler.style(text, applyColourIfExists = false, textStyle, preset?.colour, preset?.backgroundColour)
            "$styledKey $valueStyle"
        }
    }

    private fun colorizeKey(): String {
        val keyTextStyle = preset?.keyStyle ?: TextStyle.Regular
        return TextStyler.style(cellName, keyTextStyle, preset?.keyColour, preset?.backgroundColour)
    }

    private fun applyOptionSpecials(){
        if(options.colour != null){
            colour = options.colour
        }
    }

    internal fun applyOptions(options: KeyedCellOptions): KeyedCell{
        this.options = options
        applyOptionSpecials()
        return this
    }

    fun applyPreset(newPreset: KeyedPresets): KeyedCell {
        preset = newPreset
        return this
    }

    override fun render(content: String): String {
        val modified = staticModifiers.modify(content)
        val formatted = compositeFormatter.format(modified, this)
        val usedWidth = width.coerceAtMost(defaults.DEFAULT_WIDTH)
        val final = applyWidth(formatted, usedWidth)
        return final
    }

    companion object {

        val builder: (Int, Any) -> PrettyCellBase<KeyedPresets> = { width, cellName ->
            KeyedCell(width, cellName.toString())
        }
    }

}
