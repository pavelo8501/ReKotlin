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


class TransitionRow<T: Any>(
    override val typeToken: TypeToken<T>,
    initialCells: List<PrettyCellBase<*>> = emptyList()
): PrettyRowBase(initialCells), RenderableElement<T>, TraceableContext {

    constructor(typeToken: TypeToken<T>, vararg cells: PrettyCellBase<*>):this(typeToken, cells.toList())
    constructor(token: TypeToken<T>, property: KProperty1<Any, T>, container: PrettyDataContainer):this(token){
        setCells(container.prettyCells)
        transitionPropertyBacking = property
    }
    constructor(token: TypeToken<T>, transitionLambda: () -> T, container: PrettyDataContainer):this(token){
        setCells(container.prettyCells)
        transitionBacking = transitionLambda
    }

    internal var  transitionPropertyBacking: KProperty1<Any, T>? = null
    val  transitionProperty: KProperty1<Any, T> get() {
        return transitionPropertyBacking.getOrThrow(KProperty1::class)
    }

    internal var transitionBacking: (() -> T)? = null
    val transition: () -> T get() {
        return transitionBacking.getOrThrow(this)
    }

    fun provideTransition(provider: () -> T){
        transitionBacking = provider
    }
    override fun resolveReceiver(parentReceiver:Any):T{
        if(transitionPropertyBacking != null){
            return transitionProperty.getBrutForced(typeToken, parentReceiver)
        }
        return transition.invoke()
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