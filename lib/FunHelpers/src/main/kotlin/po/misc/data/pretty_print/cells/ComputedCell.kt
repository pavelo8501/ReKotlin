package po.misc.data.pretty_print.cells

import po.misc.collections.lambda_map.CallableDescriptor
import po.misc.collections.reactive_list.ReactiveList
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.formatters.text_modifiers.ColourCondition
import po.misc.data.pretty_print.formatters.text_modifiers.ConditionalTextModifier
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.formatters.text_modifiers.Formatter
import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.toProvider
import po.misc.data.strings.appendParam
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.functions.Throwing
import po.misc.reflection.displayName
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class ComputedCell<T, V>(
    override val receiverType: TypeToken<T>,
    val valueType: TypeToken<V>,
    opts: CommonCellOptions? = null,
    val builder: ComputedCell<T, V>.(V)-> Any
): PrettyCellBase(toOptions(opts,  keyless)), ReceiverAwareCell<T>, TextStyler, PrettyHelper {

    constructor(
        provider: DataProvider<T, V>,
        opts: CommonCellOptions? = null,
        builder: ComputedCell<T, V>.(V) -> Any
    ) : this(provider.receiverType, provider.valueType, opts, builder) {
        dataLoader.applyCallables(provider)
        provider[CallableDescriptor.CallableKey.ReadOnlyProperty]?.let {
            keyText = it.displayName
        }
    }
    val dataLoader: DataLoader<T, V> = DataLoader("ComputedCell", receiverType, valueType)
    override fun applyOptions(commonOpt: CommonCellOptions?): ComputedCell<T, V>{
        val options = PrettyHelper.toOptionsOrNull(commonOpt)
        if(options != null){
            cellOptions = options
        }
        return this
    }

    fun Colour.buildCondition(condition: T.()-> Boolean):DynamicColourModifier<T>{
        val colourCondition = ColourCondition<T>(this, condition)
        val dynamicColour =  textFormatter[Formatter.ColorModifier]?.safeCast<DynamicColourModifier<T>>()
        return if(dynamicColour != null){
            dynamicColour.addCondition(colourCondition)
        }else{
            val dynamicColour = DynamicColourModifier<T>(receiverType)
            dynamicColour.addCondition(colourCondition)
            textFormatter.addFormatter(dynamicColour)
            dynamicColour
        }
    }

    private fun renderValue(resolved: V, receiver:T):String{
        val fromBuilder = builder.invoke(this, resolved)
        val preformatted = resolved.stringify().formatted
        val formattedFromBuilder = fromBuilder.stringify().formatted
         if(fromBuilder is Unit || fromBuilder  is PrettyCellBase || fromBuilder is ConditionalTextModifier<*> ){
           return textFormatter.conditionalStyle(preformatted, receiver, receiverType)?:preformatted

        }else{
            return textFormatter.conditionalStyle(formattedFromBuilder, receiver, receiverType)?:formattedFromBuilder
        }
    }
    private fun justify(keyText:String, valueText:String):String{
        if(keyText.isNotBlank()){
            return justifyText("$keyText${cellOptions.keyValueSeparator} $valueText")
        }
        return justifyText(valueText)
    }
    override fun render(receiver: T, commonOptions: CommonCellOptions?): String {
        val key = styleKey()
        val value = dataLoader.resolveValue(receiver)
        if (value == null) {

            return justify(key, "null")
        } else {
            val rendered =  renderValue(value, receiver)
            return justify(key, rendered)
        }
    }

    override fun hashCode(): Int {
        var result = receiverType.hashCode()
        result = 31 * result + valueType.hashCode()
        result = 31 * result + builder.hashCode()
        result = 31 * result + dataLoader.hashCode()
        result = 31 * result + cellOptions.hashCode()
        return result
    }
    override fun equals(other: Any?): Boolean {
        if(other !is ComputedCell<*, *>) return false
        if(other.receiverType != receiverType) return false
        if(other.valueType != valueType) return false
        if(other.builder != builder) return false
        if(other.dataLoader != dataLoader) return false
        if(other.cellOptions != cellOptions) return false
        return true
    }
    override fun copy(): ComputedCell<T, V>{
        return ComputedCell(receiverType, valueType, cellOptions.copy(), builder)
    }
    override fun toString(): String {
        return buildString {
            append("ComputedCell")
            appendParam(" Width", cellOptions.width)
        }
    }

    companion object : PrettyHelper{
        val keyless : Options =  Options(CellPresets.KeylessProperty)
        inline operator fun <reified T, reified V> invoke(
            property: KProperty1<T, V>,
            opts: CommonCellOptions? = null,
            noinline builder: ComputedCell<T, V>.(V)-> Any
        ): ComputedCell<T, V>{
           return ComputedCell(property.toProvider(), opts, builder = builder)
        }
    }
}
