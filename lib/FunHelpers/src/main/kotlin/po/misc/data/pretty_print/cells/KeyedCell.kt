package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.Style
import po.misc.data.strings.FormattedPair
import po.misc.data.strings.appendParam
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


class KeyedCell<T: Any>(
    override val typeToken: TypeToken<T>,
    options: Options? = null,
): PrettyCellBase(options?:propertyOption), ReceiverAwareCell<T>, StaticRender {

    constructor(
        typeToken: TypeToken<T>,
        kProperty: KProperty1<T, Any?>,
        options: Options? = null
    ) : this(typeToken, options) {
        property = kProperty
        keyText =  kProperty.displayName
    }

    val plainKey: Boolean get() = row?.options?.plainKey?: cellOptions.plainKey
    val valueStyle: Style get() = cellOptions.style
    val keyStyle: Style get() = cellOptions.keyStyle

    var keyText: String = ""
        internal set

    private var useOwnSource:Boolean = false

    var property0: KProperty0<*>? = null
        internal set
    var property: KProperty1<T, Any?>? = null

    private fun styleKey(): String {
        if(!cellOptions.renderKey){
            return ""
        }
        val useForKey = cellOptions.useForKey
        val textToUse = useForKey ?: "$keyText :"
        if(!cellOptions.plainKey){
            return styler.modify(textToUse, cellOptions.keyStyle)
        }
        return textToUse
    }

    fun setSource(property: KProperty0<*>):KeyedCell<T> {
        property0 = property
        keyText = property.displayName
        useOwnSource = true
        return this
    }

    override fun applyOptions(commonOpt: CommonCellOptions?): KeyedCell<T>{
        val options = PrettyHelper.toOptionsOrNull(commonOpt)
        if(options != null){
            cellOptions = options
        }
        return this
    }

    override fun render(): String{
        val keyText = styleKey()
        val valueText = if(plainText){
            property0?.get()?.toString() ?: SpecialChars.EMPTY
        }else{
           val formated = property0?.get().stringify().formatedText
           styler.modify(formated)
        }
        val kevValueText = "$keyText $valueText"
        val final = justifyText(kevValueText,  cellOptions)
        return final
    }

    override fun render(formatted: FormattedPair, commonOptions: CommonCellOptions?): String{
        val options = PrettyHelper.toOptions(commonOptions, cellOptions)
        val usedText = formatted.formatedText
//        val modified = staticModifiers.modify(usedText)
//        val formatted = compositeFormatter.format(modified, this)
        val formatted =   textFormatter.style(usedText)
        val final = justifyText(formatted,  options)
        return final
    }
    override fun render(content: String, commonOptions: CommonCellOptions?): String {
        val options = PrettyHelper.toOptions(commonOptions, cellOptions)
//        val modified = staticModifiers.modify(content)
//        val formatted = compositeFormatter.format(modified, this)
        val formatted =   textFormatter.style(content)
        val final = justifyText(formatted,  options)
        return final
    }

    override fun render(receiver: T, commonOptions: CommonCellOptions?): String {
        val keyText = styleKey()
        val valueText = if (plainText) {
            property?.get(receiver).toString()
        } else {
            if(cellOptions.useSourceFormatting){
                property?.get(receiver).stringify().formatedText
            }else{
                textFormatter.style(property?.get(receiver).stringify().text)
            }
        }
        val text = if (cellOptions.renderKey) {
            "$keyText $valueText"
        } else {
            valueText
        }
        val final = justifyText(text, cellOptions)
        return final
    }

    override fun toString(): String =
         buildString {
            append("KeyedCell<${typeToken.simpleName}>")
            appendParam("Id", cellOptions.id)
            appendParam("Width", cellOptions.width)
            append(::keyText)
        }

    companion object{

        private val propertyOption = Options(CellPresets.Property)

        private val propertyNoKey: Options = Options(CellPresets.Property).also {
            it.renderKey = false
        }
    }

}
