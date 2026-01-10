package po.misc.data.pretty_print.cells

import po.misc.callbacks.callable.CallableCollection
import po.misc.callbacks.callable.ProviderProperty
import po.misc.collections.asList
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.ElementProvider
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Style
import po.misc.data.strings.FormattedText
import po.misc.data.strings.StringifyOptions
import po.misc.data.strings.appendParam
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.functions.CallableKey
import po.misc.reflection.displayName
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

class KeyedCell<T>(
    callable: CallableCollection<T, Any?>,
    opt: CellOptions? = null,
): PrettyCellBase<T>(PrettyHelper.toOptions(opt, keyedOption), callable.parameterType), SourceAwareCell<T>, PrettyHelper{

    constructor(loader:  DataLoader<T, Any?>, opt: CellOptions? = null):this(loader.elementRepository, opt){
        dataLoader.listRepository.apply(loader.listRepository)
    }
    override val sourceType: TypeToken<T> = callable.parameterType
    val receiverType: TypeToken<Any?> = callable.resultType
    val dataLoader: DataLoader<T, Any?> = DataLoader("KeyedCell loader", sourceType, receiverType)
    val valueStyle: Style get() = cellOptions.style
    val keyStyle: Style get() = cellOptions.keyStyle


    init {
        dataLoader.apply(callable)
        dataLoader[CallableKey.Property]?.let {
            keyText = it.displayName
        }
    }

    private fun resolveValues(receiver:T):List<Any?> {
        if(!dataLoader.canResolve){
            warn("Unable to resolve values. All data sources are empty")
        }
        return dataLoader.resolveList(receiver)
    }

    override fun render(source: T, opts: CellOptions?): String {
        if(!areOptionsExplicit){
            currentRenderOpts = toOptions(opts, currentRenderOpts)
        }
        val valueList =  resolveValues(source)
        val formatted =  FormattedText()
        valueList.forEach{
           val pair = it.stringify()
           formatted.add(pair)
        }
        val result = formatted.joinSubEntries(StringifyOptions.ElementOptions(SpecialChars.EMPTY))
        return finalizeRender(RenderRecord(result))
    }


    fun render(source:T, optionBuilder: (Options) -> Unit): String{
        optionBuilder.invoke(currentRenderOpts)
        areOptionsExplicit = true
        return render(source)
    }

    override fun applyOptions(opts: CellOptions?): KeyedCell<T>{
        val options = PrettyHelper.toOptionsOrNull(opts)
        if(options != null){
            setOptions(options)
        }
        return this
    }
    override fun copy(): KeyedCell<T>{
        return KeyedCell(dataLoader.copy(),  cellOptions.copy()).also {
            it.keyText = keyText
        }
    }
    override fun toString(): String = buildString {
        append("KeyedCell<${sourceType.simpleName}> ")
        appendParam(" Width", cellOptions.width)
        appendParam(::keyText)
    }

    companion object : TokenFactory{
        val valueType : TypeToken<Any?> = TypeToken<Any?>()
        val keyedOption: Options = Options(CellPresets.Property)
        inline operator fun <reified T> invoke(
            property: KProperty1<T, *>,
            opts: CellOptions? = null
        ): KeyedCell<T>{
            return KeyedCell(property.toElementProvider(), opts)
        }
        operator fun <T> invoke(
            receiverType:TypeToken<T>,
            property: KProperty1<T, *>,
            opts: CellOptions? = null
        ): KeyedCell<T>{
            return KeyedCell(property.toElementProvider(receiverType), opts)
        }

        operator fun <T> invoke(
            receiverType:TypeToken<T>,
            property: KProperty0<Any?>,
            opts: CellOptions? = null
        ): KeyedCell<T> = KeyedCell(ProviderProperty(receiverType, property), opts)

        inline operator fun <reified T> invoke(
            receiver:T,
            property: KProperty0<Any?>,
            opts: CellOptions? = null
        ): KeyedCell<T> = KeyedCell(ProviderProperty(TypeToken<T>(), property), opts)

        inline operator fun <reified T> invoke(
            property: KProperty0<Any?>,
            opts: CellOptions? = null
        ): KeyedCell<T> = KeyedCell(ProviderProperty(TypeToken<T>(), property), opts)

    }
}
