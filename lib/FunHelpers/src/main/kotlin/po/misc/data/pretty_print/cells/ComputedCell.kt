package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.formatters.text_modifiers.ColourCondition
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.formatters.text_modifiers.Formatter
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.strings.appendParam
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import po.misc.functions.Throwing
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class ComputedCell<T: Any, V: Any>(
    override val typeToken: TypeToken<T>,
    valueToken: TypeToken<V>,
    property: KProperty1<T, V>?,
    val builder: ComputedCell<T, V>.(V)-> Any
): PrettyCellBase(defaultOptions), ReceiverAwareCell<T> {

    constructor(
        typeToken: TypeToken<T>,
        valueToken: TypeToken<V>,
        provider: () -> V,
        builder: ComputedCell<T, V>.(V) -> Any
    ) : this(typeToken, valueToken, property = null, builder = builder) {
        singleLoader.setProvider(provider)
    }


    val plainKey: Boolean get() = row?.options?.plainKey?: cellOptions.plainKey
    var keyText: String = ""
        internal set

    val singleLoader: ValueLoader<T, V> = ValueLoader("ComputedCell", typeToken, valueToken)

    init {
        property?.let {
            singleLoader.setProperty(it)
        }
    }
    private fun styleKey(): String {
        if(!cellOptions.renderKey){
            return ""
        }
        val useForKey = cellOptions.useForKey
        val textToUse = useForKey ?: "$keyText :"
        if(!plainKey){
            return styler.modify(textToUse, cellOptions.keyStyle)
        }
        return textToUse
    }

    override fun applyOptions(commonOpt: CommonCellOptions?): ComputedCell<T, V>{
        val options = PrettyHelper.toOptionsOrNull(commonOpt)
        if(options != null){
            cellOptions = options
        }
        return this
    }

    fun modifyOptions(opt: Options?): ComputedCell<T, V> {
        cellOptions.applyChanges(opt)
        return this
    }

    fun Colour.buildCondition(condition : T.()-> Boolean):DynamicColourModifier<T>{
        val colourCondition = ColourCondition<T>(this, condition)
        val dynamicColour =  textFormatter2[Formatter.ColorModifier]?.safeCast<DynamicColourModifier<T>>()
        return if(dynamicColour != null){
            dynamicColour.addCondition(colourCondition)
        }else{
            val dynamicColour = DynamicColourModifier<T>(typeToken)
            dynamicColour.addCondition(colourCondition)
            textFormatter2.addFormatter(dynamicColour)
            dynamicColour
        }
    }

    private fun renderValue(resolved: Any):String{
       val text = if(plainText){
            resolved.toString()
        }else{
           resolved.stringify().formatedString
       }
       return textFormatter2.style(text)
    }

    private fun renderValue(resolved: V, receiver:T):String{
        val modified = textFormatter2.conditionalStyle(resolved.toString(), receiver, typeToken)
        if(modified != null){
            return modified
        }
        val text = if(plainText){
            resolved.toString()
        }else{
            resolved.stringify().formatedString
        }
        return textFormatter2.style(text)
    }

    private fun reasonRenderValue(receiver:T, resolved: V, builderResult: Any):String{
       return when(builderResult){
            is PrettyCellBase -> renderValue(resolved)
            is String -> {
                if(TextStyler.hasStyles(builderResult)){
                    builderResult
                }else{
                    renderValue(builderResult)
                }
            }
            else -> renderValue(resolved, receiver)
        }
    }

    override fun render(receiver: T, commonOptions: CommonCellOptions?): String {
        val value = singleLoader.resolveValue(receiver, Throwing)
        val computed = builder.invoke(this, value)
        val valueText =  reasonRenderValue(receiver, value, computed)

        if(!cellOptions.renderKey){
            val  justifiedText = justifyText(valueText, cellOptions)
            return justifiedText
        }else{
            val keyText = styleKey()
            val  justifiedText = if(keyText.isNotEmpty()){
                justifyText("$keyText $valueText", cellOptions)
            }else{
                justifyText(valueText, cellOptions)
            }
            return justifiedText
        }
    }

    override fun toString(): String {
        return buildString {
            append("ComputedCell")
            appendParam(" Id", cellOptions.id)
            appendParam(" Width", cellOptions.width)
        }
    }

    companion object {
        val defaultOptions : Options get() = Options().also {
            it.renderKey = false
        }
       // val defaultOptions : KeyedOptions get() = KeyedOptions(renderKey = false)
    }
}
