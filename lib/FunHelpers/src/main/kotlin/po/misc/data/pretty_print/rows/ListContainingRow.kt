package po.misc.data.pretty_print.rows

import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.reflection.Readonly
import po.misc.reflection.resolveTypedProperty
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


class ListContainingRow<T: Any>(
    override val typeToken: TypeToken<Collection<T>>,
    val property: KProperty1<Any, Collection<T>>,
    initialCells: List<PrettyCellBase<*>> = emptyList()
): PrettyRowBase(initialCells), RenderableElement<Collection<T>>, TraceableContext {

    constructor(
        token: TypeToken<Collection<T>>,
        property: KProperty1<Any, Collection<T>>,
        container: PrettyDataContainer
    ):this(token, property){
        setCells(container.prettyCells)
    }

    override fun resolveReceiver(parentReceiver: Any): Collection<T> {
        return property.get(parentReceiver)
    }

    companion object{

        @PublishedApi
        internal inline fun <reified T1 : Any> buildRow(
            property: KProperty<Collection<T1>>,
            parentToken: TypeToken<*>,
            preset: RowPresets? = null,
            noinline builder: CellContainer<Collection<T1>>.() -> Unit
        ) : ListContainingRow<T1> {
            val token = TypeToken.create<Collection<T1>>()
            val options = preset?.toOptions()

            val resolved = property.resolveTypedProperty(Readonly, parentToken, token)
            if (resolved == null) {
                val errMsg = "switchProperty ${property.name} can not be resolved on class ${parentToken.simpleName}"
                throw IllegalArgumentException(errMsg)
            }
            val container = CellContainer(token)
            builder.invoke(container)
            val row = ListContainingRow(token, resolved, container)
            if (options != null) {
                row.options = options
            }
            return row
        }
    }
}