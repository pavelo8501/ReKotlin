package po.misc.data.pretty_print.rows

import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.reflection.Readonly
import po.misc.reflection.getBrutForced
import po.misc.reflection.resolveTypedProperty
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


/**
 * A row that renders once for each element inside a collection property.
 *
 * This row:
 * - extracts a collection via [property]
 * - iterates over each element
 * - renders itself individually for each element
 *
 * Example:
 * ```
 * ListContainingRow(User::orders) {
 *     addCell(Order::id)
 *     addCell(Order::status)
 * }
 * ```
 *
 * @param T type of the elements inside the collection
 * @param typeToken runtime type information for the collection type
 * @param property the property returning the collection of elements
 * @param id optional identifier for selective rendering
 * @param initialCells cells that define the structure of each list row
 */
class ListContainingRow<T: Any>(
    override val typeToken: TypeToken<T>,
    property: KProperty1<Any, Collection<T>>?,
    id: Enum<*>? = null,
    initialCells: List<PrettyCellBase<*>> = emptyList()
): PrettyRowBase(id, initialCells), RenderableElement<T>, TraceableContext {

    constructor(
        token: TypeToken<T>,
        property: KProperty1<Any, Collection<T>>,
        container: PrettyDataContainer
    ):this(token, property, initialCells = container.cells)

    constructor(
        token: TypeToken<T>,
        cells: List<PrettyCellBase<*>>,
        provider: () -> Collection<T>,
    ):this(token, null, initialCells = cells){
        providerBacking = provider
    }

    internal var  transitionPropertyBacking: KProperty1<Any, Collection<T>>? = null

    val  transitionProperty: KProperty1<Any, Collection<T>> get() {
        return transitionPropertyBacking.getOrThrow(KProperty1::class)
    }

    internal var providerBacking: (() -> Collection<T>)? = null
    val provider: () -> Collection<T> get() {
        return providerBacking.getOrThrow(this)
    }

    init {
        transitionPropertyBacking = property
    }

    override fun resolveReceiver(parentReceiver: Any): Collection<T> {
        if(transitionPropertyBacking != null){
            return transitionProperty.get(parentReceiver)
        }
        return provider.invoke()
    }

    companion object{

        @PublishedApi
        internal inline fun <reified T1 : Any> buildRow(
            property: KProperty<Collection<T1>>,
            parentToken: TypeToken<*>,
            preset: RowPresets? = null,
            noinline builder: CellContainer<T1>.() -> Unit
        ) : ListContainingRow<T1> {
            val token = TypeToken.create<T1>()
            val options = preset?.toOptions()
            val resolved = property.resolveTypedProperty(Readonly, parentToken, TypeToken.create<Collection<T1>>())
            if (resolved == null) {
                val errMsg = "switchProperty ${property.name} can not be resolved on class ${parentToken.simpleName}"
                throw IllegalArgumentException(errMsg)
            }
            val container = CellContainer(token)
            builder.invoke(container)
            val row = ListContainingRow(token, property = resolved, initialCells = container.cells)
            if (options != null) {
                row.options = options
            }
            return row
        }
    }
}