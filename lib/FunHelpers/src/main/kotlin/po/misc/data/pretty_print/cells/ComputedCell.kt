package po.misc.data.pretty_print.cells

import po.misc.callbacks.callable.CallableCollection
import po.misc.callbacks.callable.FunctionCallable
import po.misc.data.pretty_print.formatters.DynamicStyleFormatter
import po.misc.data.pretty_print.formatters.text_modifiers.ColourCondition
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.strings.appendParam
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.asMutable
import po.misc.functions.CallableKey
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class ComputedCell<S, T>(
    callable: CallableCollection<S, T>,
    opts: Options? = null,
    val builder: ComputedCell<S, T>.(T)-> Any
): PrettyCellBase<S>(opts, callable.parameterType), SourceAwareCell<S>, TextStyler, PrettyHelper {

    override val sourceType: TypeToken<S> = callable.parameterType
    val  receiverType: TypeToken<T> = callable.resultType

    val dataLoader: DataLoader<S, T> = DataLoader("ComputedCell", sourceType, receiverType)
    private var dynamicAttached: Boolean = false

    override val dynamicColour: DynamicColourModifier<T> = DynamicColourModifier<T>(receiverType)

    init {
        dataLoader.apply(callable)
        dataLoader[CallableKey.Provider]?.let {
            keyText = it.styledName.plain
        }
    }

    override fun applyOptions(opts: CellOptions?): ComputedCell<S, T>{
        return super.applyOptions(opts) as ComputedCell<S, T>
    }

    fun Colour.buildCondition(condition: T.()-> Boolean): DynamicColourModifier<T>{
        val colourCondition = ColourCondition<T>(this, condition)
        dynamicColour.add(colourCondition)
        if(!dynamicAttached){
            textFormatter.addFormatter(dynamicColour)
            dynamicAttached = true
        }
        return dynamicColour
    }

    private fun checkIfPositiveResult(builderResult: Any):Boolean{
        return !(builderResult is Unit || builderResult is PrettyCellBase<*> || builderResult is  DynamicStyleFormatter<*>)
    }

    private fun runDynamicConditions(span: MutableSpan,  value:T):MutableSpan{
        dynamicColour.modify(span, value)
        return span
    }
    private fun resolveReceiver(source: S):RenderRecord{
        val mutable = dataLoader.resolveValue(source)?.let { value ->
            val builderResult = builder.invoke(this, value)
            if(checkIfPositiveResult(builderResult)) {
                val span = builderResult.stringify().asMutable()
                runDynamicConditions(span, value)
            } else {
                builderResult.stringify()
            }
        }?: run {
            source.stringify()
        }
        return keyText?.let {
            createKeyed(mutable, it)
        }?:run {
            RenderRecord(mutable, null, null)
        }
    }

    override fun render(source: S, opts: CellOptions?): String {
        applyOptions(opts)
        val renderRecord = resolveReceiver(source)
        return finalizeRender(renderRecord)
    }
    fun render(source:S, optionBuilder: (Options) -> Unit): String{
        optionBuilder.invoke(cellOptions)
        applyOptions(cellOptions)
        val renderRecord = resolveReceiver(source)
        return finalizeRender(renderRecord)
    }

    override fun CellParameters.renderInScope(receiver: S): TextSpan {
        val renderRecord = resolveReceiver(receiver)
        return finalizeScopedRender(renderRecord)
    }

    override fun hashCode(): Int {
        var result = sourceType.hashCode()
        result = 31 * result + sourceType.hashCode()
        result = 31 * result + builder.hashCode()
        result = 31 * result + dataLoader.hashCode()
        result = 31 * result + cellOptions.hashCode()
        return result
    }
    override fun equals(other: Any?): Boolean {
        if(other !is ComputedCell<*, *>) return false
        if(other.sourceType != sourceType) return false
        if(other.builder != builder) return false
        if(other.dataLoader != dataLoader) return false
        if(other.cellOptions != cellOptions) return false
        return true
    }
    override fun copy(): ComputedCell<S, T>{
        return ComputedCell(dataLoader.elementRepository.copy(), cellOptions.copy(), builder)
    }
    override fun toString(): String {
        return buildString {
            append("ComputedCell")
            appendParam(" Width", cellOptions.width)
        }
    }

    companion object : PrettyHelper, TokenFactory{

        val keyless : Options =  Options(CellPresets.KeylessProperty)
        inline operator fun <reified S, reified T> invoke(
            property: KProperty1<S, T>,
            opts: CellOptions? = null,
            noinline builder: ComputedCell<S, T>.(T)-> Any
        ): ComputedCell<S, T>{
           val options = toOptionsOrNull(opts)
           return ComputedCell(property.toElementProvider(), options, builder = builder)
        }

        inline operator fun <S, reified T> invoke(
            sourceType: TypeToken<S>,
            property: KProperty1<S, T>,
            opts: CellOptions? = null,
            noinline builder: ComputedCell<S, T>.(T)-> Any
        ): ComputedCell<S, T>{
            val options = toOptionsOrNull(opts)
            return ComputedCell(property.toElementProvider(sourceType), options, builder = builder)
        }

        inline operator fun <reified S, reified T> invoke(
            noinline function: Function1<S, T>,
            opts: CellOptions? = null,
            noinline builder: ComputedCell<S, T>.(T)-> Any
        ): ComputedCell<S, T>{
            val options = toOptionsOrNull(opts)
            return ComputedCell(FunctionCallable<S, T>(function), options, builder = builder)
        }
    }
}
