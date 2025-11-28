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
class TransitionRow<T: Any>(
    val typeToken: TypeToken<T>,
    initialCells: List<PrettyCellBase<*>> = emptyList(),
    id: Enum<*>? = null,
): PrettyRowBase(id, initialCells),  TraceableContext {

    constructor(
        typeToken: TypeToken<T>,
        vararg cells: PrettyCellBase<*>
    ):this(typeToken, cells.toList())

    constructor(
        token: TypeToken<T>,
        property: KProperty1<Any, T>,
        container: PrettyDataContainer
    ):this(token, container.cells,  container.id){
        transitionPropertyBacking = property
    }
    constructor(
        token: TypeToken<T>,
        container: PrettyDataContainer,
        provider: () -> T
    ):this(token, container.cells, container.id){
        providerBacking = provider
    }


    internal var  transitionPropertyBacking: KProperty1<Any, T>? = null
    val  transitionProperty: KProperty1<Any, T> get() {
        return transitionPropertyBacking.getOrThrow(KProperty1::class)
    }

    internal var providerBacking: (() -> T)? = null
    val provider: () -> T get() {
        return providerBacking.getOrThrow(this)
    }

    fun provideTransition(provider: () -> T){
        providerBacking = provider
    }

    fun resolveReceiver(parentReceiver:Any):T{
        if(transitionPropertyBacking != null){
            return transitionProperty.getBrutForced(typeToken, parentReceiver)
        }
        return provider.invoke()
    }

    companion object{

        @PublishedApi
        internal inline fun <reified T1 : Any> buildRow(
            property: KProperty<T1>,
            parentClass: KClass<*>,
            rowOptions: RowPresets? = null,
            noinline builder: CellContainer<T1>.() -> Unit
        ): TransitionRow<T1> {
            val token = TypeToken.create<T1>()
            val options = rowOptions?.toOptions()

            return property.resolveTypedProperty(Readonly, parentClass, token)?.let { kProperty1->
                val constructor = CellContainer<T1>(token)
                builder.invoke(constructor)
                val realRow = TransitionRow<T1>(token, kProperty1, constructor)
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
        internal inline fun <reified T1 : Any> buildRow(
            property: KProperty<T1>,
            parentToken: TypeToken<*>,
            preset: RowPresets? = null,
            noinline builder: CellContainer<T1>.() -> Unit
        ): TransitionRow<T1> =  buildRow(property, parentToken.kClass, preset, builder)

    }
}