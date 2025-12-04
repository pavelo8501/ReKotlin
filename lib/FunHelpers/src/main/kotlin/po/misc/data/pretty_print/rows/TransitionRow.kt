package po.misc.data.pretty_print.rows

import po.misc.collections.asList
import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.reflection.Readonly
import po.misc.reflection.getBrutForced
import po.misc.reflection.resolveTypedProperty
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * A row whose receiver is not the main object but a nested property.
 *
 * `TransitionRow` receives:
 *
 * - a [typeToken] describing the nested receiver type
 * - an optional [id]
 * - an initial set of cells
 *
 * During rendering, a nested object is resolved (via `resolveReceiver(...)`)
 * and used to populate the row.
 *
 * Example:
 * ```
 * TransitionRow(User::address) {
 *     addCell(Address::street)
 *     addCell(Address::city)
 * }
 * ```
 *
 * @param T the nested receiver type for this row
 * @param typeToken runtime type information for the nested receiver
 * @param initialCells list of cells that form this row
 * @param id optional identifier for selective rendering
 */
class TransitionRow<PR: Any,  T: Any>(
    typeToken: TypeToken<T>,
    property: KProperty1<PR, T>?,
    initialCells: List<PrettyCellBase<*>> = emptyList(),
    options: RowOptions = RowOptions()
): PrettyRowBase<T>(typeToken, initialCells, options),  RenderableElement<PR, T>, TraceableContext {

    constructor(
        token: TypeToken<T>,
        initialCells: List<PrettyCellBase<*>>,
    ):this(token, null, initialCells)

    constructor(
        token: TypeToken<T>,
        property: KProperty1<PR, T>,
        container: CellContainerBase<T>
    ):this(token, property, container.cells,  container.options)

    constructor(
        token: TypeToken<T>,
        container: CellContainerBase<T>,
        provider: () -> T
    ):this(token, null, container.cells, container.options){
        providerBacking = provider
    }

    constructor(
        token: TypeToken<T>,
        row : PrettyRowBase<*>,
        provider: () -> T
    ):this(token, null, row.cells, row.options){
        providerBacking = provider
    }

    override val ids: List<Enum<*>> get() = options.id?.asList()?:emptyList()

    internal var transitionPropertyBacking: KProperty1<PR, T>? = null
    val  transitionProperty: KProperty1<PR, T> get() {
        return transitionPropertyBacking.getOrThrow(KProperty1::class)
    }

    internal var providerBacking: (() -> T)? = null
    val provider: () -> T get() {
        return providerBacking.getOrThrow(this)
    }

    init {
        transitionPropertyBacking = property
    }

    fun provideTransition(provider: () -> T){
        providerBacking = provider
    }

    override fun resolveReceiver(parent:PR):T{

        if(transitionPropertyBacking != null){
            return transitionProperty.getBrutForced(typeToken, parent)
        }
        return provider.invoke()
    }
    fun render(parentReceiver:PR, renderOnlyList:  List<Enum<*>>): String{
        val receiver = resolveReceiver(parentReceiver)
        return  runRender(receiver, rowOptions = null, renderOnlyList)
    }
    companion object{

        @PublishedApi
        internal inline fun <T: Any, reified T1: Any> buildRow(
            property: KProperty<T1>,
            parentClass: KClass<T>,
            rowPresets: RowPresets? = null,
            noinline builder: CellContainer<T1>.() -> Unit
        ): TransitionRow<T, T1> {
            val token = TypeToken.create<T1>()
            val options = rowPresets?.toOptions()

            return property.resolveTypedProperty(Readonly, parentClass, token)?.let { kProperty1->
                val constructor = CellContainer<T1>(token)
                builder.invoke(constructor)
                val realRow = TransitionRow<T, T1>(token, kProperty1, constructor)
                if(options != null){
                    realRow.options = options
                }
                realRow
            } ?: run {
                val errMsg = "switchProperty ${property.name} can not be resolved on class ${parentClass.simpleName}"
                throw IllegalArgumentException(errMsg)
            }
        }

        @PublishedApi
        internal inline fun <T: Any, reified T1 : Any> buildRow(
            property: KProperty<T1>,
            parentToken: TypeToken<T>,
            preset: RowPresets? = null,
            noinline builder: CellContainer<T1>.() -> Unit
        ): TransitionRow<T, T1> =  buildRow(property, parentToken.kClass, preset, builder)

    }
}




