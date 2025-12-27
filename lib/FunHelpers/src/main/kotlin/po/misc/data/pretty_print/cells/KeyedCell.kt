package po.misc.data.pretty_print.cells

import po.misc.collections.asList
import po.misc.collections.lambda_map.CallableDescriptor
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Style
import po.misc.data.pretty_print.toProvider
import po.misc.data.strings.FormattedPair
import po.misc.data.strings.appendLineParam
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.reflection.displayName
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

class KeyedCell<T>(
    override val receiverType: TypeToken<T>,
    opt: CommonCellOptions? = null,
): PrettyCellBase(PrettyHelper.toOptions(opt, keyedOption)), ReceiverAwareCell<T>, TextStyler{

    constructor(
        provider: DataProvider<T, Any?>,
        opt: CommonCellOptions? = null,
    ) : this(provider.receiverType, opt) {
        dataLoader.applyCallables(provider)
        provider[CallableDescriptor.CallableKey.ReadOnlyProperty]?.let {
            keyText = it.displayName
        }
    }

    val dataLoader: DataLoader<T, Any?> = DataLoader("KeyedCell loader", receiverType, valueType)

    val valueStyle: Style get() = cellOptions.style
    val keyStyle: Style get() = cellOptions.keyStyle

    private val keySeparator : String get() {
       return if(cellOptions.renderKey){
            cellOptions.keyValueSeparator.toString()
        }else{
            SpecialChars.EMPTY
        }
    }
    private var useOwnSource:Boolean = false

    var property0: KProperty0<*>? = null
        private set(value) {
            value?.let {
                field = it
                keyText = it.name
            }
        }

    private fun resolveValues(receiver:T):List<Any?> {
        val values = dataLoader.resolveList(receiver)
        if(values.isNotEmpty()){
            return values
        }
       return property0?.get()?.asList()?:emptyList()
    }
    private fun renderFormating(receiver:T): String{
        val valueList = resolveValues(receiver)
        return  valueList.joinToString{  textFormatter.style(it.stringify().formatted) }
    }
    private fun renderWithSourceFormatting(receiver:T): String{
        val valueList =  resolveValues(receiver)
        return  valueList.joinToString{  it.stringify().formatted }
    }
    private fun renderPlainText(receiver:T): String{
        val valueList =  resolveValues(receiver)
        return  valueList.joinToString{  it.toString() }
    }

    fun render(): String{
        val keyText = styleKey()
        val valueText = if(plainText){
            property0?.get()?.toString() ?: SpecialChars.EMPTY
        }else{
           val formated = property0?.get().stringify().formatted
           styler.modify(formated)
        }
        val kevValueText = "$keyText $valueText"
        val final = justifyText(kevValueText)
        return final
    }
    override fun render(formatted: FormattedPair, commonOptions: CommonCellOptions?): String{
        val options = PrettyHelper.toOptions(commonOptions, cellOptions)
        val usedText = formatted.formatted
        val formatted =   textFormatter.style(usedText)
        val final = justifyText(formatted)
        return final
    }
    override fun render(content: String, commonOptions: CommonCellOptions?): String {
        val options = PrettyHelper.toOptions(commonOptions, cellOptions)
        val formatted =   textFormatter.style(content)
        val final = justifyText(formatted)
        return final
    }
    override fun render(receiver: T, commonOptions: CommonCellOptions?): String {
        val keyText = styleKey()
        val valueText = if (plainText) {
            renderPlainText(receiver)
        } else {
            if(cellOptions.useSourceFormatting){
                renderWithSourceFormatting(receiver)
            }else{
                renderFormating(receiver)
            }
        }
        val text = "$keyText${keySeparator} $valueText".trim()
        val final = justifyText(text)
        return final
    }

    fun provideProperty(prop: KProperty0<Any?>){
        property0 = prop
        keyText = prop.displayName
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
    override fun copy(): KeyedCell<T>{
        val optCopy = cellOptions.copy()
        val keyedCellCopy = KeyedCell(receiverType)
        keyedCellCopy.keyText = keyText
        keyedCellCopy.applyOptions(optCopy)
        property0?.let {
            keyedCellCopy.provideProperty(it)
        }
        return keyedCellCopy
    }
    override fun equals(other: Any?): Boolean {
        if(other !is KeyedCell<*>) return false
        if(other.receiverType != receiverType) return false
        if(other.cellOptions != cellOptions) return false
        if(other.property0 != property0) return false
        return true
    }
    override fun hashCode(): Int {
        var result = receiverType.hashCode()
        result = 31 * result + (property0?.hashCode() ?: 0)
        result = 31 * result + cellOptions.hashCode()
        return result
    }
    override fun toString(): String = buildString {
        appendLine("KeyedCell<${receiverType.simpleName}>")
        appendLineParam("Width", cellOptions.width)
        appendLineParam(::keyText)
    }

    companion object : TokenFactory{

        val valueType : TypeToken<Any?> = TypeToken<Any?>()
        val keyedOption: Options = Options(CellPresets.Property)

        inline operator fun <reified T> invoke(
            property: KProperty1<T, *>,
            options: CommonCellOptions? = null
        ): KeyedCell<T>{
            val token = tokenOf<T>()
            return KeyedCell(property.toProvider(token), options)
        }
        inline operator fun <reified T> invoke(
            receiver:T,
            property: KProperty0<Any?>,
            options: Options = keyedOption
        ): KeyedCell<T>{
            val token = TypeToken<T>()
            val cell = KeyedCell(token, options)
            cell.provideProperty(property)
            return cell
        }

        inline operator fun <reified T> invoke(
            property: KProperty0<Any?>,
            options: Options = keyedOption
        ): KeyedCell<T>{
            val cell = KeyedCell(tokenOf<T>(), options)
            cell.provideProperty(property)
            return cell
        }
    }
}
