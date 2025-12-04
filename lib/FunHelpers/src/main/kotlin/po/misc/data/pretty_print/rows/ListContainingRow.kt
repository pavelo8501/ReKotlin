package po.misc.data.pretty_print.rows

import po.misc.collections.asList
import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.section.PrettySection
import po.misc.reflection.Readonly
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
class ListContainingRow<PR: Any,  T: Any>(
    typeToken: TypeToken<T>,
    val prettyRows : List<PrettyRowBase<*>>,
    property: KProperty1<PR, Collection<T>>?,
    options: RowOptions = RowOptions()
): PrettyRowBase<T>(typeToken,  initialCells = emptyList(), options),  RenderableElement<PR, Collection<T>>, TraceableContext {

    constructor(
        token: TypeToken<T>,
        container: PrettySection<PR>,
        property: KProperty1<PR, Collection<T>>,
    ):this(token, container.prettyRows, property)

    constructor(
        token: TypeToken<T>,
        container: PrettySection<PR>,
        provider: () -> Collection<T>,
    ):this(token, container.prettyRows, property = null){
        providerBacking = provider
    }

    override val ids: List<Enum<*>> get() = prettyRows.mapNotNull { it.id }

    internal var  transitionPropertyBacking: KProperty1<PR, Collection<T>>? = null
    val  transitionProperty: KProperty1<PR, Collection<T>> get() {
        return transitionPropertyBacking.getOrThrow(KProperty1::class)
    }
    internal var providerBacking: (() -> Collection<T>)? = null
    val provider: () -> Collection<T> get() {
        return providerBacking.getOrThrow(this)
    }

    init {
        transitionPropertyBacking = property
    }

    internal fun <T: Any> runRender(
        receiverList: Collection<T>,
        renderOnlyList: List<Enum<*>>
    ): String{
        val stringBuilder = StringBuilder()
        receiverList.forEach {receiver->
            prettyRows.forEach { row ->
                val render = row.runRender(receiver, rowOptions = null, renderOnlyList)
                stringBuilder.appendLine(render)
            }
        }
        return stringBuilder.toString()
    }

    fun render(parent: PR, renderOnlyList: List<Enum<*>>): String{
        val receiverList =  resolveReceiver(parent)
        return  runRender(receiverList, renderOnlyList)
    }

    override fun resolveReceiver(parent: PR): Collection<T> {
        if(transitionPropertyBacking != null){
            return transitionProperty.get(parent)
        }
        return provider.invoke()
    }

    companion object{

        @PublishedApi
        internal inline fun <PR: Any, reified T : Any> buildRow(
            property: KProperty<Collection<T>>,
            parentToken: TypeToken<PR>,
            preset: RowPresets? = null,
            noinline builder: CellContainer<T>.() -> Unit
        ) : ListContainingRow<PR, T> {
            val token = TypeToken.create<T>()
            val options = preset?.toOptions()
            val resolved = property.resolveTypedProperty(Readonly, parentToken, TypeToken.create<Collection<T>>())
            if (resolved == null) {
                val errMsg = "switchProperty ${property.name} can not be resolved on class ${parentToken.simpleName}"
                throw IllegalArgumentException(errMsg)
            }
            val container = CellContainer(token)
            val parentRow = PrettyRow(container)
            if (options != null) {
                parentRow.options = options
            }
            builder.invoke(container)
            val row = ListContainingRow(token,  parentRow.asList() , property = resolved)
            return row
        }
    }
}