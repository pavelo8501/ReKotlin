package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.KeyedOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.KeyedPresets
import po.misc.data.pretty_print.parts.TextStyleOptions
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.strings.FormattedPair
import po.misc.data.strings.appendParam
import po.misc.data.styles.TextStyler
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class KeyedCell<T: Any>(
    override val typeToken: TypeToken<T>,
    val cellName: String,
    row: PrettyRow<T>? = null
): PrettyCellBase(KeyedOptions(KeyedPresets.Property)), ReceiverAwareCell<T> {

    constructor(
        typeToken: TypeToken<T>,
        kProperty: KProperty1<T, Any>,
        row: PrettyRow<T>? = null
    ) : this(typeToken, kProperty.displayName, row) {
        property = kProperty
    }

    var property: KProperty1<T, Any>? = null

    var keyedOptions: KeyedOptions
        get() = cellOptions as KeyedOptions
        set(value) {
          cellOptions = value
        }

    val valueStyle: TextStyleOptions get() = keyedOptions.styleOptions
    val keyStyle: TextStyleOptions get() = keyedOptions.keyStyleOptions

    init {
        textFormatter.formatter = { text, _ ->
            text
        }
        dynamicTextStyler.formatter = { text, _, ->
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

    override fun applyOptions(opt: CommonCellOptions?): KeyedCell<T>{
        opt?.let {
            keyedOptions = PrettyHelper.toKeyedOptions(it)
        }
        return this
    }

    override fun render(formatted: FormattedPair, commonOptions: CommonCellOptions?): String{
        val options = PrettyHelper.toKeyedOptions(commonOptions, keyedOptions)
        val usedText = formatted.formatedText
        val modified = staticModifiers.modify(usedText)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }
    override fun render(content: String, commonOptions: CommonCellOptions?): String {
        val options = PrettyHelper.toKeyedOptions(commonOptions, keyedOptions)
        val modified = staticModifiers.modify(content)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }
    override fun render(receiver: T, commonOptions: CommonCellOptions?): String {
        val usePlain = keyedOptions.usePlain
        val text = property?.get(receiver).toString()
        val modified =  if(usePlain){
            text
        }else{
           val byStatic = staticModifiers.modify(text)
           compositeFormatter.format(byStatic, this)
        }
        val final = justifyText(modified,  keyedOptions)
        return final
    }

    override fun toString(): String =
         buildString {
            append("KeyedCell<${typeToken.simpleName}>")
            appendParam("Id", keyedOptions.id)
            appendParam("Width", keyedOptions.width)
            append(::cellName)
        }

    companion object

}
